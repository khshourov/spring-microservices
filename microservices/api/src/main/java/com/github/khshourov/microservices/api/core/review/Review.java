package com.github.khshourov.microservices.api.core.review;

public record Review(
    int productId,
    int reviewId,
    String author,
    String subject,
    String content,
    String serviceAddress) {
  public Review updateServiceAddress(String serviceAddress) {
    return new Review(
        this.productId, this.reviewId, this.author, this.subject, this.content, serviceAddress);
  }
}
