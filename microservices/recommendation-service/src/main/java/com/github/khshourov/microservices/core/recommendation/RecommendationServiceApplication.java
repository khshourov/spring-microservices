package com.github.khshourov.microservices.core.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@ComponentScan("com.github.khshourov.microservices")
public class RecommendationServiceApplication {
  public static void main(String[] args) {
    Hooks.enableAutomaticContextPropagation();

    SpringApplication.run(RecommendationServiceApplication.class, args);
  }
}
