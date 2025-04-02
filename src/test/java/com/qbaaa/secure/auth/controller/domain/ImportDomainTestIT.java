package com.qbaaa.secure.auth.controller.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.config.ContainerConfiguration;
import com.qbaaa.secure.auth.dto.LoginRequest;
import com.qbaaa.secure.auth.repository.DomainRepositoryTest;
import com.qbaaa.secure.auth.repository.RoleRepositoryTest;
import com.qbaaa.secure.auth.repository.UserRepositoryTest;
import com.qbaaa.secure.auth.service.strategy.LoginStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfiguration.class)
@ActiveProfiles("test")
class ImportDomainTestIT {

    private static final String API_POST_IMPORT_DOMAIN = "/admin/domains";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoginStrategyService loginStrategyService;

    @Autowired
    private DomainRepositoryTest domainRepositoryTest;

    @Autowired
    private RoleRepositoryTest roleRepositoryTest;

    @Autowired
    private UserRepositoryTest userRepositoryTest;

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
    @Sql(scripts = "classpath:test/db/data/domain/post_import_domain.sql")
    void shouldImportedDomain() {
        try {
            // given
            var addingDomain = "ProjectTest";

            assertAll(
                    "CHECK TABLES DATA BEFORE IMPORT DOMAIN",
                    () -> Assertions.assertEquals(2, domainRepositoryTest.count()),
                    () -> Assertions.assertEquals(0, roleRepositoryTest.countByDomainName(addingDomain)),
                    () -> Assertions.assertEquals(0, userRepositoryTest.countByDomainName(addingDomain))
            );

            var loginRequest = new LoginRequest("user001","secretUser001");
            var token = loginStrategyService.authenticate("master", "http://localhost", loginRequest);

            var file = new File(getClass().getClassLoader().getResource("upload/importDomainTest.json").getFile());
            MockMultipartFile fileUpload = new MockMultipartFile(
                    "file",
                    "importDomainTest.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    new FileInputStream(file))
                    ;

            // when
            mockMvc.perform(MockMvcRequestBuilders.multipart(API_POST_IMPORT_DOMAIN)
                    .file(fileUpload)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken()))

                    //then
                    .andExpect(status().isOk())
            ;

            assertAll(
                    "CHECK TABLES DATA AFTER IMPORT DOMAIN",
                    () -> Assertions.assertEquals(3, domainRepositoryTest.count()),
                    () -> Assertions.assertEquals(2, roleRepositoryTest.countByDomainName(addingDomain)),
                    () -> Assertions.assertEquals(2, userRepositoryTest.countByDomainName(addingDomain)),
                    () -> {
                        var addingUser = userRepositoryTest.findByDomainNameAndUsername(addingDomain, "user001");
                        Assertions.assertTrue(addingUser.isPresent());
                        Assertions.assertEquals(1, addingUser.get().getRoles().size());
                    }
            );


        } catch (Exception e) {
            Assertions.fail("ERROR: " + e.getMessage());
        }

    }

}
