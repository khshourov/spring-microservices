package com.github.khshourov.microservices.core.product.persistence;

import com.github.khshourov.microservices.core.product.testlib.MongoDbTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

@DataMongoTest
class ProductRepositoryTest extends MongoDbTestBase {
  @Autowired ProductRepository repository;

  ProductEntity savedEntity;

  @BeforeEach
  void init() {
    StepVerifier.create(repository.deleteAll()).verifyComplete();

    ProductEntity productEntity = new ProductEntity(1, "Product 1", 1);
    StepVerifier.create(repository.save(productEntity))
        .expectNextMatches(
            dbEntity -> {
              savedEntity = dbEntity;
              return savedEntity.equals(productEntity);
            })
        .verifyComplete();
  }

  @Test
  void findByProductId() {
    StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
        .expectNextMatches(dbEntity -> savedEntity.equals(dbEntity))
        .verifyComplete();
  }
}
