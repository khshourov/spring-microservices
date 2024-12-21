package com.github.khshourov.microservices.api.core.review;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {
  @PostMapping(value = "/review", consumes = "application/json", produces = "application/json")
  Mono<Review> createReview(@RequestBody Review request);

  @DeleteMapping(value = "/reviews", produces = "application/json")
  Mono<Void> deleteReviews(@RequestParam(value = "productId", required = true) int productId);

  @GetMapping(value = "/reviews", produces = "application/json")
  Flux<Review> getReviews(@RequestParam(value = "productId") int productId);
}
