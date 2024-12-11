package com.github.khshourov.microservices.api.composite.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductCompositeService {
  @GetMapping(value = "/composite/product/{productId}", produces = "application/json")
  ProductAggregate getProduct(@PathVariable int productId);
}
