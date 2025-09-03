package com.qbaaa.secure.auth.controller.auth;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.auth.api.dto.LoginRequest;
import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.config.time.FakeTimeProvider;
import com.qbaaa.secure.auth.config.time.FakeTimeProviderConfig;
import com.qbaaa.secure.auth.repository.RefreshTokenRepositoryTest;
import com.qbaaa.secure.auth.repository.SessionRepositoryTest;
import com.qbaaa.secure.auth.repository.UserRepositoryTest;
import com.qbaaa.secure.auth.shared.exception.RestErrorCodeType;
import com.qbaaa.secure.auth.shared.exception.rest.ErrorDetails;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@Slf4j
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = FakeTimeProviderConfig.class)
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class LoginUserTestIT {

  private static final String API_POST_LOGIN = "/domains/{domainName}/auth/token";

  @Autowired private FakeTimeProvider fakeTimeProvider;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SessionRepositoryTest sessionRepositoryTest;

  @Autowired private RefreshTokenRepositoryTest refreshTokenRepositoryTest;

  @Autowired private UserRepositoryTest userRepositoryTest;

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

  private static Stream<Arguments> inputCredentialsUser() {
    return Stream.of(
        Arguments.of("user001", "secretUser001"), Arguments.of("user002", "secretUser002"));
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
          () -> Assertions.assertEquals(0, refreshTokenRepositoryTest.countByUsername(username)));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
                final var tokenResponse =
                    objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), TokenResponse.class);
                Assertions.assertNotNull(tokenResponse);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertNotNull(
                            tokenResponse.accessToken(), "Access token should not be null"),
                    () ->
                        Assertions.assertFalse(
                            tokenResponse.accessToken().isEmpty(),
                            "Access token should not be empty"),
                    () ->
                        Assertions.assertFalse(
                            tokenResponse.accessToken().isEmpty(),
                            "Access token should have a size greater than 0"),
                    () ->
                        Assertions.assertNotNull(
                            tokenResponse.refreshToken(), "Refresh token should not be null"),
                    () ->
                        Assertions.assertFalse(
                            tokenResponse.refreshToken().isEmpty(),
                            "Refresh token should not be empty"),
                    () ->
                        Assertions.assertFalse(
                            tokenResponse.refreshToken().isEmpty(),
                            "Refresh token should have a size greater than 0"));

                assertAll(
                    "CHECK TABLES DATA AFTER LOGIN TO APP",
                    () ->
                        Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username)),
                    () ->
                        Assertions.assertEquals(
                            1, refreshTokenRepositoryTest.countByUsername(username)));
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  private static Stream<Arguments> inputCredentialsBadUser() {
    return Stream.of(
        Arguments.of("user001", "secretUser077"),
        Arguments.of("user002", "secretUser088"),
        Arguments.of("userNotExists", "secretPassword"));
  }

  @ParameterizedTest(name = "#{index} - Run test with username = {0}, password = {1}")
  @MethodSource("inputCredentialsBadUser")
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login.sql")
  void shouldErrorWhenBadCredentials(String username, String password) {
    try {

      // given
      final var domainName = "test-domain";
      final var loginRequest = new LoginRequest(username, password);

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
                final var errorResponse =
                    objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(errorResponse);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.CREDENTIALS_INVALIDATION.getErrorType(),
                            errorResponse.code()));
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login.sql")
  void shouldErrorWhenUserNoActiveAccount() {
    try {
      // given
      final var domainName = "test-domain";
      final var loginRequest = new LoginRequest("user003NoActive", "secretUser003");

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
                final var errorResponse =
                    objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(errorResponse);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.USER_NO_ACTIVE_ACCOUNT.getErrorType(),
                            errorResponse.code()));
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login.sql")
  void shouldLoginToAppWhenAccountLockedAfterThreeFailedAttemptsAndUnlockedAfterOneMinute() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "user004";
      final var loginRequest = new LoginRequest(username, "wrongSecret");

      assertAll(
          "CHECK TABLES DATA BEFORE LOGIN TO APP",
          () -> Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
          () -> Assertions.assertEquals(0, refreshTokenRepositoryTest.countByUsername(username)));

      // when
      for (int i = 0; i < 3; i++) {
        log.info("_________________REQUEST {}_________________", (i + 1));
        mockMvc
            .perform(
                MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                    .content(objectMapper.writeValueAsString(loginRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is4xxClientError());
        log.info("_________________REQUEST END_________________");
        assertAll(
            "CHECK TABLES DATA AFTER LOGIN TO APP",
            () -> Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
            () -> Assertions.assertEquals(0, refreshTokenRepositoryTest.countByUsername(username)));
      }

      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.LOCKED.value(), result.getResponse().getStatus());
                final var errorResponse =
                    objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(errorResponse);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.ACCOUNT_LOCKED.getErrorType(), errorResponse.code()));
              });

      LocalDateTime newTime = LocalDateTime.now().plusMinutes(10);
      fakeTimeProvider.setLocalDateTime(newTime);

      final var loginRequestCorrect = new LoginRequest(username, "secretUser002");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequestCorrect))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), TokenResponse.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () -> Assertions.assertNotNull(response.accessToken()),
                    () -> Assertions.assertNotNull(response.refreshToken()));
              });

      assertAll(
          "CHECK TABLES DATA AFTER LOGIN TO APP",
          () -> Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username)),
          () -> Assertions.assertEquals(1, refreshTokenRepositoryTest.countByUsername(username)));

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login.sql")
  void shouldLoginUserToAppAndCleanAttemptsAndTimeWhenAfter2Attempts() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userLockClean";
      final var password = "secretUser002";
      final var loginRequest = new LoginRequest(username, password);

      assertAll(
          "CHECK TABLES DATA BEFORE LOGIN TO APP",
          () -> Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
          () -> Assertions.assertEquals(0, refreshTokenRepositoryTest.countByUsername(username)));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
                final var tokenResponse =
                    objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), TokenResponse.class);
                Assertions.assertNotNull(tokenResponse);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertNotNull(
                            tokenResponse.accessToken(), "Access token should not be null"),
                    () ->
                        Assertions.assertFalse(
                            tokenResponse.accessToken().isEmpty(),
                            "Access token should not be empty"),
                    () ->
                        Assertions.assertFalse(
                            tokenResponse.accessToken().isEmpty(),
                            "Access token should have a size greater than 0"),
                    () ->
                        Assertions.assertNotNull(
                            tokenResponse.refreshToken(), "Refresh token should not be null"),
                    () ->
                        Assertions.assertFalse(
                            tokenResponse.refreshToken().isEmpty(),
                            "Refresh token should not be empty"),
                    () ->
                        Assertions.assertFalse(
                            tokenResponse.refreshToken().isEmpty(),
                            "Refresh token should have a size greater than 0"));

                assertAll(
                    "CHECK TABLES DATA AFTER LOGIN TO APP",
                    () ->
                        Assertions.assertEquals(1, sessionRepositoryTest.countByUsername(username)),
                    () ->
                        Assertions.assertEquals(
                            1, refreshTokenRepositoryTest.countByUsername(username)),
                    () -> {
                      var user =
                          userRepositoryTest.findByDomainNameAndUsername(domainName, username);
                      Assertions.assertTrue(user.isPresent());
                      Assertions.assertEquals(0, user.get().getFailedLoginAttempts());
                      Assertions.assertTrue(user.get().getLastFailedLoginTime().isEmpty());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login.sql")
  void shouldErrorWhenAfter3AttemptsLessThanOneMinuteAgo() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userLockAccount";
      final var password = "secretUserWrong";
      final var loginRequest = new LoginRequest(username, password);

      assertAll(
          "CHECK TABLES DATA BEFORE LOGIN TO APP",
          () -> Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
          () -> Assertions.assertEquals(0, refreshTokenRepositoryTest.countByUsername(username)));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.LOCKED.value(), result.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.ACCOUNT_LOCKED.name(), response.code()));

                assertAll(
                    "CHECK TABLES DATA AFTER LOGIN TO APP",
                    () ->
                        Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
                    () ->
                        Assertions.assertEquals(
                            0, refreshTokenRepositoryTest.countByUsername(username)),
                    () -> {
                      var user =
                          userRepositoryTest.findByDomainNameAndUsername(domainName, username);
                      Assertions.assertTrue(user.isPresent());
                      Assertions.assertEquals(3, user.get().getFailedLoginAttempts());
                      Assertions.assertFalse(user.get().getLastFailedLoginTime().isEmpty());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login.sql")
  void shouldErrorWhenWrongPasswordAfter2AttemptsIncrease() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userLockIncrease";
      final var password = "secretUserWrong";
      final var loginRequest = new LoginRequest(username, password);

      assertAll(
          "CHECK TABLES DATA BEFORE LOGIN TO APP",
          () -> Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
          () -> Assertions.assertEquals(0, refreshTokenRepositoryTest.countByUsername(username)));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.CREDENTIALS_INVALIDATION.name(), response.code()));

                assertAll(
                    "CHECK TABLES DATA AFTER LOGIN TO APP",
                    () ->
                        Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
                    () ->
                        Assertions.assertEquals(
                            0, refreshTokenRepositoryTest.countByUsername(username)),
                    () -> {
                      var user =
                          userRepositoryTest.findByDomainNameAndUsername(domainName, username);
                      Assertions.assertTrue(user.isPresent());
                      Assertions.assertEquals(3, user.get().getFailedLoginAttempts());
                      Assertions.assertFalse(user.get().getLastFailedLoginTime().isEmpty());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }
}
