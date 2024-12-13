package com.github.khshourov.microservices.core.product.persistence;

import com.github.khshourov.microservices.api.core.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  @Mapping(target = "serviceAddress", ignore = true)
  Product entityToApi(ProductEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  ProductEntity apiToEntity(Product product);
}
