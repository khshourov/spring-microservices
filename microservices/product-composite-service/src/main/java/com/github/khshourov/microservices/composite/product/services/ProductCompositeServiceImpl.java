package com.github.khshourov.microservices.composite.product.services;

import com.github.khshourov.microservices.api.composite.product.ProductAggregate;
import com.github.khshourov.microservices.api.composite.product.ProductCompositeService;
import com.github.khshourov.microservices.api.composite.product.RecommendationSummary;
import com.github.khshourov.microservices.api.composite.product.ReviewSummary;
import com.github.khshourov.microservices.api.composite.product.ServiceAddresses;
import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import com.github.khshourov.microservices.composite.product.ProductCompositeIntegration;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
  private final ServiceUtil serviceUtil;
  private final ProductCompositeIntegration integration;

  @Autowired
  public ProductCompositeServiceImpl(
      ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
    this.serviceUtil = serviceUtil;
    this.integration = integration;
  }

  @Override
  public ProductAggregate getProduct(int productId) {
    Product product = integration.getProduct(productId);
    if (product == null) {
      throw new NotFoundException("No product found for productId: " + productId);
    }

    List<Recommendation> recommendations = integration.getRecommendations(productId);
    List<Review> reviews = integration.getReviews(productId);

    return createProductAggregate(
        product, recommendations, reviews, serviceUtil.getServiceAddress());
  }

  private ProductAggregate createProductAggregate(
      Product product,
      List<Recommendation> recommendations,
      List<Review> reviews,
      String serviceAddress) {

    int productId = product.productId();
    String name = product.name();
    int weight = product.weight();

    List<RecommendationSummary> recommendationSummaries =
        Optional.ofNullable(recommendations).orElse(Collections.emptyList()).stream()
            .map(
                recommendation ->
                    new RecommendationSummary(
                        recommendation.recommendationId(),
                        recommendation.author(),
                        recommendation.rate()))
            .toList();

    List<ReviewSummary> reviewSummaries =
        Optional.ofNullable(reviews).orElse(Collections.emptyList()).stream()
            .map(review -> new ReviewSummary(review.reviewId(), review.author(), review.subject()))
            .toList();

    String productAddress = product.serviceAddress();
    String reviewAddress =
        (reviews != null && !reviews.isEmpty()) ? reviews.getFirst().serviceAddress() : "";
    String recommendationAddress =
        (recommendations != null && !recommendations.isEmpty())
            ? recommendations.getFirst().serviceAddress()
            : "";
    ServiceAddresses serviceAddresses =
        new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

    return new ProductAggregate(
        productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
  }
}
