package com.github.khshourov.microservices.api.core.product;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ProductService {
  @PostMapping(value = "/product", consumes = "application/json", produces = "application/json")
  Product createProduct(@RequestBody Product request);

  @DeleteMapping(value = "/product/{productId}", produces = "application/json")
  void deleteProduct(@PathVariable int productId);

  @GetMapping(value = "/product/{productId}", produces = "application/json")
  Product getProduct(@PathVariable int productId);
}
