package com.github.khshourov.microservices.composite.product;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import com.github.khshourov.microservices.api.composite.product.ProductAggregate;
import com.github.khshourov.microservices.api.composite.product.RecommendationSummary;
import com.github.khshourov.microservices.api.composite.product.ReviewSummary;
import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTest {
  private static final int PRODUCT_ID_OK = 1;
  private static final int PRODUCT_ID_NOT_FOUND = 2;
  private static final int PRODUCT_ID_INVALID = 3;

  @Autowired private WebTestClient client;
  @MockitoBean private ProductCompositeIntegration compositeIntegration;

  @BeforeEach
  void setUp() {
    when(compositeIntegration.getProduct(PRODUCT_ID_OK))
        .thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));
    when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
        .thenReturn(
            Flux.fromIterable(
                singletonList(
                    new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address"))));
    when(compositeIntegration.getReviews(PRODUCT_ID_OK))
        .thenReturn(
            Flux.fromIterable(
                singletonList(
                    new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));

    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
        .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

    when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
        .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
  }

  @Test
  void createProductWithoutRecommendationAndReviewInformation() {
    ProductAggregate aggregate = new ProductAggregate(1, "p1", 1, null, null, null);

    client
        .post()
        .uri("/composite/product")
        .body(just(aggregate), ProductAggregate.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus();
  }

  @Test
  void createProductWithRecommendationsAndReviews() {
    ProductAggregate aggregate =
        new ProductAggregate(
            1,
            "p1",
            1,
            List.of(new RecommendationSummary(1, "a1", 1, "c1")),
            List.of(new ReviewSummary(1, "a1", "s1", "c1")),
            null);

    client
        .post()
        .uri("/composite/product")
        .body(just(aggregate), ProductAggregate.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus();
  }

  @Test
  void getProductById() {
    client
        .get()
        .uri("/composite/product/" + PRODUCT_ID_OK)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId")
        .isEqualTo(PRODUCT_ID_OK)
        .jsonPath("$.recommendations.length()")
        .isEqualTo(1)
        .jsonPath("$.reviews.length()")
        .isEqualTo(1);
  }

  @Test
  void getProductNotFound() {
    client
        .get()
        .uri("/composite/product/" + PRODUCT_ID_NOT_FOUND)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/composite/product/" + PRODUCT_ID_NOT_FOUND)
        .jsonPath("$.message")
        .isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
  }

  @Test
  void getProductInvalidInput() {
    client
        .get()
        .uri("/composite/product/" + PRODUCT_ID_INVALID)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(UNPROCESSABLE_ENTITY)
        .expectHeader()
        .contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/composite/product/" + PRODUCT_ID_INVALID)
        .jsonPath("$.message")
        .isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
  }
}
