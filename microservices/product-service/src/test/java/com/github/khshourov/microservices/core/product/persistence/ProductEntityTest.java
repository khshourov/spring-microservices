package com.github.khshourov.microservices.core.product.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.microservices.core.product.testlib.MongoDbTestBase;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
class ProductEntityTest extends MongoDbTestBase {
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
  void create() {
    ProductEntity newEntity = new ProductEntity(2, "Product 2", 2);
    StepVerifier.create(repository.save(newEntity))
        .expectNextMatches(dbEntity -> newEntity.getProductId() == dbEntity.getProductId())
        .verifyComplete();

    StepVerifier.create(repository.findById(newEntity.getId()))
        .expectNextMatches(newEntity::equals)
        .verifyComplete();
  }

  @Test
  void update() {
    savedEntity.setName("Product 1-prime");
    StepVerifier.create(repository.save(savedEntity))
        .expectNextMatches(dbEntity -> "Product 1-prime".equals(dbEntity.getName()))
        .verifyComplete();

    StepVerifier.create(repository.findById(savedEntity.getId()))
        .expectNextMatches(
            dbEntity -> dbEntity.getVersion() == 1 && "Product 1-prime".equals(dbEntity.getName()))
        .verifyComplete();
  }

  @Test
  void delete() {
    StepVerifier.create(repository.delete(savedEntity)).verifyComplete();

    StepVerifier.create(repository.existsById(savedEntity.getId()))
        .expectNext(false)
        .verifyComplete();
  }

  @Test
  void productIdCanNotBeDuplicate() {
    ProductEntity duplicateEntity = new ProductEntity(savedEntity.getProductId(), "Product 3", 3);

    StepVerifier.create(repository.save(duplicateEntity))
        .expectError(DuplicateKeyException.class)
        .verify();
  }

  @Test
  void updatedEntityNeedsToBeFresh() {
    Optional<ProductEntity> dbEntity1 = repository.findById(savedEntity.getId()).blockOptional();
    Optional<ProductEntity> dbEntity2 = repository.findById(savedEntity.getId()).blockOptional();

    assertTrue(dbEntity1.isPresent());
    assertTrue(dbEntity2.isPresent());

    ProductEntity entity1 = dbEntity1.get();
    ProductEntity entity2 = dbEntity2.get();

    entity1.setName("Product 1-prime");
    repository.save(entity1).block();

    entity2.setName("Product 2-prime");
    StepVerifier.create(repository.save(entity2))
        .expectError(OptimisticLockingFailureException.class)
        .verify();

    StepVerifier.create(repository.findById(savedEntity.getId()))
        .expectNextMatches(
            entity -> entity.getVersion() == 1 && "Product 1-prime".equals(entity.getName()))
        .verifyComplete();
  }
}
