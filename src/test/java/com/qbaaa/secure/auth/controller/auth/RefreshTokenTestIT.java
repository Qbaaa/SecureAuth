package com.qbaaa.secure.auth.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.config.time.FakeTimeProvider;
import com.qbaaa.secure.auth.config.time.FakeTimeProviderConfig;
import com.qbaaa.secure.auth.dto.LoginRequest;
import com.qbaaa.secure.auth.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import com.qbaaa.secure.auth.exception.rest.ErrorDetails;
import com.qbaaa.secure.auth.repository.RefreshTokenRepositoryTest;
import com.qbaaa.secure.auth.repository.SessionRepositoryTest;
import com.qbaaa.secure.auth.service.strategy.LoginStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FakeTimeProviderConfig.class)
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class RefreshTokenTestIT {

    private static final String API_POST_REFRESH_TOKEN = "/auth/domains/{domainName}/token";

    @Autowired
    private FakeTimeProvider fakeTimeProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoginStrategyService loginStrategyService;

    @Autowired
    private SessionRepositoryTest sessionRepositoryTest;

    @Autowired
    private RefreshTokenRepositoryTest refreshTokenRepositoryTest;

    @BeforeAll
    static void beforeAll(TestInfo testInfo) {
        log.info("--------------------------------------------------");
        log.info("--------------------------------------------------");
        log.info("START TEST CLASS: " + testInfo.getTestClass().get());
        log.info("--------------------------------------------------");
        log.info("--------------------------------------------------");
    }

    @BeforeEach
    void setup() {
        fakeTimeProvider.setInstance(Instant.now());
        fakeTimeProvider.setLocalDateTime(LocalDateTime.now());
    }

    @Test
    @Sql(scripts = "classpath:test/db/clean_all_data.sql")
    @Sql(scripts = "classpath:test/db/data/auth/post_refresh_token.sql")
    void shouldRefreshToken() {
        try {
            // given
            final var domainName = "test-domain";
            var username = "user002";
            var loginRequest = new LoginRequest(username,"secretUser002");
            var token = loginStrategyService.authenticate(domainName, "http://localhost", loginRequest);

            assertAll(
                    "CHECK TABLES DATA BEFORE REFRESH TOKEN",
                    () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username)),
                    () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username))
            );

            var refreshTokenOldRequest = new RefreshTokenRequest(token.refreshToken());

            // when
            mockMvc.perform(MockMvcRequestBuilders.post(API_POST_REFRESH_TOKEN, domainName)
                            .content(objectMapper.writeValueAsString(refreshTokenOldRequest))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))

                    //then
                    .andExpect(result -> {
                        Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
                        final var tokenResponse =
                                objectMapper
                                        .readValue(result.getResponse().getContentAsByteArray(),
                                                TokenResponse.class);
                        Assertions.assertNotNull(tokenResponse);
                        assertAll(
                                "CHECK API RESPONSE",

                                () -> Assertions.assertNotNull(tokenResponse.accessToken(),
                                        "Access token should not be null"),
                                () -> Assertions.assertFalse(tokenResponse.accessToken().isEmpty(),
                                        "Access token should not be empty"),
                                () -> Assertions.assertFalse(tokenResponse.accessToken().isEmpty(),
                                        "Access token should have a size greater than 0"),

                                () -> Assertions.assertNotNull(tokenResponse.refreshToken(),
                                        "Refresh token should not be null"),
                                () -> Assertions.assertFalse(tokenResponse.refreshToken().isEmpty(),
                                        "Refresh token should not be empty"),
                                () -> Assertions.assertFalse(tokenResponse.refreshToken().isEmpty(),
                                        "Refresh token should have a size greater than 0")
                        );

                        assertAll(
                                "CHECK TABLES DATA AFTER REFRESH TOKEN",
                                () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username)),
                                () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username)),
                                () -> Assertions.assertTrue(refreshTokenRepositoryTest
                                        .existsByToken(tokenResponse.refreshToken()))
                        );
                    });

        } catch (Exception e) {
            Assertions.fail("ERROR: " + e.getMessage());
        }

    }

    @Test
    @Sql(scripts = "classpath:test/db/clean_all_data.sql")
    @Sql(scripts = "classpath:test/db/data/auth/post_refresh_token.sql")
    void shouldReturnErrorWhenRefreshTokenExpired() {
        try {
            // given
            Instant newTime = Instant.parse("2025-03-31T10:00:00Z");
            fakeTimeProvider.setInstance(newTime);
            final var domainName = "test-domain";
            var username = "user002";
            var loginRequest = new LoginRequest(username,"secretUser002");
            var token = loginStrategyService.authenticate(domainName, "http://localhost", loginRequest);

            assertAll(
                    "CHECK TABLES DATA BEFORE REFRESH TOKEN",
                    () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username)),
                    () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username))
            );

            var refreshTokenOldRequest = new RefreshTokenRequest(token.refreshToken());

            // when
            mockMvc.perform(MockMvcRequestBuilders.post(API_POST_REFRESH_TOKEN, domainName)
                            .content(objectMapper.writeValueAsString(refreshTokenOldRequest))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))

                    //then
                    .andExpect(result -> {
                        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
                        final var errorResponse =
                                objectMapper
                                        .readValue(result.getResponse().getContentAsByteArray(),
                                                ErrorDetails.class);
                        Assertions.assertNotNull(errorResponse);

                    });

            assertAll(
                    "CHECK TABLES DATA AFTER REFRESH TOKEN",
                    () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username)),
                    () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username))
            );

        } catch (Exception e) {
            Assertions.fail("ERROR: " + e.getMessage());
        }

    }

    @Test
    @Sql(scripts = "classpath:test/db/clean_all_data.sql")
    @Sql(scripts = "classpath:test/db/data/auth/post_refresh_token.sql")
    void shouldReturnErrorWhenSessionExpired() {
        try {
            // given
            LocalDateTime newTime = LocalDateTime.of(2025, 3, 31, 10, 0, 0);
            fakeTimeProvider.setLocalDateTime(newTime);
            final var domainName = "test-domain";
            var username = "user002";
            var loginRequest = new LoginRequest(username,"secretUser002");
            var token = loginStrategyService.authenticate(domainName, "http://localhost", loginRequest);

            assertAll(
                    "CHECK TABLES DATA BEFORE REFRESH TOKEN",
                    () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username)),
                    () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username))
            );

            var refreshTokenOldRequest = new RefreshTokenRequest(token.refreshToken());

            // when
            mockMvc.perform(MockMvcRequestBuilders.post(API_POST_REFRESH_TOKEN, domainName)
                            .content(objectMapper.writeValueAsString(refreshTokenOldRequest))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))

                    //then
                    .andExpect(result -> {
                        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
                        final var errorResponse =
                                objectMapper
                                        .readValue(result.getResponse().getContentAsByteArray(),
                                                ErrorDetails.class);
                        Assertions.assertNotNull(errorResponse);
                    });

            assertAll(
                    "CHECK TABLES DATA AFTER REFRESH TOKEN",
                    () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username)),
                    () -> Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username))
            );

        } catch (Exception e) {
            Assertions.fail("ERROR: " + e.getMessage());
        }

    }

}
