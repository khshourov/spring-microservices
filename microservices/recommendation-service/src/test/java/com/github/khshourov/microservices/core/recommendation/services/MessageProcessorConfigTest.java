package com.github.khshourov.microservices.core.recommendation.services;

import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.event.Event;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationRepository;
import com.github.khshourov.microservices.core.recommendation.testlib.MongoDbTestBase;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
class MessageProcessorConfigTest extends MongoDbTestBase {
  @Autowired
  @Qualifier("messageProcessor") private Consumer<Event<Integer, Recommendation>> eventConsumer;

  @Autowired private RecommendationRepository repository;

  @Test
  void testCreateEvent() {
    Recommendation recommendation = new Recommendation(1, 1, "a1", 1, "c1", "sa");

    eventConsumer.accept(
        new Event<>(Event.Type.CREATE, recommendation.productId(), recommendation));

    StepVerifier.create(repository.findByProductId(1)).expectNextCount(1).verifyComplete();
  }

  @Test
  void testDeleteEvent() {
    eventConsumer.accept(new Event<>(Event.Type.DELETE, 1, null));

    StepVerifier.create(repository.findByProductId(1)).expectNextCount(0).verifyComplete();
  }
}
