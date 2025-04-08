package com.qbaaa.secure.auth.controller.auth;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.dto.RegisterRequest;
import com.qbaaa.secure.auth.repository.UserRepositoryTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class RegisterUserTestIT {

  private static final String API_POST_REGISTER = "/domains/{domainName}/auth/register";

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

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_register.sql")
  void shouldRegisterUser() {
    try {
      // given
      final var domainName = "test-domain";
      var username = "newUser007";
      var registerRequest = new RegisterRequest(username, "newUser@test.com", "secret007");

      assertAll(
          "CHECK TABLES DATA BEFORE REGISTER",
          () -> Assertions.assertEquals(1, userRepositoryTest.countByDomainName(domainName)));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_REGISTER, domainName)
                  .content(objectMapper.writeValueAsString(registerRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))

          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

                assertAll(
                    "CHECK TABLES DATA AFTER REGISTER",
                    () ->
                        Assertions.assertEquals(
                            2, userRepositoryTest.countByDomainName(domainName)),
                    () -> {
                      var addingUser =
                          userRepositoryTest.findByDomainNameAndUsername(domainName, username);
                      Assertions.assertTrue(addingUser.isPresent());
                      Assertions.assertTrue(addingUser.get().getIsActive());
                      Assertions.assertEquals(2, addingUser.get().getRoles().size());
                      Assertions.assertNotNull(addingUser.get().getPassword());
                    });
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_register.sql")
  void shouldReturnErrorWhenRegisterDomainIsDisable() {
    try {
      // given
      final var domainName = "master";
      var username = "newUser007";
      var registerRequest = new RegisterRequest(username, "newUser001@test.com", "secret007");

      assertAll(
          "CHECK TABLES DATA BEFORE REGISTER",
          () -> Assertions.assertEquals(0, userRepositoryTest.countByDomainName(domainName)));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_REGISTER, domainName)
                  .content(objectMapper.writeValueAsString(registerRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))

          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());

                assertAll(
                    "CHECK TABLES DATA AFTER REGISTER",
                    () ->
                        Assertions.assertEquals(
                            0, userRepositoryTest.countByDomainName(domainName)));
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_register.sql")
  void shouldReturnErrorWhenUsernameExistsInDomain() {
    try {
      // given
      final var domainName = "test-domain";
      var username = "user001";
      var registerRequest = new RegisterRequest(username, "user@test.com", "secret007");

      assertAll(
          "CHECK TABLES DATA BEFORE REGISTER",
          () -> Assertions.assertEquals(1, userRepositoryTest.countByDomainName(domainName)));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_REGISTER, domainName)
                  .content(objectMapper.writeValueAsString(registerRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))

          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.CONFLICT.value(), result.getResponse().getStatus());

                assertAll(
                    "CHECK TABLES DATA AFTER REGISTER",
                    () ->
                        Assertions.assertEquals(
                            1, userRepositoryTest.countByDomainName(domainName)));
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  @Sql(scripts = "classpath:test/db/clean_all_data.sql")
  @Sql(scripts = "classpath:test/db/data/auth/post_register.sql")
  void shouldReturnErrorWhenEmailExistsInDomain() {
    try {
      // given
      final var domainName = "test-domain";
      var username = "newUser007";
      var registerRequest = new RegisterRequest(username, "user001@test.com", "secret007");

      assertAll(
          "CHECK TABLES DATA BEFORE REGISTER",
          () -> Assertions.assertEquals(1, userRepositoryTest.countByDomainName(domainName)));

      // when
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(API_POST_REGISTER, domainName)
                  .content(objectMapper.writeValueAsString(registerRequest))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))

          // then
          .andExpect(
              result -> {
                Assertions.assertEquals(
                    HttpStatus.CONFLICT.value(), result.getResponse().getStatus());

                assertAll(
                    "CHECK TABLES DATA AFTER REGISTER",
                    () ->
                        Assertions.assertEquals(
                            1, userRepositoryTest.countByDomainName(domainName)));
              });

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }
}
