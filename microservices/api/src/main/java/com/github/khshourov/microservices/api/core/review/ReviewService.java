package com.github.khshourov.microservices.api.core.review;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface ReviewService {
  @GetMapping(value = "/reviews", produces = "application/json")
  List<Review> getReviews(@RequestParam(value = "productId") int productId);
}
