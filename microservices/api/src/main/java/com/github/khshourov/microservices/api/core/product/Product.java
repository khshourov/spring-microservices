package com.github.khshourov.microservices.api.core.product;

public class Product {
  private int productId;
  private String name;
  private int weight;
  private String serviceAddress;

  public Product(int productId, String name, int weight, String serviceAddress) {
    this.productId = productId;
    this.name = name;
    this.weight = weight;
    this.serviceAddress = serviceAddress;
  }

  public int getProductId() {
    return productId;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public String getServiceAddress() {
    return serviceAddress;
  }

  public void setServiceAddress(String serviceAddress) {
    this.serviceAddress = serviceAddress;
  }

  @Override
  public String toString() {
    return "Product{"
        + "productId="
        + productId
        + ", name='"
        + name
        + '\''
        + ", weight="
        + weight
        + ", serviceAddress='"
        + serviceAddress
        + '\''
        + '}';
  }
}
