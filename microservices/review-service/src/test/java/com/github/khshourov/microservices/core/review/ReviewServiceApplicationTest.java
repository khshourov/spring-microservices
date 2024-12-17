package com.github.khshourov.microservices.core.review;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;

import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.core.review.persistence.ReviewEntity;
import com.github.khshourov.microservices.core.review.persistence.ReviewRepository;
import com.github.khshourov.microservices.core.review.testlib.MySqlTestBase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTest extends MySqlTestBase {
  @Autowired private WebTestClient client;
  @Autowired private ReviewRepository repository;

  @BeforeEach
  void init() {
    repository.deleteAll();
  }

  @Test
  void reviewShouldBeReturnedWithValidData() {
    Review request = new Review(1, 1, "a1", "s1", "c1", "sa");

    client
        .post()
        .uri("/review")
        .body(just(request), Review.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId")
        .isEqualTo(1)
        .jsonPath("$.reviewId")
        .isEqualTo(1);

    List<ReviewEntity> entities = repository.findByProductId(1);
    assertThat(entities, hasSize(1));

    ReviewEntity entity = entities.getFirst();
    assertEquals(request.productId(), entity.getProductId());
    assertEquals(request.reviewId(), entity.getReviewId());
    assertEquals(request.author(), entity.getAuthor());
    assertEquals(request.subject(), entity.getSubject());
    assertEquals(request.content(), entity.getContent());
  }

  @Test
  void productIdAndReviewIdTogetherMustBeUnique() {
    ReviewEntity entity = new ReviewEntity(1, 1, "a1", "s1", "c1");
    repository.save(entity);

    Review request = new Review(1, 1, "a1", "s1", "c1", "sa");

    client
        .post()
        .uri("/review")
        .body(just(request), Review.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/review")
        .jsonPath("$.message")
        .isEqualTo("Product-Id and Review-Id, together must be unique");
  }

  @Test
  void idempotentDeleteOperationShouldRemoveRecord() {
    ReviewEntity entity = new ReviewEntity(1, 1, "a1", "s1", "c1");
    repository.save(entity);

    int deletableProductId = 1;

    client
        .delete()
        .uri("/reviews?productId=" + deletableProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk();

    List<ReviewEntity> entities = repository.findByProductId(deletableProductId);
    assertTrue(entities.isEmpty());

    client
        .delete()
        .uri("/reviews?productId=" + deletableProductId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void getReviewsForValidProductId() {
    int validProductId = 1;
    repository.saveAll(
        List.of(
            new ReviewEntity(validProductId, 1, "a1", "s1", "c1"),
            new ReviewEntity(validProductId, 2, "a2", "s2", "c2"),
            new ReviewEntity(validProductId, 3, "a3", "s3", "c3"),
            new ReviewEntity(validProductId, 4, "a4", "s4", "c4")));

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
        .isEqualTo(4)
        .jsonPath("$[0].productId")
        .isEqualTo(validProductId)
        .jsonPath("$[0].reviewId")
        .isEqualTo(1)
        .jsonPath("$[1].reviewId")
        .isEqualTo(2)
        .jsonPath("$[2].reviewId")
        .isEqualTo(3)
        .jsonPath("$[3].reviewId")
        .isEqualTo(4);
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
