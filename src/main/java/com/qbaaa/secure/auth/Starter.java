package com.qbaaa.secure.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qbaaa.secure.auth.config.SecureAuthProperties;
import com.qbaaa.secure.auth.dto.DomainTransferDto;
import com.qbaaa.secure.auth.exception.DomainImportException;
import com.qbaaa.secure.auth.service.DomainImportService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Starter implements ApplicationRunner {

  private final ObjectMapper objectMapper;
  private final SecureAuthProperties secureAuthProperties;
  private final DomainImportService domainImportService;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    try {
      var jsonContent = loadJsonDomain("import/importMainDomain.json");
      var domainDto = changeConfigurationDomain(jsonContent);
      domainImportService.importDomainStartApplication(domainDto);
      log.info("Created Main Domain");
    } catch (DomainImportException e) {
      log.warn("Main Domain interrupted, {}", e.getMessage());
    }
  }

  private String loadJsonDomain(String path) throws IOException {
    var resource = new ClassPathResource(path);
    return new String(Files.readAllBytes(Paths.get(resource.getURI())));
  }

  private DomainTransferDto changeConfigurationDomain(String domainJson)
      throws JsonProcessingException {
    var jsonNode = objectMapper.readTree(domainJson);

    var domainProperties = secureAuthProperties.getDomain();
    Optional.ofNullable(domainProperties)
        .ifPresent(
            domain -> {
              if (jsonNode instanceof ObjectNode objectNode) {
                objectNode.put("name", domain);
              }
            });

    var usersArray = (ArrayNode) jsonNode.get("users");
    if (usersArray != null && !usersArray.isEmpty()) {
      ObjectNode firstUser = (ObjectNode) usersArray.get(0);

      var userProperties = secureAuthProperties.getUser();
      Optional.ofNullable(userProperties)
          .ifPresent(user -> firstUser.put("username", userProperties));

      var emailProperties = secureAuthProperties.getEmail();
      Optional.ofNullable(emailProperties)
          .ifPresent(email -> firstUser.put("email", emailProperties));

      var passwordNode = (ObjectNode) firstUser.get("password");
      if (passwordNode != null) {
        var passwordProperties = secureAuthProperties.getPassword();
        Optional.ofNullable(passwordProperties)
            .ifPresent(password -> passwordNode.put("password", passwordProperties));
      }
    }

    return objectMapper.treeToValue(jsonNode, DomainTransferDto.class);
  }
}
