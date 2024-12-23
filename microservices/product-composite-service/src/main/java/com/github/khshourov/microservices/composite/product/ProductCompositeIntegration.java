package com.github.khshourov.microservices.composite.product;

import com.github.khshourov.microservices.api.core.product.ProductService;
import com.github.khshourov.microservices.api.core.recommendation.RecommendationService;
import com.github.khshourov.microservices.api.core.review.ReviewService;
import org.springframework.boot.actuate.health.Health;
import reactor.core.publisher.Mono;

public interface ProductCompositeIntegration
    extends ProductService, RecommendationService, ReviewService {
  Mono<Health> getProductHealth();

  Mono<Health> getRecommendationHealth();

  Mono<Health> getReviewHealth();
}
