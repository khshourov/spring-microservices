package com.github.khshourov.microservices.api.core.product;

public record Product(int productId, String name, int weight, String serviceAddress) {
  public Product updateServiceAddress(String serviceAddress) {
    return new Product(this.productId, this.name, this.weight, serviceAddress);
  }
}
