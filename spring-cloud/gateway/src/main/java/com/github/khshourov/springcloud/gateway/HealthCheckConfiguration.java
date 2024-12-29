package com.github.khshourov.springcloud.gateway;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class HealthCheckConfiguration {
  private static final Logger log = LoggerFactory.getLogger(HealthCheckConfiguration.class);

  private final WebClient webClient;

  @Autowired
  public HealthCheckConfiguration(WebClient.Builder builder) {
    this.webClient = builder.build();
  }

  @Bean
  public ReactiveHealthContributor healthCheckMicroservices() {
    Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

    registry.put("product", () -> getHealth("http://product"));
    registry.put("recommendation", () -> getHealth("http://recommendation"));
    registry.put("review", () -> getHealth("http://review"));
    registry.put("product-composite", () -> getHealth("http://product-composite"));
    registry.put("auth-server", () -> getHealth("http://auth-server"));

    return CompositeReactiveHealthContributor.fromMap(registry);
  }

  private Mono<Health> getHealth(String baseUrl) {
    String url = baseUrl + "/actuator/health";

    return this.webClient
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .map(s -> new Health.Builder().up().build())
        .onErrorResume(exception -> Mono.just(new Health.Builder().down().build()))
        .log(log.getName(), Level.FINE);
  }
}
