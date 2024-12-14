package com.github.khshourov.microservices.core.product.testlib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.microservices.core.product.persistence.ProductEntity;

public class Asserts {
  public static void assertProductEntity(ProductEntity expected, ProductEntity actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getVersion(), actual.getVersion());
    assertEquals(expected.getProductId(), actual.getProductId());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getWeight(), actual.getWeight());
  }
}
