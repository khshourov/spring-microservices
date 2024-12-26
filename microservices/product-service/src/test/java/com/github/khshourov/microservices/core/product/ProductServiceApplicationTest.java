package com.github.khshourov.microservices.core.product;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;

import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.core.product.persistence.ProductEntity;
import com.github.khshourov.microservices.core.product.persistence.ProductRepository;
import com.github.khshourov.microservices.core.product.testlib.MongoDbTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = {"eureka.client.enabled=false"})
class ProductServiceApplicationTest extends MongoDbTestBase {
  @Autowired private WebTestClient client;
  @Autowired private ProductRepository repository;

  private final ProductEntity existingEntity = new ProductEntity(1, "Product 1", 1);

  @BeforeEach
  void init() {
    repository.deleteAll().block();
  }

  @Test
  void createProductWithUniqueProductId() {
    Product uniqueProduct = new Product(1, "Product 1", 1, "SA");

    client
        .post()
        .uri("/product")
        .body(just(uniqueProduct), Product.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId")
        .isEqualTo(uniqueProduct.productId());

    StepVerifier.create(repository.findByProductId(uniqueProduct.productId()))
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void errorShouldBeThrownForDuplicateProductId() {
    Product duplicateProduct = new Product(existingEntity.getProductId(), "Product 1", 1, "SA");
    repository.save(existingEntity).block();

    client
        .post()
        .uri("/product")
        .body(just(duplicateProduct), Product.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path")
        .isEqualTo("/product")
        .jsonPath("$.message")
        .isEqualTo("Duplicate product-id: " + duplicateProduct.productId());
  }

  @Test
  void sameEntityCanBeDeletedMultipleTimes() {
    repository.save(existingEntity).block();

    client
        .delete()
        .uri("/product/" + existingEntity.getProductId())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk();

    StepVerifier.create(repository.findByProductId(existingEntity.getProductId()))
        .expectNextCount(0)
        .verifyComplete();

    client
        .delete()
        .uri("/product/" + existingEntity.getProductId())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void getProductForValidId() {
    repository.save(existingEntity).block();
    int validProductId = existingEntity.getProductId();

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

    StepVerifier.create(repository.findByProductId(validProductId))
        .expectNextCount(1)
        .verifyComplete();
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
