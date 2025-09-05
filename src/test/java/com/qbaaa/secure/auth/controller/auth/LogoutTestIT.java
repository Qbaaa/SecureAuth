package com.qbaaa.secure.auth.controller.auth;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.auth.api.dto.LoginRequest;
import com.qbaaa.secure.auth.auth.api.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.auth.usecase.strategy.LoginUseCaseStrategy;
import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.repository.RefreshTokenRepositoryTest;
import com.qbaaa.secure.auth.repository.SessionRepositoryTest;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class LogoutTestIT {

  private static final String API_POST_LOGOUT = "/domains/{domainName}/auth/logout";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private LoginUseCaseStrategy loginUseCaseStrategy;

  @Autowired private SessionRepositoryTest sessionRepositoryTest;

  @Autowired private RefreshTokenRepositoryTest refreshTokenRepositoryTest;

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
        Arguments.of("master", "user001", "secretUser001"),
        Arguments.of("test-domain", "user002", "secretUser002"));
  }

  @ParameterizedTest(
      name = "#{index} - Run test with domainName = {0},  username = {1}, password = {2}")
  @MethodSource("inputCredentialsUser")
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_logout.sql")
  void shouldLogout(String domainName, String username, String password) {
    try {
      // given
      var loginRequest = new LoginRequest(username, password);
      var auth = loginUseCaseStrategy.authenticate(domainName, "http://localhost", loginRequest);

      assertAll(
          "CHECK TABLES DATA BEFORE LOGOUT",
          () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username)),
          () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username)));

      if (auth instanceof TokenResponse token) {
        var refreshTokenRequest = new RefreshTokenRequest(token.refreshToken());
        // when
        mockMvc
            .perform(
                MockMvcRequestBuilders.post(API_POST_LOGOUT, domainName)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                    .content(objectMapper.writeValueAsString(refreshTokenRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))

            // then
            .andExpect(
                result -> {
                  Assertions.assertEquals(
                      HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());

                  assertAll(
                      "CHECK TABLES DATA AFTER LOGOUT",
                      () ->
                          Assertions.assertEquals(
                              0, refreshTokenRepositoryTest.countByUsername(username)),
                      () ->
                          Assertions.assertEquals(
                              0, sessionRepositoryTest.countByUsername(username)));
                });
      }
    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_logout.sql")
  void shouldReturnErrorBadDomainLogout() {
    try {
      // given
      var username = "user001";
      var password = "secretUser001";
      var loginRequest = new LoginRequest(username, password);
      var auth = loginUseCaseStrategy.authenticate("master", "http://localhost", loginRequest);

      assertAll(
          "CHECK TABLES DATA BEFORE LOGOUT",
          () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username)),
          () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username)));

      if (auth instanceof TokenResponse token) {
        var refreshTokenRequest = new RefreshTokenRequest(token.refreshToken());

        // when
        mockMvc
            .perform(
                MockMvcRequestBuilders.post(API_POST_LOGOUT, "test-domain")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                    .content(objectMapper.writeValueAsString(refreshTokenRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))

            // then
            .andExpect(
                result -> {
                  Assertions.assertEquals(
                      HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());

                  assertAll(
                      "CHECK TABLES DATA AFTER LOGOUT",
                      () ->
                          Assertions.assertEquals(
                              1, refreshTokenRepositoryTest.countByUsername(username)),
                      () ->
                          Assertions.assertEquals(
                              1, sessionRepositoryTest.countByUsername(username)));
                });
      }

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }
}
