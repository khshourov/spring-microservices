package com.github.khshourov.microservices.api.core.recommendation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {
  @PostMapping(
      value = "/recommendation",
      consumes = "application/json",
      produces = "application/json")
  Mono<Recommendation> createRecommendation(@RequestBody Recommendation request);

  @DeleteMapping(value = "/recommendations", produces = "application/json")
  Mono<Void> deleteRecommendations(
      @RequestParam(value = "productId", required = true) int productId);

  @GetMapping(value = "/recommendations", produces = "application/json")
  Flux<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);
}
