package com.github.khshourov.microservices.api.core.recommendation;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface RecommendationService {
  @PostMapping(
      value = "/recommendation",
      consumes = "application/json",
      produces = "application/json")
  Recommendation createRecommendation(@RequestBody Recommendation request);

  @DeleteMapping(value = "/recommendations", produces = "application/json")
  void deleteRecommendations(@RequestParam(value = "productId", required = true) int productId);

  @GetMapping(value = "/recommendations", produces = "application/json")
  List<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);
}
