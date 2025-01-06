package com.github.khshourov.microservices.core.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@ComponentScan("com.github.khshourov.microservices")
public class ProductServiceApplication {
  public static void main(String[] args) {
    Hooks.enableAutomaticContextPropagation();

    SpringApplication.run(ProductServiceApplication.class, args);
  }
}
