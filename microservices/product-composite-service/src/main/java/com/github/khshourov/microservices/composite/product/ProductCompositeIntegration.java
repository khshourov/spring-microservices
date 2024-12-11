package com.github.khshourov.microservices.composite.product;

import com.github.khshourov.microservices.api.core.product.ProductService;
import com.github.khshourov.microservices.api.core.recommendation.RecommendationService;
import com.github.khshourov.microservices.api.core.review.ReviewService;

public interface ProductCompositeIntegration
    extends ProductService, RecommendationService, ReviewService {}
