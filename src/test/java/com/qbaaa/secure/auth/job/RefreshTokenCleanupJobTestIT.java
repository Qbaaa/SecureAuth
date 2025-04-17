package com.qbaaa.secure.auth.job;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.config.time.FakeTimeProvider;
import com.qbaaa.secure.auth.config.time.FakeTimeProviderConfig;
import com.qbaaa.secure.auth.repository.RefreshTokenRepositoryTest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@Slf4j
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = FakeTimeProviderConfig.class)
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class RefreshTokenCleanupJobTestIT {

  @Autowired private RefreshTokenCleanupJob refreshTokenCleanupJob;

  @Autowired private FakeTimeProvider fakeTimeProvider;

  @Autowired private RefreshTokenRepositoryTest refreshTokenRepositoryTest;

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
  @Sql(scripts = "classpath:test/db/data/job/job_delete.sql")
  void shouldDeletedExpiredToken() {
    try {
      // given
      LocalDateTime newTime = LocalDateTime.of(2025, 4, 10, 10, 0, 0);
      fakeTimeProvider.setLocalDateTime(newTime);

      assertAll(
          "CHECK TABLES DATA BEFORE JOB DELETE REFRESH TOKEN",
          () -> Assertions.assertEquals(5, refreshTokenRepositoryTest.count()));

      // when
      refreshTokenCleanupJob.deleteExpiredTokens();

      // then
      assertAll(
          "CHECK TABLES DATA AFTER JOB DELETE REFRESH TOKEN",
          () -> Assertions.assertEquals(2, refreshTokenRepositoryTest.count()));

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }
}
