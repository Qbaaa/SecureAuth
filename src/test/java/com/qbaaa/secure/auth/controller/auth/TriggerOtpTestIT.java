package com.qbaaa.secure.auth.controller.auth;

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
import com.qbaaa.secure.auth.user.domain.enums.OperationType;
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
class TriggerOtpTestIT {

  private static final String API_POST_CODE = "/domains/{domainName}/auth/code";

  @Autowired private FakeTimeProvider fakeTimeProvider;

  @Autowired private MockOtpProvider mockOtpProvider;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

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
  @Sql(scripts = "classpath:test/db/data/auth/post_code_otp.sql")
  void shouldReturnOKWhenUserNotExists() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userNotExists";
      final var operationRequest =
          new OperationRequest(OperationPublicType.RESET_PASSWORD, username);

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_CODE, domainName)
                  .content(objectMapper.writeValueAsString(operationRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
                Assertions.assertEquals("Send OTP", result.getResponse().getContentAsString());
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_code_otp.sql")
  void shouldTriggeredOtpWhenUserExists() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userOtpSMS";
      final var operationRequest =
          new OperationRequest(OperationPublicType.RESET_PASSWORD, username);

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_CODE, domainName)
                  .content(objectMapper.writeValueAsString(operationRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
                Assertions.assertEquals("Send OTP", result.getResponse().getContentAsString());
              });

      assertAll(
          "CHECK TABLES DATA AFTER",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            var opt = user.get().getOtp();
            Assertions.assertTrue(opt.isPresent());
            Assertions.assertEquals(OperationType.RESET_PASSWORD, opt.get().getOperationType());
          });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_code_otp.sql")
  void shouldDoubleTriggeredOtpWhenUserExists() {
    try {
      // given
      final var domainName = "test-domain";
      final var username = "userOtpEmail";
      final var operationRequest =
          new OperationRequest(OperationPublicType.RESET_PASSWORD, username);

      LocalDateTime newTime = LocalDateTime.of(2025, 2, 10, 10, 10);
      fakeTimeProvider.setLocalDateTime(newTime);
      mockOtpProvider.setSecretMock("firstSecret-321");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_CODE, domainName)
                  .content(objectMapper.writeValueAsString(operationRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
                Assertions.assertEquals("Send OTP", result.getResponse().getContentAsString());
              });

      assertAll(
          "CHECK TABLES DATA AFTER",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            var opt = user.get().getOtp();
            Assertions.assertTrue(opt.isPresent());
            Assertions.assertEquals(OperationType.RESET_PASSWORD, opt.get().getOperationType());
            Assertions.assertEquals("firstSecret-321", opt.get().getSecret());
            Assertions.assertTrue(
                opt.get().getCreatedAt().isBefore(LocalDateTime.of(2025, 2, 11, 10, 10)));
          });

      LocalDateTime newTimeRequest2 = LocalDateTime.of(2020, 6, 21, 10, 10);
      fakeTimeProvider.setLocalDateTime(newTimeRequest2);
      mockOtpProvider.setSecretMock("secondSecret-123");
      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_CODE, domainName)
                  .content(objectMapper.writeValueAsString(operationRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
                Assertions.assertEquals("Send OTP", result.getResponse().getContentAsString());
              });

      assertAll(
          "CHECK TABLES DATA AFTER",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            var opt = user.get().getOtp();
            Assertions.assertTrue(opt.isPresent());
            Assertions.assertEquals(OperationType.RESET_PASSWORD, opt.get().getOperationType());
            Assertions.assertEquals("secondSecret-123", opt.get().getSecret());
            Assertions.assertTrue(
                opt.get().getCreatedAt().isBefore(LocalDateTime.of(2020, 6, 22, 10, 10)));
          });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }
}
