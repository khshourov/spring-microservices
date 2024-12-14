package com.github.khshourov.microservices.core.product.persistence;

import static com.github.khshourov.microservices.core.product.testlib.Asserts.assertProductEntity;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.microservices.core.product.testlib.MongoDbTestBase;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataMongoTest
class ProductRepositoryTest extends MongoDbTestBase {
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
  void findByProductId() {
    Optional<ProductEntity> dbEntity = repository.findByProductId(savedEntity.getProductId());

    assertTrue(dbEntity.isPresent());
    assertProductEntity(savedEntity, dbEntity.get());
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

  private Pageable assertNextPage(
      Pageable page, List<Integer> expectedProductIds, boolean expectedNextPage) {
    Page<ProductEntity> productEntityPage = repository.findAll(page);

    assertIterableEquals(
        expectedProductIds, productEntityPage.stream().map(ProductEntity::getProductId).toList());
    assertEquals(expectedNextPage, productEntityPage.hasNext());

    return productEntityPage.nextPageable();
  }
}
