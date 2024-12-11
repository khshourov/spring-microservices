package com.github.khshourov.microservices.core.review;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTest {
  @Autowired private WebTestClient client;

  @Test
  void getReviewsForValidProductId() {
    int validProductId = 1;

    client
        .get()
        .uri("/reviews?productId=" + validProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(3)
        .jsonPath("$[0].productId")
        .isEqualTo(validProductId);
  }

  @Test
  void productIdIsRequired() {
    client
        .get()
        .uri("/reviews")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/reviews")
        .jsonPath("$.message")
        .isEqualTo("Required query parameter 'productId' is not present.");
  }

  @Test
  void productIdShouldBeInteger() {
    String wronglyTypedProductId = "product-id";

    client
        .get()
        .uri("/reviews?productId=" + wronglyTypedProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/reviews")
        .jsonPath("$.message")
        .isEqualTo("Type mismatch.");
  }

  @Test
  void productIdCanNotBeNegative() {
    int invalidProductId = -1;

    client
        .get()
        .uri("/reviews?productId=" + invalidProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/reviews")
        .jsonPath("$.message")
        .isEqualTo("Invalid product-id: " + invalidProductId);
  }

  @Test
  void emptyListWhenProductNotFound() {
    int notFoundProductId = 213;

    client
        .get()
        .uri("/reviews?productId=" + notFoundProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(0);
  }
}
