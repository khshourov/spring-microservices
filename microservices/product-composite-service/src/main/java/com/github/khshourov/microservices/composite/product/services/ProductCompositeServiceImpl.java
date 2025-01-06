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
import com.github.khshourov.microservices.composite.product.services.tracing.ObservationUtil;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
  private static final Logger log = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

  private final SecurityContext nullSecCtx = new SecurityContextImpl();

  private final ServiceUtil serviceUtil;
  private final ProductCompositeIntegration integration;
  private final ObservationUtil observationUtil;

  @Autowired
  public ProductCompositeServiceImpl(
      ServiceUtil serviceUtil,
      ProductCompositeIntegration integration,
      ObservationUtil observationUtil) {
    this.serviceUtil = serviceUtil;
    this.integration = integration;
    this.observationUtil = observationUtil;
  }

  @Override
  public Mono<Void> createProduct(ProductAggregate request) {
    return observationWithProductInfo(request.productId(), () -> createProductInternal(request));
  }

  private Mono<Void> createProductInternal(ProductAggregate request) {
    try {
      log.debug(
          "createCompositeProduct: creating a new composite entity for product-id: {}",
          request.productId());

      List<Mono<?>> monoList = new ArrayList<>();
      monoList.add(this.getLogAuthorizationInfoMono());

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
    return observationWithProductInfo(productId, () -> deleteProductInternal(productId));
  }

  private Mono<Void> deleteProductInternal(int productId) {
    log.debug("deleteCompositeProduct: deleting composite entity for product-id: {}", productId);

    try {
      return Mono.zip(
              r -> "",
              getLogAuthorizationInfoMono(),
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

  @Override
  public Mono<ProductAggregate> getProduct(int productId, int delay, int faultPercent) {
    return observationWithProductInfo(
        productId, () -> getProductInternal(productId, delay, faultPercent));
  }

  @SuppressWarnings({"unchecked"})
  private Mono<ProductAggregate> getProductInternal(int productId, int delay, int faultPercent) {
    return Mono.zip(
            values -> {
              if (values.length != 4) {
                throw new RuntimeException(
                    "Expected 3 values, but got " + values.length + " values.");
              }
              if (values[0] == null) {
                throw new NotFoundException("No product found for productId: " + productId);
              }

              return this.createProductAggregate(
                  (SecurityContext) values[0],
                  (Product) values[1],
                  (List<Recommendation>) values[2],
                  (List<Review>) values[3],
                  serviceUtil.getServiceAddress());
            },
            getSecurityContextMono(),
            this.integration.getProduct(productId, delay, faultPercent),
            this.integration.getRecommendations(productId).collectList(),
            this.integration.getReviews(productId).collectList())
        .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
        .log(log.getName(), Level.FINE);
  }

  private ProductAggregate createProductAggregate(
      SecurityContext securityContext,
      Product product,
      List<Recommendation> recommendations,
      List<Review> reviews,
      String serviceAddress) {
    this.logAuthorizationInfo(securityContext);

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

  private Mono<SecurityContext> getLogAuthorizationInfoMono() {
    return getSecurityContextMono().doOnNext(this::logAuthorizationInfo);
  }

  private Mono<SecurityContext> getSecurityContextMono() {
    return ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSecCtx);
  }

  private void logAuthorizationInfo(SecurityContext sc) {
    if (sc != null
        && sc.getAuthentication() != null
        && sc.getAuthentication() instanceof JwtAuthenticationToken) {
      Jwt jwtToken = ((JwtAuthenticationToken) sc.getAuthentication()).getToken();
      logAuthorizationInfo(jwtToken);
    } else {
      log.warn("No JWT based Authentication supplied, running tests are we?");
    }
  }

  private void logAuthorizationInfo(Jwt jwt) {
    if (jwt == null) {
      log.warn("No JWT supplied");
    } else if (log.isDebugEnabled()) {
      log.debug(
          "Authorization info: Subject: {}, scopes: {}, expires: {}, issuer: {}, audience: {}",
          jwt.getClaims().get("sub"),
          jwt.getClaims().get("scope"),
          jwt.getClaims().get("exp"),
          jwt.getIssuer(),
          jwt.getAudience());
    }
  }

  private <T> T observationWithProductInfo(int productInfo, Supplier<T> supplier) {
    return this.observationUtil.observe(
        "composite observation",
        "product info",
        "productId",
        String.valueOf(productInfo),
        supplier);
  }
}
