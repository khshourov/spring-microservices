package com.github.khshourov.microservices.composite.product.services;

import static org.springframework.http.HttpMethod.GET;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import com.github.khshourov.microservices.composite.product.ProductCompositeIntegration;
import com.github.khshourov.microservices.util.http.HttpErrorInfo;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductCompositeIntegrationImpl implements ProductCompositeIntegration {
  private static final Logger log = LoggerFactory.getLogger(ProductCompositeIntegrationImpl.class);

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  private final String productServiceHost;
  private final int productServicePort;

  private final String recommendationServiceHost;
  private final int recommendationServicePort;

  private final String reviewServiceHost;
  private final int reviewServicePort;

  @Autowired
  public ProductCompositeIntegrationImpl(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;

    this.productServiceHost = productServiceHost;
    this.productServicePort = productServicePort;

    this.recommendationServiceHost = recommendationServiceHost;
    this.recommendationServicePort = recommendationServicePort;

    this.reviewServiceHost = reviewServiceHost;
    this.reviewServicePort = reviewServicePort;
  }

  @Override
  public Product getProduct(int productId) {
    try {
      String url =
          String.format(
              "http://%s:%d/product/%d",
              this.productServiceHost, this.productServicePort, productId);
      log.debug("Calling Product api: {}", url);

      Product product = this.restTemplate.getForObject(url, Product.class);

      assert product != null;
      log.debug("Found a product with id: {}", product.productId());

      return product;
    } catch (HttpClientErrorException exception) {
      switch (HttpStatus.resolve(exception.getStatusCode().value())) {
        case NOT_FOUND -> throw new NotFoundException(getErrorMessage(exception));
        case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(exception));
        case null -> {}
        default -> {
          log.warn("Got an unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
          log.warn("Error body: {}", exception.getResponseBodyAsString());
          throw exception;
        }
      }
    }

    return null;
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {
    try {
      String url =
          String.format(
              "http://%s:%d/recommendations?productId=%d",
              this.recommendationServiceHost, this.recommendationServicePort, productId);
      log.debug("Calling Recommendation api: {}", url);

      List<Recommendation> recommendations =
          this.restTemplate
              .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
              .getBody();

      assert recommendations != null;
      log.debug(
          "Found {} recommendations for a product with id: {}", recommendations.size(), productId);

      return recommendations;
    } catch (Exception exception) {
      log.warn(
          "Got an exception while requesting recommendations, return zero recommendations: {}",
          exception.getMessage());
      return List.of();
    }
  }

  @Override
  public List<Review> getReviews(int productId) {
    try {
      String url =
          String.format(
              "http://%s:%d/reviews?productId=%d",
              this.reviewServiceHost, this.reviewServicePort, productId);
      log.debug("Calling Review api: {}", url);

      List<Review> reviews =
          this.restTemplate
              .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {})
              .getBody();

      assert reviews != null;
      log.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);

      return reviews;
    } catch (Exception exception) {
      log.warn(
          "Got an exception while requesting reviews, return zero reviews: {}",
          exception.getMessage());
      return List.of();
    }
  }

  private String getErrorMessage(HttpClientErrorException ex) {
    try {
      return this.objectMapper
          .readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class)
          .message();
    } catch (IOException ignored) {
      return ex.getMessage();
    }
  }
}
