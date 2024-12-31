package com.github.khshourov.springcloud.gateway;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
class GatewayApplicationTest {
  @Test
  void contextLoads() {
    // Placeholder test so that we can check if application can be loaded
  }
}
