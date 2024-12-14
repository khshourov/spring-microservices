package com.github.khshourov.microservices.core.product.persistence;

import static com.github.khshourov.microservices.core.product.testlib.Asserts.assertProductEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.microservices.core.product.testlib.MongoDbTestBase;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

@DataMongoTest
class ProductEntityTest extends MongoDbTestBase {
  @Autowired ProductRepository repository;

  ProductEntity savedEntity;

  @BeforeEach
  void init() {
    repository.deleteAll();

    ProductEntity productEntity = new ProductEntity(1, "Product 1", 1);
    savedEntity = repository.save(productEntity);

    assertProductEntity(productEntity, savedEntity);
  }

  @Test
  void create() {
    ProductEntity newEntity = new ProductEntity(2, "Product 2", 2);
    repository.save(newEntity);

    Optional<ProductEntity> dbEntity = repository.findById(newEntity.getId());

    assertTrue(dbEntity.isPresent());
    assertProductEntity(newEntity, dbEntity.get());
  }

  @Test
  void update() {
    savedEntity.setName("Product 1-prime");
    repository.save(savedEntity);

    Optional<ProductEntity> dbEntity = repository.findById(savedEntity.getId());

    assertTrue(dbEntity.isPresent());
    assertEquals(1, dbEntity.get().getVersion());
    assertEquals("Product 1-prime", dbEntity.get().getName());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);

    assertFalse(repository.findById(savedEntity.getId()).isPresent());
  }

  @Test
  void productIdCanNotBeDuplicate() {
    ProductEntity duplicateEntity = new ProductEntity(savedEntity.getProductId(), "Product 3", 3);

    assertThrows(DuplicateKeyException.class, () -> repository.save(duplicateEntity));
  }

  @Test
  void updatedEntityNeedsToBeFresh() {
    Optional<ProductEntity> dbEntity1 = repository.findById(savedEntity.getId());
    Optional<ProductEntity> dbEntity2 = repository.findById(savedEntity.getId());

    assertTrue(dbEntity1.isPresent());
    assertTrue(dbEntity2.isPresent());

    ProductEntity entity1 = dbEntity1.get();
    ProductEntity entity2 = dbEntity2.get();

    entity1.setName("Product 1-prime");
    repository.save(entity1);

    entity2.setName("Product 2-prime");
    assertThrows(OptimisticLockingFailureException.class, () -> repository.save(entity2));

    Optional<ProductEntity> entity = repository.findById(savedEntity.getId());
    assertTrue(entity.isPresent());
    assertEquals(1, entity.get().getVersion());
    assertEquals("Product 1-prime", entity.get().getName());
  }
}
