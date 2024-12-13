package com.github.khshourov.microservices.core.product.persistence;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.microservices.core.product.testconfiguration.MongoDbTestBase;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
  void findByProductId() {
    Optional<ProductEntity> dbEntity = repository.findByProductId(savedEntity.getProductId());

    assertTrue(dbEntity.isPresent());
    assertProductEntity(savedEntity, dbEntity.get());
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

  @Test
  void pagination() {
    repository.deleteAll();

    List<ProductEntity> productEntityList =
        rangeClosed(1, 10).mapToObj(i -> new ProductEntity(i, "Product " + i, i)).toList();
    repository.saveAll(productEntityList);

    Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
    nextPage = assertNextPage(nextPage, List.of(1, 2, 3, 4), true);
    nextPage = assertNextPage(nextPage, List.of(5, 6, 7, 8), true);
    assertNextPage(nextPage, List.of(9, 10), false);
  }

  private void assertProductEntity(ProductEntity expected, ProductEntity actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getVersion(), actual.getVersion());
    assertEquals(expected.getProductId(), actual.getProductId());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getWeight(), actual.getWeight());
  }

  private Pageable assertNextPage(
      Pageable page, List<Integer> expectedProductIds, boolean expectedNextPage) {
    Page<ProductEntity> productEntityPage = repository.findAll(page);

    assertIterableEquals(
        expectedProductIds, productEntityPage.stream().map(ProductEntity::getProductId).toList());
    assertEquals(expectedNextPage, productEntityPage.hasNext());

    return productEntityPage.nextPageable();
  }
}
