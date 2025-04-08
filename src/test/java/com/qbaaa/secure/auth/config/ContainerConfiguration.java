package com.qbaaa.secure.auth.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfiguration {

  private static String POSTGRES_IMAGE_NAME = "postgres:15-alpine";

  private static final Network network = Network.newNetwork();

  @ServiceConnection
  private static final PostgreSQLContainer POSTGRES_CONTAINER =
      new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
          .withNetwork(network)
          .withNetworkAliases("postgres")
          .withReuse(false);

  static {
    POSTGRES_CONTAINER.start();
  }
}
