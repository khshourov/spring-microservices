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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
  private static final Logger log = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final ProductCompositeIntegration integration;

  @Autowired
  public ProductCompositeServiceImpl(
      ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
    this.serviceUtil = serviceUtil;
    this.integration = integration;
  }

  @Override
  public Mono<Void> createProduct(ProductAggregate request) {
    try {
      log.debug(
          "createCompositeProduct: creating a new composite entity for product-id: {}",
          request.productId());

      List<Mono<?>> monoList = new ArrayList<>();

      Product product = new Product(request.productId(), request.name(), request.weight(), null);
      monoList.add(this.integration.createProduct(product));

      Optional.ofNullable(request.recommendations())
          .orElse(Collections.emptyList())
          .forEach(
              recommendationSummary ->
                  monoList.add(
                      this.integration.createRecommendation(
                          new Recommendation(
                              request.productId(),
                              recommendationSummary.recommendationId(),
                              recommendationSummary.author(),
                              recommendationSummary.rate(),
                              recommendationSummary.content(),
                              null))));

      Optional.ofNullable(request.reviews())
          .orElse(Collections.emptyList())
          .forEach(
              reviewSummary ->
                  monoList.add(
                      this.integration.createReview(
                          new Review(
                              request.productId(),
                              reviewSummary.reviewId(),
                              reviewSummary.author(),
                              reviewSummary.subject(),
                              reviewSummary.content(),
                              null))));
      log.debug(
          "createCompositeProduct: created a new composite entity for product-id: {}",
          request.productId());

      return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
          .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
          .then();
    } catch (RuntimeException e) {
      log.warn(
          "createCompositeProduct: Exception when creating composite product: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {
    log.debug("deleteCompositeProduct: deleting composite entity for product-id: {}", productId);

    try {
      return Mono.zip(
              r -> "",
              this.integration.deleteProduct(productId),
              this.integration.deleteReviews(productId),
              this.integration.deleteRecommendations(productId))
          .doOnError(e -> log.warn(e.getMessage()))
          .log(log.getName(), Level.FINE)
          .then();
    } catch (RuntimeException e) {
      log.warn("deleteCompositeProduct failed: {}", e.toString());
    }

    return Mono.empty();
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public Mono<ProductAggregate> getProduct(int productId) {
    return Mono.zip(
            values -> {
              if (values.length != 3) {
                throw new RuntimeException(
                    "Expected 3 values, but got " + values.length + " values.");
              }
              if (values[0] == null) {
                throw new NotFoundException("No product found for productId: " + productId);
              }

              return this.createProductAggregate(
                  (Product) values[0],
                  (List<Recommendation>) values[1],
                  (List<Review>) values[2],
                  serviceUtil.getServiceAddress());
            },
            this.integration.getProduct(productId),
            this.integration.getRecommendations(productId).collectList(),
            this.integration.getReviews(productId).collectList())
        .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
        .log(log.getName(), Level.FINE);
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
                        recommendation.rate(),
                        recommendation.content()))
            .toList();

    List<ReviewSummary> reviewSummaries =
        Optional.ofNullable(reviews).orElse(Collections.emptyList()).stream()
            .map(
                review ->
                    new ReviewSummary(
                        review.reviewId(), review.author(), review.subject(), review.content()))
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
