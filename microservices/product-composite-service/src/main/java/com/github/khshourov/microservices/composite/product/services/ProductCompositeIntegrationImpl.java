package com.github.khshourov.microservices.composite.product.services;

import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.event.Event;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import com.github.khshourov.microservices.composite.product.ProductCompositeIntegration;
import com.github.khshourov.microservices.util.http.HttpErrorInfo;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Component
public class ProductCompositeIntegrationImpl implements ProductCompositeIntegration {
  private static final Logger log = LoggerFactory.getLogger(ProductCompositeIntegrationImpl.class);

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final StreamBridge streamBridge;
  private final Scheduler publishEventScheduler;
  private final ServiceUtil serviceUtil;

  private static final String PRODUCT_BASE_URL = "http://product";
  private static final String RECOMMENDATION_BASE_URL = "http://recommendation";
  private static final String REVIEW_BASE_URL = "http://review";

  @Autowired
  public ProductCompositeIntegrationImpl(
      WebClient.Builder webClientBuilder,
      ObjectMapper objectMapper,
      StreamBridge streamBridge,
      @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
      ServiceUtil serviceUtil) {
    this.webClient = webClientBuilder.build();
    this.objectMapper = objectMapper;
    this.streamBridge = streamBridge;
    this.publishEventScheduler = publishEventScheduler;
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Mono<Product> createProduct(Product request) {
    return Mono.fromCallable(
            () -> {
              Event<Integer, Product> event =
                  new Event<>(Event.Type.CREATE, request.productId(), request);
              this.sendEvent("products-out-0", event);
              return request;
            })
        .subscribeOn(this.publishEventScheduler);
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {
    return Mono.fromRunnable(
            () -> {
              this.sendEvent("products-out-0", new Event<>(Event.Type.DELETE, productId, null));
            })
        .subscribeOn(this.publishEventScheduler)
        .then();
  }

  @Override
  @Retry(name = "product")
  @TimeLimiter(name = "product")
  @CircuitBreaker(name = "product", fallbackMethod = "getProductFallback")
  public Mono<Product> getProduct(int productId, int delay, int faultPercent) {
    URI uri =
        UriComponentsBuilder.fromUriString(
                PRODUCT_BASE_URL + "/product/{productId}?delay={delay}&faultPercent={faultPercent}")
            .build(productId, delay, faultPercent);

    return this.webClient
        .get()
        .uri(uri)
        .retrieve()
        .bodyToMono(Product.class)
        .log(log.getName(), FINE)
        .onErrorMap(WebClientResponseException.class, this::handleHttpClientException);
  }

  private Mono<Product> getProductFallback(
      int productId, int delay, int faultPercent, CallNotPermittedException ex) {
    log.warn(
        "Creating a fail-fast fallback product for productId = {}, delay = {}, faultPercent = {} and exception = {} ",
        productId,
        delay,
        faultPercent,
        ex.toString());

    if (productId == 13) {
      String errMsg = "Product Id: " + productId + " not found in fallback cache!";
      log.warn(errMsg);
      throw new NotFoundException(errMsg);
    }

    return Mono.just(
        new Product(
            productId,
            "Fallback product" + productId,
            productId,
            this.serviceUtil.getServiceAddress()));
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation request) {
    return Mono.fromCallable(
            () -> {
              Event<Integer, Recommendation> event =
                  new Event<>(Event.Type.CREATE, request.productId(), request);
              this.sendEvent("recommendations-out-0", event);
              return request;
            })
        .subscribeOn(this.publishEventScheduler);
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {
    return Mono.fromRunnable(
            () -> {
              this.sendEvent(
                  "recommendations-out-0", new Event<>(Event.Type.DELETE, productId, null));
            })
        .subscribeOn(this.publishEventScheduler)
        .then();
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    String url =
        String.format("%s/recommendations?productId=%d", RECOMMENDATION_BASE_URL, productId);

    return this.webClient
        .get()
        .uri(url)
        .retrieve()
        .bodyToFlux(Recommendation.class)
        .log(log.getName(), FINE)
        .onErrorResume(e -> empty());
  }

  @Override
  public Mono<Review> createReview(Review request) {
    return Mono.fromCallable(
            () -> {
              Event<Integer, Review> event =
                  new Event<>(Event.Type.CREATE, request.productId(), request);
              this.sendEvent("reviews-out-0", event);
              return request;
            })
        .subscribeOn(this.publishEventScheduler);
  }

  @Override
  public Mono<Void> deleteReviews(int productId) {
    return Mono.fromRunnable(
            () -> {
              this.sendEvent("reviews-out-0", new Event<>(Event.Type.DELETE, productId, null));
            })
        .subscribeOn(this.publishEventScheduler)
        .then();
  }

  @Override
  public Flux<Review> getReviews(int productId) {
    String url = String.format("%s/reviews?productId=%d", REVIEW_BASE_URL, productId);

    return this.webClient
        .get()
        .uri(url)
        .retrieve()
        .bodyToFlux(Review.class)
        .log(log.getName(), FINE)
        .subscribeOn(this.publishEventScheduler)
        .onErrorResume(e -> empty());
  }

  public Mono<Health> getProductHealth() {
    return getHealth(PRODUCT_BASE_URL);
  }

  public Mono<Health> getRecommendationHealth() {
    return getHealth(RECOMMENDATION_BASE_URL);
  }

  public Mono<Health> getReviewHealth() {
    return getHealth(REVIEW_BASE_URL);
  }

  private Mono<Health> getHealth(String url) {
    url += "/actuator/health";
    log.debug("Will call the Health API on URL: {}", url);
    return webClient
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .map(s -> new Health.Builder().up().build())
        .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
        .log(log.getName(), FINE);
  }

  private <K, D> void sendEvent(String channel, Event<K, D> event) {
    log.debug("Sending {} to channel: {}", event.getEventType(), channel);

    Message<?> message =
        MessageBuilder.withPayload(event).setHeader("partitionKey", event.getKey()).build();
    this.streamBridge.send(channel, message);
  }

  private Throwable handleHttpClientException(Throwable throwable) {
    if (!(throwable instanceof WebClientResponseException exception)) {
      log.warn("Got an unexpected error: {}, will rethrow it", throwable.toString());
      return throwable;
    }

    HttpStatus httpStatus =
        Optional.ofNullable(HttpStatus.resolve(exception.getStatusCode().value()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    switch (httpStatus) {
      case NOT_FOUND -> {
        return new NotFoundException(getErrorMessage(exception));
      }
      case UNPROCESSABLE_ENTITY -> {
        return new InvalidInputException(getErrorMessage(exception));
      }
      default -> {
        log.warn("Got an unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
        log.warn("Error body: {}", exception.getMessage());
        return exception;
      }
    }
  }

  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return this.objectMapper
          .readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class)
          .message();
    } catch (IOException ignored) {
      return ex.getMessage();
    }
  }
}
