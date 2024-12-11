package com.github.khshourov.microservices.core.product;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTest {
  @Autowired private WebTestClient client;

  @Test
  void getProductForValidId() {
    int validProductId = 1;

    client
        .get()
        .uri("/product/" + validProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId")
        .isEqualTo(validProductId);
  }

  @Test
  void throwExceptionForWronglyTypedProductId() {
    String wronglyTypedProductId = "product-id";

    client
        .get()
        .uri("/product/" + wronglyTypedProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/product/" + wronglyTypedProductId)
        .jsonPath("$.message")
        .isEqualTo("Type mismatch.");
  }

  @Test
  void productIdShouldBeGreaterThanZero() {
    int invalidProductId = 0;

    client
        .get()
        .uri("/product/" + invalidProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/product/" + invalidProductId)
        .jsonPath("$.message")
        .isEqualTo("Invalid product-id: " + invalidProductId);
  }

  @Test
  void throwExceptionWhenProductNotFound() {
    int notFoundProductId = 13;

    client
        .get()
        .uri("/product/" + notFoundProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/product/" + notFoundProductId)
        .jsonPath("$.message")
        .isEqualTo("No product found for product-id: " + notFoundProductId);
  }
}
