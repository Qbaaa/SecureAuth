package com.qbaaa.secure.auth.controller.auth;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.auth.api.dto.LoginRequest;
import com.qbaaa.secure.auth.auth.api.dto.MfaResponse;
import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.config.otp.MockOtpProvider;
import com.qbaaa.secure.auth.config.otp.MockOtpProviderConfig;
import com.qbaaa.secure.auth.config.time.FakeTimeProvider;
import com.qbaaa.secure.auth.config.time.FakeTimeProviderConfig;
import com.qbaaa.secure.auth.multifactorauth.api.dto.MfaRequest;
import com.qbaaa.secure.auth.repository.RefreshTokenRepositoryTest;
import com.qbaaa.secure.auth.repository.SessionRepositoryTest;
import com.qbaaa.secure.auth.repository.UserRepositoryTest;
import com.qbaaa.secure.auth.shared.exception.RestErrorCodeType;
import com.qbaaa.secure.auth.shared.exception.rest.ErrorDetails;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Slf4j
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {FakeTimeProviderConfig.class, MockOtpProviderConfig.class})
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class LoginMfaTestIT {

  private static final String API_POST_LOGIN = "/domains/{domainName}/auth/token";
  private static final String API_MFA = "/domains/{domainName}/auth/login/mfa";

  @Autowired private FakeTimeProvider fakeTimeProvider;

  @Autowired private MockOtpProvider mockOtpProvider;

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
    mockOtpProvider.setSecretMock("secret123456");
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login_mfa.sql")
  void shouldErrorWhenCorrectPasswordAndWrongMfaSecretAfter2Attempts() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userMfa";
      final var password = "secretUser002";
      final var loginRequest = new LoginRequest(username, password);
      final AtomicReference<String> pendingToken = new AtomicReference<>();

      log.info("--------------------------------------------------");
      log.info("START REQUEST username/password");
      log.info("--------------------------------------------------");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              resultLogin -> {
                Assertions.assertEquals(
                    HttpStatus.OK.value(), resultLogin.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        resultLogin.getResponse().getContentAsByteArray(), MfaResponse.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertNotNull(
                            response.pendingToken(), "Pending token should not be null"),
                    () -> Assertions.assertEquals("MFA_REQUIRED", response.status()));
                pendingToken.set(response.pendingToken());
              });

      log.info("--------------------------------------------------");
      log.info("START REQUEST mfa");
      log.info("--------------------------------------------------");
      final var mfa = new MfaRequest("wrong");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_MFA, domainName)
                  .content(objectMapper.writeValueAsString(mfa))
                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + pendingToken))
          .andExpect(
              resultMfa -> {
                Assertions.assertEquals(
                    HttpStatus.LOCKED.value(), resultMfa.getResponse().getStatus());
                final var error =
                    objectMapper.readValue(
                        resultMfa.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(error);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.ACCOUNT_LOCKED.name(), error.code()));

                log.info("--------------------------------------------------");
                log.info("END REQUEST mfa");
                log.info("--------------------------------------------------");
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
                      Assertions.assertTrue(user.get().getLastFailedLoginTime().isPresent());
                      var opt = user.get().getOtp();
                      Assertions.assertTrue(opt.isEmpty());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login_mfa.sql")
  void shouldReturnAccessTokenErrorWhenCorrectPasswordAndWrongSecretCorrectSecret1Attempts() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userMfaSMS";
      final var password = "secretUser002";
      final var loginRequest = new LoginRequest(username, password);
      final AtomicReference<String> pendingToken = new AtomicReference<>();

      log.info("--------------------------------------------------");
      log.info("START REQUEST username/password");
      log.info("--------------------------------------------------");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              resultLogin -> {
                Assertions.assertEquals(
                    HttpStatus.OK.value(), resultLogin.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        resultLogin.getResponse().getContentAsByteArray(), MfaResponse.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () -> Assertions.assertNotNull(response.pendingToken()),
                    () -> Assertions.assertEquals("MFA_REQUIRED", response.status()));
                pendingToken.set(response.pendingToken());
              });

      mockOtpProvider.setSecretMock("second123456");
      log.info("--------------------------------------------------");
      log.info("START REQUEST mfa");
      log.info("--------------------------------------------------");
      final var mfa = new MfaRequest("wrong");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_MFA, domainName)
                  .content(objectMapper.writeValueAsString(mfa))
                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + pendingToken))
          .andExpect(
              resultMfa -> {
                Assertions.assertEquals(
                    HttpStatus.UNAUTHORIZED.value(), resultMfa.getResponse().getStatus());
                final var error =
                    objectMapper.readValue(
                        resultMfa.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(error);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.CREDENTIALS_INVALIDATION.name(), error.code()));

                assertAll(
                    "CHECK TABLES DATA AFTER LOGIN TO APP",
                    () -> {
                      var user =
                          userRepositoryTest.findByDomainNameAndUsername(domainName, username);
                      Assertions.assertTrue(user.isPresent());
                      Assertions.assertEquals(2, user.get().getFailedLoginAttempts());
                      var opt = user.get().getOtp();
                      Assertions.assertTrue(opt.isPresent());
                    });
              });

      log.info("--------------------------------------------------");
      log.info("START REQUEST 2 mfa");
      log.info("--------------------------------------------------");
      final var mfa2 = new MfaRequest("second123456");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_MFA, domainName)
                  .content(objectMapper.writeValueAsString(mfa2))
                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + pendingToken))
          .andExpect(
              resultMfa -> {
                Assertions.assertEquals(HttpStatus.OK.value(), resultMfa.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        resultMfa.getResponse().getContentAsByteArray(), TokenResponse.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE", () -> Assertions.assertNotNull(response.accessToken()));
                log.info("END REQUEST 2 mfa--------------------------------------------------");

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
                      var opt = user.get().getOtp();
                      Assertions.assertTrue(opt.isEmpty());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login_mfa.sql")
  void shouldReturnAccessTokenWhenCorrectPasswordAndCorrectMfaSecret() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userMfa";
      final var password = "secretUser002";
      final var loginRequest = new LoginRequest(username, password);
      final AtomicReference<String> pendingToken = new AtomicReference<>();

      log.info("--------------------------------------------------");
      log.info("START REQUEST username/password");
      log.info("--------------------------------------------------");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              resultLogin -> {
                Assertions.assertEquals(
                    HttpStatus.OK.value(), resultLogin.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        resultLogin.getResponse().getContentAsByteArray(), MfaResponse.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertNotNull(
                            response.pendingToken(), "Pending token should not be null"),
                    () -> Assertions.assertEquals("MFA_REQUIRED", response.status()));
                pendingToken.set(response.pendingToken());
              });

      log.info("--------------------------------------------------");
      log.info("START REQUEST mfa");
      log.info("--------------------------------------------------");
      final var mfa = new MfaRequest("secret123456");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_MFA, domainName)
                  .content(objectMapper.writeValueAsString(mfa))
                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + pendingToken))
          .andExpect(
              resultMfa -> {
                Assertions.assertEquals(HttpStatus.OK.value(), resultMfa.getResponse().getStatus());
                final var token =
                    objectMapper.readValue(
                        resultMfa.getResponse().getContentAsByteArray(), TokenResponse.class);
                Assertions.assertNotNull(token);
                assertAll(
                    "CHECK API RESPONSE",
                    () -> Assertions.assertNotNull(token.accessToken()),
                    () -> Assertions.assertNotNull(token.refreshToken()));

                log.info("--------------------------------------------------");
                log.info("END REQUEST mfa");
                log.info("--------------------------------------------------");
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
                      var opt = user.get().getOtp();
                      Assertions.assertTrue(opt.isEmpty());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login_mfa.sql")
  void shouldErrorWhenCorrectPasswordAndCorrectMfaSecretButExpired() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userMfa";
      final var password = "secretUser002";
      final var loginRequest = new LoginRequest(username, password);
      final AtomicReference<String> pendingToken = new AtomicReference<>();

      log.info("--------------------------------------------------");
      log.info("START REQUEST username/password");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              resultLogin -> {
                Assertions.assertEquals(
                    HttpStatus.OK.value(), resultLogin.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        resultLogin.getResponse().getContentAsByteArray(), MfaResponse.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertNotNull(
                            response.pendingToken(), "Pending token should not be null"),
                    () -> Assertions.assertEquals("MFA_REQUIRED", response.status()));
                pendingToken.set(response.pendingToken());
              });

      log.info("--------------------------------------------------");
      log.info("START REQUEST mfa");
      final var mfa = new MfaRequest("secret123456");
      LocalDateTime newTime = LocalDateTime.now().plusMinutes(10);
      fakeTimeProvider.setLocalDateTime(newTime);
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_MFA, domainName)
                  .content(objectMapper.writeValueAsString(mfa))
                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + pendingToken))
          .andExpect(
              resultMfa -> {
                Assertions.assertEquals(
                    HttpStatus.LOCKED.value(), resultMfa.getResponse().getStatus());
                final var error =
                    objectMapper.readValue(
                        resultMfa.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(error);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.ACCOUNT_LOCKED.getErrorType(), error.code()));

                log.info("END REQUEST mfa--------------------------------------------------");
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
                      Assertions.assertTrue(user.get().getLastFailedLoginTime().isPresent());
                      var opt = user.get().getOtp();
                      Assertions.assertTrue(opt.isEmpty());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login_mfa.sql")
  void shouldErrorWhenCorrectPasswordAndWrongMfaSecretAfter1Attempts() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userMfaSMS";
      final var password = "secretUser002";
      final var loginRequest = new LoginRequest(username, password);
      final AtomicReference<String> pendingToken = new AtomicReference<>();

      log.info("--------------------------------------------------");
      log.info("START REQUEST username/password");
      log.info("--------------------------------------------------");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              resultLogin -> {
                Assertions.assertEquals(
                    HttpStatus.OK.value(), resultLogin.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        resultLogin.getResponse().getContentAsByteArray(), MfaResponse.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertNotNull(
                            response.pendingToken(), "Pending token should not be null"),
                    () -> Assertions.assertEquals("MFA_REQUIRED", response.status()));
                pendingToken.set(response.pendingToken());
              });
      assertAll(
          "CHECK TABLES DATA AFTER LOGIN TO APP",
          () -> Assertions.assertEquals(0, sessionRepositoryTest.countByUsername(username)),
          () -> Assertions.assertEquals(0, refreshTokenRepositoryTest.countByUsername(username)),
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            Assertions.assertEquals(1, user.get().getFailedLoginAttempts());
            var opt = user.get().getOtp();
            Assertions.assertTrue(opt.isPresent());
            Assertions.assertEquals("secret123456", opt.get().getSecret());
          });

      mockOtpProvider.setSecretMock("newSecret123");
      log.info("--------------------------------------------------");
      log.info("START REQUEST mfa");
      log.info("--------------------------------------------------");
      final var mfa = new MfaRequest("wrong");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_MFA, domainName)
                  .content(objectMapper.writeValueAsString(mfa))
                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + pendingToken))
          .andExpect(
              resultMfa -> {
                Assertions.assertEquals(
                    HttpStatus.UNAUTHORIZED.value(), resultMfa.getResponse().getStatus());
                final var error =
                    objectMapper.readValue(
                        resultMfa.getResponse().getContentAsByteArray(), ErrorDetails.class);
                Assertions.assertNotNull(error);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertEquals(
                            RestErrorCodeType.CREDENTIALS_INVALIDATION.name(), error.code()));

                log.info("--------------------------------------------------");
                log.info("END REQUEST mfa");
                log.info("--------------------------------------------------");
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
                      Assertions.assertEquals(2, user.get().getFailedLoginAttempts());
                      Assertions.assertTrue(user.get().getLastFailedLoginTime().isPresent());
                      var opt = user.get().getOtp();
                      Assertions.assertTrue(opt.isPresent());
                      Assertions.assertEquals("newSecret123", opt.get().getSecret());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_login_mfa.sql")
  void shouldErrorWhenNoBearer() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userMfa";
      final var password = "secretUser002";
      final var loginRequest = new LoginRequest(username, password);

      log.info("--------------------------------------------------");
      log.info("START REQUEST username/password");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_LOGIN, domainName)
                  .content(objectMapper.writeValueAsString(loginRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              resultLogin -> {
                Assertions.assertEquals(
                    HttpStatus.OK.value(), resultLogin.getResponse().getStatus());
                final var response =
                    objectMapper.readValue(
                        resultLogin.getResponse().getContentAsByteArray(), MfaResponse.class);
                Assertions.assertNotNull(response);
                assertAll(
                    "CHECK API RESPONSE",
                    () ->
                        Assertions.assertNotNull(
                            response.pendingToken(), "Pending token should not be null"),
                    () -> Assertions.assertEquals("MFA_REQUIRED", response.status()));
              });

      log.info("--------------------------------------------------");
      log.info("START REQUEST mfa");
      final var mfa = new MfaRequest("123456");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_MFA, domainName)
                  .content(objectMapper.writeValueAsString(mfa))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(
              resultMfa -> {
                Assertions.assertEquals(
                    HttpStatus.FORBIDDEN.value(), resultMfa.getResponse().getStatus());

                log.info("END REQUEST mfa--------------------------------------------------");
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
                      Assertions.assertEquals(2, user.get().getFailedLoginAttempts());
                      Assertions.assertTrue(user.get().getLastFailedLoginTime().isPresent());
                      var opt = user.get().getOtp();
                      Assertions.assertTrue(opt.isPresent());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }
}
