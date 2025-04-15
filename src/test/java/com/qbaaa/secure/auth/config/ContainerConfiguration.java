package com.qbaaa.secure.auth.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfiguration {

  private static String POSTGRES_IMAGE_NAME = "postgres:15-alpine";
  private static String EMAIL_IMAGE_NAME = "axllent/mailpit";

  private static final Network network = Network.newNetwork();

  @ServiceConnection
  private static final PostgreSQLContainer POSTGRES_CONTAINER =
      new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
          .withNetwork(network)
          .withNetworkAliases("postgres")
          .withReuse(false);

  private static final GenericContainer EMAIL_CONTAINER =
      new GenericContainer<>(EMAIL_IMAGE_NAME)
          .withNetwork(network)
          .withExposedPorts(1025, 8025)
          .withReuse(false);

  static {
    POSTGRES_CONTAINER.start();
    EMAIL_CONTAINER.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mail.host", EMAIL_CONTAINER::getHost);
    registry.add("spring.mail.port", () -> EMAIL_CONTAINER.getMappedPort(1025));
  }
}
