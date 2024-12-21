package com.github.khshourov.microservices.core.recommendation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;

import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationEntity;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationRepository;
import com.github.khshourov.microservices.core.recommendation.testlib.MongoDbTestBase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTest extends MongoDbTestBase {
  @Autowired private WebTestClient client;

  @Autowired private RecommendationRepository repository;

  @BeforeEach
  void init() {
    repository.deleteAll().block();
  }

  @Test
  void createRecommendationEntityWithUniqueCombinationOfProductIdAndRecommendationId() {
    Recommendation recommendation = new Recommendation(1, 1, "a1", 1, "c1", "sa");

    client
        .post()
        .uri("/recommendation")
        .body(just(recommendation), Recommendation.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId")
        .isEqualTo(1)
        .jsonPath("$.recommendationId")
        .isEqualTo(1);
  }

  @Test
  void creationFailedWithDuplicationCombinationOfProductIdAndRecommendationId() {
    repository.save(new RecommendationEntity(1, 1, "a1", 1, "c1")).block();
    Recommendation recommendation = new Recommendation(1, 1, "a1", 1, "c1", "sa");

    client
        .post()
        .uri("/recommendation")
        .body(just(recommendation), Recommendation.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/recommendation")
        .jsonPath("$.message")
        .isEqualTo("Duplicate combination of product-id and recommendation-id: (1, 1)");
  }

  @Test
  void deleteRecommendationsForValidProductId() {
    int validProductId = 1;
    repository
        .saveAll(
            List.of(
                new RecommendationEntity(validProductId, 1, "a1", 1, "c1"),
                new RecommendationEntity(validProductId, 2, "a2", 2, "c2"),
                new RecommendationEntity(validProductId, 3, "a3", 3, "c3"),
                new RecommendationEntity(validProductId, 4, "a4", 4, "c4")))
        .collectList()
        .block();

    client
        .delete()
        .uri("/recommendations?productId=" + validProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk();

    List<RecommendationEntity> entities =
        this.repository.findByProductId(validProductId).collectList().block();
    assertNotNull(entities);
    assertTrue(entities.isEmpty());

    // DELETE should be idempotent
    client
        .delete()
        .uri("/recommendations?productId=" + validProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void getRecommendationsForValidProductId() {
    int validProductId = 1;
    repository
        .saveAll(
            List.of(
                new RecommendationEntity(validProductId, 1, "a1", 1, "c1"),
                new RecommendationEntity(validProductId, 2, "a2", 2, "c2"),
                new RecommendationEntity(validProductId, 3, "a3", 3, "c3"),
                new RecommendationEntity(validProductId, 4, "a4", 4, "c4")))
        .collectList()
        .block();

    client
        .get()
        .uri("/recommendations?productId=" + validProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(4)
        .jsonPath("$[0].productId")
        .isEqualTo(validProductId);
  }

  @Test
  void productIdIsRequired() {
    client
        .get()
        .uri("/recommendations")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/recommendations")
        .jsonPath("$.message")
        .isEqualTo("Required query parameter 'productId' is not present.");
  }

  @Test
  void productIdShouldBeInteger() {
    String wronglyTypedProductId = "product-id";

    client
        .get()
        .uri("/recommendations?productId=" + wronglyTypedProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/recommendations")
        .jsonPath("$.message")
        .isEqualTo("Type mismatch.");
  }

  @Test
  void productIdCanNotBeNegative() {
    int invalidProductId = -1;

    client
        .get()
        .uri("/recommendations?productId=" + invalidProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/recommendations")
        .jsonPath("$.message")
        .isEqualTo("Invalid product-id: " + invalidProductId);
  }

  @Test
  void emptyListWhenProductNotFound() {
    int notFoundProductId = 113;

    client
        .get()
        .uri("/recommendations?productId=" + notFoundProductId)
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
