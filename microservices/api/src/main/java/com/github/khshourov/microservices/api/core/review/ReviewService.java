package com.github.khshourov.microservices.api.core.review;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface ReviewService {
  @PostMapping(value = "/review", consumes = "application/json", produces = "application/json")
  Review createReview(@RequestBody Review request);

  @DeleteMapping(value = "/reviews", produces = "application/json")
  void deleteReviews(@RequestParam(value = "productId", required = true) int productId);

  @GetMapping(value = "/reviews", produces = "application/json")
  List<Review> getReviews(@RequestParam(value = "productId") int productId);
}
