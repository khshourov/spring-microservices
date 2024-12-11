package com.github.khshourov.microservices.api.core.recommendation;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface RecommendationService {
  @GetMapping(value = "/recommendations", produces = "application/json")
  List<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);
}
