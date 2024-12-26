package com.github.khshourov.microservices.composite.product.services;

import static com.github.khshourov.microservices.composite.product.testlib.IsSameEvent.sameEventWithoutCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import com.github.khshourov.microservices.api.composite.product.ProductAggregate;
import com.github.khshourov.microservices.api.composite.product.RecommendationSummary;
import com.github.khshourov.microservices.api.composite.product.ReviewSummary;
import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.event.Event;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.main.allow-bean-definition-overriding=true",
      "eureka.client.enabled=false"
    })
@Import(TestChannelBinderConfiguration.class)
public class ProductCompositeServiceImplTest {
  private static final Logger log = LoggerFactory.getLogger(ProductCompositeServiceImplTest.class);
  @Autowired private WebTestClient client;
  @Autowired private OutputDestination destination;

  @BeforeEach
  void setUp() {
    purgeMessages("products");
    purgeMessages("recommendations");
    purgeMessages("reviews");
  }

  @Test
  void publishProductCreateEventWithoutRecommendationAndReviewInformation() {
    ProductAggregate aggregate = new ProductAggregate(1, "p1", 1, null, null, null);

    client
        .post()
        .uri("/composite/product")
        .body(just(aggregate), ProductAggregate.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus();

    final List<String> productMessages = getMessages("products");
    final List<String> recommendationMessages = getMessages("recommendations");
    final List<String> reviewMessages = getMessages("reviews");

    assertEquals(1, productMessages.size());
    assertEquals(0, recommendationMessages.size());
    assertEquals(0, reviewMessages.size());

    Event<Integer, Product> expectedEvent =
        new Event<>(
            Event.Type.CREATE,
            aggregate.productId(),
            new Product(aggregate.productId(), aggregate.name(), aggregate.weight(), null));
    assertThat(productMessages.getFirst(), is(sameEventWithoutCreatedAt(expectedEvent)));
  }

  @Test
  void publishProductCreateEventWithRecommendationAndReviewInformation() {
    ProductAggregate aggregate =
        new ProductAggregate(
            1,
            "p1",
            1,
            List.of(new RecommendationSummary(1, "a1", 1, "c1")),
            List.of(new ReviewSummary(1, "a1", "s1", "c1")),
            null);

    client
        .post()
        .uri("/composite/product")
        .body(just(aggregate), ProductAggregate.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus();

    final List<String> productMessages = getMessages("products");
    final List<String> recommendationMessages = getMessages("recommendations");
    final List<String> reviewMessages = getMessages("reviews");

    assertEquals(1, productMessages.size());

    Event<Integer, Product> expectedProductEvent =
        new Event<>(
            Event.Type.CREATE,
            aggregate.productId(),
            new Product(aggregate.productId(), aggregate.name(), aggregate.weight(), null));
    assertThat(productMessages.getFirst(), is(sameEventWithoutCreatedAt(expectedProductEvent)));

    assertEquals(1, recommendationMessages.size());

    RecommendationSummary recommendationSummary = aggregate.recommendations().getFirst();
    Event<Integer, Recommendation> expectedRecommendationEvent =
        new Event<>(
            Event.Type.CREATE,
            aggregate.productId(),
            new Recommendation(
                aggregate.productId(),
                recommendationSummary.recommendationId(),
                recommendationSummary.author(),
                recommendationSummary.rate(),
                recommendationSummary.content(),
                null));
    assertThat(
        recommendationMessages.getFirst(),
        is(sameEventWithoutCreatedAt(expectedRecommendationEvent)));

    assertEquals(1, reviewMessages.size());

    ReviewSummary reviewSummary = aggregate.reviews().getFirst();
    Event<Integer, Review> expectedReviewEvent =
        new Event<>(
            Event.Type.CREATE,
            aggregate.productId(),
            new Review(
                aggregate.productId(),
                reviewSummary.reviewId(),
                reviewSummary.author(),
                reviewSummary.subject(),
                reviewSummary.content(),
                null));
    assertThat(reviewMessages.getFirst(), is(sameEventWithoutCreatedAt(expectedReviewEvent)));
  }

  @Test
  void publishProductDeleteEvent() {
    client
        .delete()
        .uri("/composite/product/" + 1)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isAccepted();

    final List<String> productMessages = getMessages("products");
    final List<String> recommendationMessages = getMessages("recommendations");
    final List<String> reviewMessages = getMessages("reviews");

    assertEquals(1, productMessages.size());

    Event<Integer, Product> expectedProductEvent = new Event<>(Event.Type.DELETE, 1, null);
    assertThat(productMessages.getFirst(), is(sameEventWithoutCreatedAt(expectedProductEvent)));

    assertEquals(1, recommendationMessages.size());

    Event<Integer, Recommendation> expectedRecommendationEvent =
        new Event<>(Event.Type.DELETE, 1, null);
    assertThat(
        recommendationMessages.getFirst(),
        is(sameEventWithoutCreatedAt(expectedRecommendationEvent)));

    assertEquals(1, reviewMessages.size());

    Event<Integer, Review> expectedReviewEvent = new Event<>(Event.Type.DELETE, 1, null);
    assertThat(reviewMessages.getFirst(), is(sameEventWithoutCreatedAt(expectedReviewEvent)));
  }

  private void purgeMessages(String channel) {
    getMessages(channel);
  }

  private List<String> getMessages(String channel) {
    List<String> messages = new ArrayList<>();
    boolean anyMoreMessages = true;

    while (anyMoreMessages) {
      Message<byte[]> message = getMessage(channel);

      if (message == null) {
        anyMoreMessages = false;

      } else {
        messages.add(new String(message.getPayload()));
      }
    }
    return messages;
  }

  private Message<byte[]> getMessage(String bindingName) {
    try {
      return destination.receive(0, bindingName);
    } catch (NullPointerException npe) {
      // If the messageQueues member variable in the target object contains no queues when the
      // receive method is called, it will cause a NPE to be thrown.
      // So we catch the NPE here and return null to indicate that no messages were found.
      log.error("getMessage() received a NPE with binding = {}", bindingName);
      return null;
    }
  }
}
