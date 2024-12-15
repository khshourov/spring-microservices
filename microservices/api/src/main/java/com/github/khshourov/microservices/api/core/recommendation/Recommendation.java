package com.github.khshourov.microservices.api.core.recommendation;

public record Recommendation(
    int productId,
    int recommendationId,
    String author,
    int rate,
    String content,
    String serviceAddress) {
  public Recommendation updateServiceAddress(String serviceAddress) {
    return new Recommendation(
        this.productId,
        this.recommendationId,
        this.author,
        this.rate,
        this.content,
        serviceAddress);
  }
}
