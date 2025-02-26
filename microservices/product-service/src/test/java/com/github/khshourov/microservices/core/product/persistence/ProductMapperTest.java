package com.github.khshourov.microservices.core.product.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.khshourov.microservices.api.core.product.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductMapperTest {
  private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

  @Test
  void testMappingFromProductToEntity() {
    Product product = new Product(1, "Product 1", 1, "SA");

    ProductEntity productEntity = productMapper.apiToEntity(product);

    assertEquals(product.productId(), productEntity.getProductId());
    assertEquals(product.name(), productEntity.getName());
    assertEquals(product.weight(), productEntity.getWeight());
  }

  @Test
  void testMappingFromEntityToProduct() {
    ProductEntity productEntity = new ProductEntity(1, "Product 1", 1);

    Product product = productMapper.entityToApi(productEntity);

    assertEquals(productEntity.getProductId(), product.productId());
    assertEquals(productEntity.getName(), product.name());
    assertEquals(productEntity.getWeight(), product.weight());
    assertNull(product.serviceAddress());
  }
}
