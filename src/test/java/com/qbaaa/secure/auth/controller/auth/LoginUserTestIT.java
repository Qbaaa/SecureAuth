package com.qbaaa.secure.auth.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.dto.LoginRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import com.qbaaa.secure.auth.exception.RestErrorCodeType;
import com.qbaaa.secure.auth.exception.rest.ErrorDetails;
import com.qbaaa.secure.auth.repository.RefreshTokenRepositoryTest;
import com.qbaaa.secure.auth.repository.SessionRepositoryTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class LoginUserTestIT {

    private static final String API_POST_LOGIN = "/domains/{domainName}/auth/token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private static Stream<Arguments> inputCredentialsUser() {
        return Stream.of(
                Arguments.of("user001", "secretUser001"),
                Arguments.of("user002", "secretUser002")
        );
    }

    @ParameterizedTest(name = "#{index} - Run test with username = {0}, password = {1}")
    @MethodSource("inputCredentialsUser")
    @Sql(scripts = "classpath:test/db/clean_all_data.sql")
    @Sql(scripts = "classpath:test/db/data/auth/post_login.sql")
    void shouldLoginUserToApp(String username, String password) {
        try {
            // given
            final var domainName = "test-domain";
            final var loginRequest = new LoginRequest(username, password);

            assertAll(
                    "CHECK TABLES DATA BEFORE LOGIN TO APP",
                    () -> Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
                    () -> Assertions.assertEquals(0, refreshTokenRepositoryTest.countByUsername(username))
            );


            // when
            mockMvc.perform(MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    // then
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
                                "CHECK TABLES DATA AFTER LOGIN TO APP",
                                () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username)),
                                () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username))
                        );
                    });


        } catch (Exception e) {
            Assertions.fail("ERROR: " + e.getMessage());
        }

    }

    private static Stream<Arguments> inputCredentialsBadUser() {
        return Stream.of(
                Arguments.of("user001", "secretUser077"),
                Arguments.of("user002", "secretUser088"),
                Arguments.of("userNotExists", "secretPassword")
        );
    }

    @ParameterizedTest(name = "#{index} - Run test with username = {0}, password = {1}")
    @MethodSource("inputCredentialsBadUser")
    @Sql(scripts = "classpath:test/db/clean_all_data.sql")
    @Sql(scripts = "classpath:test/db/data/auth/post_login.sql")
    void shouldNoLoginUserToApp(String username, String password) {
        try {

            // given
            final var domainName = "test-domain";
            final var loginRequest = new LoginRequest(username, password);

            // when
            mockMvc.perform(MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    // then
                    .andExpect(result -> {
                        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
                        final var errorResponse =
                                objectMapper
                                        .readValue(result.getResponse().getContentAsByteArray(),
                                                ErrorDetails.class);
                        Assertions.assertNotNull(errorResponse);
                        assertAll(
                                "CHECK API RESPONSE",
                                () -> Assertions.assertEquals(RestErrorCodeType.CREDENTIALS_INVALIDATION.getErrorType(), errorResponse.code())
                        );
                    });


        } catch (Exception e) {
            Assertions.fail("ERROR: " + e.getMessage());
        }

    }

}
