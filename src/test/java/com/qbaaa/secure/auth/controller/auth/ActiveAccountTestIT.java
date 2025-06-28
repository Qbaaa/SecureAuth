package com.qbaaa.secure.auth.controller.auth;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.config.time.FakeTimeProvider;
import com.qbaaa.secure.auth.config.time.FakeTimeProviderConfig;
import com.qbaaa.secure.auth.entity.EmailVerificationTokenEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.repository.EmailTokenRepositoryTest;
import com.qbaaa.secure.auth.repository.UserRepositoryTest;
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
    classes = FakeTimeProviderConfig.class)
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class ActiveAccountTestIT {

  private static final String API_GET_ACTIVE_ACCOUNT =
      "/domains/{domainName}/auth/account/activate";

  static final String TOKEN_PARAM_NAME = "token";

  @Autowired private MockMvc mockMvc;

  @Autowired private FakeTimeProvider fakeTimeProvider;

  @Autowired private JwtService jwtService;

  @Autowired private UserRepositoryTest userRepositoryTest;

  @Autowired private EmailTokenRepositoryTest emailTokenRepositoryTest;

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
  @Sql(scripts = "classpath:test/db/data/auth/post_active_account.sql")
  void shouldActiveAccount() {
    try {
      // given
      final var domainName = "test-domain-001";
      var username = "user001";
      var emailTokenValidity = 300;
      var token =
          jwtService.createActiveAccountToken(
              "http://localhost", domainName, emailTokenValidity, username);

      assertAll(
          "CHECK TABLES DATA BEFORE ACTIVE ACCOUNT",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            Assertions.assertFalse(user.get().getIsActive());
            saveEmailToken(user.get(), emailTokenValidity, token);
          },
          () -> Assertions.assertEquals(1, emailTokenRepositoryTest.count()));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.get(API_GET_ACTIVE_ACCOUNT, domainName)
                  .param(TOKEN_PARAM_NAME, token)
                  .contentType(MediaType.APPLICATION_JSON_VALUE))

          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

                assertAll(
                    "CHECK TABLES DATA AFTER ACTIVE ACCOUNT",
                    () -> Assertions.assertEquals(0, emailTokenRepositoryTest.count()),
                    () -> {
                      var user =
                          userRepositoryTest.findByDomainNameAndUsername(domainName, username);
                      Assertions.assertTrue(user.isPresent());
                      Assertions.assertTrue(user.get().getIsActive());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_active_account.sql")
  void shouldActiveAccountWhenEmailTokenNotFound() {
    try {
      // given
      final var domainName = "test-domain-001";
      var username = "user001";
      var emailTokenValidity = 300;
      var token =
          jwtService.createActiveAccountToken(
              "http://localhost", domainName, emailTokenValidity, username);

      assertAll(
          "CHECK TABLES DATA BEFORE ACTIVE ACCOUNT",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            Assertions.assertFalse(user.get().getIsActive());
          },
          () -> Assertions.assertEquals(0, emailTokenRepositoryTest.count()));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.get(API_GET_ACTIVE_ACCOUNT, domainName)
                  .param(TOKEN_PARAM_NAME, token)
                  .contentType(MediaType.APPLICATION_JSON_VALUE))

          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());

                assertAll(
                    "CHECK TABLES DATA AFTER ACTIVE ACCOUNT",
                    () -> Assertions.assertEquals(0, emailTokenRepositoryTest.count()),
                    () -> {
                      var user =
                          userRepositoryTest.findByDomainNameAndUsername(domainName, username);
                      Assertions.assertTrue(user.isPresent());
                      Assertions.assertFalse(user.get().getIsActive());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_active_account.sql")
  void shouldActiveAccountWhenEmailTokenExpired() {
    try {
      // given
      final var domainName = "test-domain-001";
      var username = "user001";
      var emailTokenValidity = 300;
      Instant newTime = Instant.parse("2025-04-10T10:00:00Z");
      fakeTimeProvider.setInstance(newTime);
      var token =
          jwtService.createActiveAccountToken(
              "http://localhost", domainName, emailTokenValidity, username);

      assertAll(
          "CHECK TABLES DATA BEFORE ACTIVE ACCOUNT",
          () -> {
            var user = userRepositoryTest.findByDomainNameAndUsername(domainName, username);
            Assertions.assertTrue(user.isPresent());
            Assertions.assertFalse(user.get().getIsActive());
            saveEmailToken(user.get(), emailTokenValidity, token);
          },
          () -> Assertions.assertEquals(1, emailTokenRepositoryTest.count()));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.get(API_GET_ACTIVE_ACCOUNT, domainName)
                  .param(TOKEN_PARAM_NAME, token)
                  .contentType(MediaType.APPLICATION_JSON_VALUE))

          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());

                assertAll(
                    "CHECK TABLES DATA AFTER ACTIVE ACCOUNT",
                    () -> Assertions.assertEquals(1, emailTokenRepositoryTest.count()),
                    () -> {
                      var user =
                          userRepositoryTest.findByDomainNameAndUsername(domainName, username);
                      Assertions.assertTrue(user.isPresent());
                      Assertions.assertFalse(user.get().getIsActive());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  private void saveEmailToken(UserEntity user, Integer emailTokenValidity, String token) {
    var dateTime = fakeTimeProvider.getLocalDateTimeNow();
    var emailToken = new EmailVerificationTokenEntity();
    emailToken.setToken(token);
    emailToken.setUser(user);
    emailToken.setCreatedAt(dateTime);
    emailToken.setExpiresAt(dateTime.plusSeconds(emailTokenValidity));
    emailTokenRepositoryTest.save(emailToken);
  }
}
