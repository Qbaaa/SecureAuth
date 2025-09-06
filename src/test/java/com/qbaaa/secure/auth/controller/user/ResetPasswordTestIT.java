package com.qbaaa.secure.auth.controller.user;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.config.otp.MockOtpProvider;
import com.qbaaa.secure.auth.config.otp.MockOtpProviderConfig;
import com.qbaaa.secure.auth.config.time.FakeTimeProvider;
import com.qbaaa.secure.auth.config.time.FakeTimeProviderConfig;
import com.qbaaa.secure.auth.multifactorauth.api.dto.OperationRequest;
import com.qbaaa.secure.auth.multifactorauth.domain.enums.OperationPublicType;
import com.qbaaa.secure.auth.repository.UserRepositoryTest;
import com.qbaaa.secure.auth.user.api.dto.ResetPasswordRequest;
import com.qbaaa.secure.auth.user.infrastructure.entity.PasswordEntity;
import java.time.Instant;
import java.time.LocalDateTime;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class ResetPasswordTestIT {

  private static final String API_POST_CODE = "/domains/{domainName}/auth/code";
  private static final String API_POST_RESET_PASSWORD =
      "/domains/{domainName}/users/reset-password";

  @Autowired private FakeTimeProvider fakeTimeProvider;

  @Autowired private MockOtpProvider mockOtpProvider;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepositoryTest userRepositoryTest;

  @Autowired private PasswordEncoder passwordEncoder;

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
  @Sql(scripts = "classpath:test/db/data/auth/post_reset_password.sql")
  void shouldPositiveResetWhenUserExistsCorrectPasswordCorrectCode() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userResetPassword";
      final var newPassword = "newSecret321";
      final var secretOtpCorrect = "PasswordSecret321";
      mockOtpProvider.setSecretMock("PasswordSecret321");
      final var operationRequest =
          new OperationRequest(OperationPublicType.RESET_PASSWORD, username);
      final var resetPassword =
          new ResetPasswordRequest(username, newPassword, newPassword, secretOtpCorrect);
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_CODE, domainName)
                  .content(objectMapper.writeValueAsString(operationRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(
              result ->
                  Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus()));

      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_RESET_PASSWORD, domainName)
                  .content(objectMapper.writeValueAsString(resetPassword))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result ->
                  Assertions.assertEquals(
                      HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus()));

      assertAll(
          "CHECK TABLES DATA AFTER",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            var opt = user.get().getOtp();
            Assertions.assertTrue(opt.isEmpty());
            PasswordEntity userPassword = user.get().getPassword();
            Assertions.assertTrue(comparePassword(newPassword, userPassword.getPassword()));
            Assertions.assertTrue(
                userPassword.getUpdatedAt().isAfter(LocalDateTime.now().minusMinutes(5)));
          });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_reset_password.sql")
  void shouldErrorWhenUserExistsCorrectPasswordWrongCode() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userResetPassword";
      final var newPassword = "newPassword321";
      mockOtpProvider.setSecretMock("PasswordSecret321");
      final var operationRequest =
          new OperationRequest(OperationPublicType.RESET_PASSWORD, username);
      final var resetPassword =
          new ResetPasswordRequest(username, newPassword, newPassword, "wrong");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_CODE, domainName)
                  .content(objectMapper.writeValueAsString(operationRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
              });

      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_RESET_PASSWORD, domainName)
                  .content(objectMapper.writeValueAsString(resetPassword))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
              });

      assertAll(
          "CHECK TABLES DATA AFTER",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            PasswordEntity userPassword = user.get().getPassword();
            Assertions.assertTrue(comparePassword("secretUser002", userPassword.getPassword()));
            Assertions.assertTrue(
                userPassword.getUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(5)));
            var opt = user.get().getOtp();
            Assertions.assertTrue(opt.isEmpty());
          });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_reset_password.sql")
  void shouldErrorWhenUserExistsPasswordIsCompromised() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userResetPassword";
      final var newPassword = "Password";
      mockOtpProvider.setSecretMock("PasswordSecret321");
      final var operationRequest =
          new OperationRequest(OperationPublicType.RESET_PASSWORD, username);
      final var resetPassword =
          new ResetPasswordRequest(username, newPassword, newPassword, "PasswordSecret321");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_CODE, domainName)
                  .content(objectMapper.writeValueAsString(operationRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
              });

      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_RESET_PASSWORD, domainName)
                  .content(objectMapper.writeValueAsString(resetPassword))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
              });

      assertAll(
          "CHECK TABLES DATA AFTER",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            PasswordEntity userPassword = user.get().getPassword();
            Assertions.assertTrue(comparePassword("secretUser002", userPassword.getPassword()));
            Assertions.assertTrue(
                userPassword.getUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(5)));
            var opt = user.get().getOtp();
            Assertions.assertTrue(opt.isEmpty());
          });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_reset_password.sql")
  void shouldErrorWhenUserNotExists() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userNotExists";
      final var newPassword = "newPassword321";
      mockOtpProvider.setSecretMock("PasswordSecret321");
      final var operationRequest =
          new OperationRequest(OperationPublicType.RESET_PASSWORD, username);
      final var resetPassword =
          new ResetPasswordRequest(username, newPassword, newPassword, "PasswordSecret321");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_CODE, domainName)
                  .content(objectMapper.writeValueAsString(operationRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
              });

      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_RESET_PASSWORD, domainName)
                  .content(objectMapper.writeValueAsString(resetPassword))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  private boolean comparePassword(String inputPassword, String userPasswordEncoded) {
    return passwordEncoder.matches(inputPassword, userPasswordEncoded);
  }
}
