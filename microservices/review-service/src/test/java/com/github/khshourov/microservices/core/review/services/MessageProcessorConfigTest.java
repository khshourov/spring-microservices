package com.github.khshourov.microservices.core.review.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.event.Event;
import com.github.khshourov.microservices.core.review.persistence.ReviewEntity;
import com.github.khshourov.microservices.core.review.persistence.ReviewRepository;
import com.github.khshourov.microservices.core.review.testlib.MySqlTestBase;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
class MessageProcessorConfigTest extends MySqlTestBase {
  @Autowired
  @Qualifier("messageProcessor") private Consumer<Event<Integer, Review>> eventConsumer;

  @Autowired private ReviewRepository repository;

  @BeforeEach
  void init() {
    repository.deleteAll();
  }

  @Test
  void testCreateEvent() {
    Review review = new Review(1, 1, "a1", "s1", "c1", "sa");

    eventConsumer.accept(new Event<>(Event.Type.CREATE, review.productId(), review));

    List<ReviewEntity> entities = repository.findByProductId(1);
    assertEquals(1, entities.size());
    assertEquals(review.productId(), entities.getFirst().getProductId());
  }

  @Test
  void testDeleteEvent() {
    ReviewEntity entity = new ReviewEntity(1, 1, "a1", "s1", "c1");
    repository.save(entity);

    eventConsumer.accept(new Event<>(Event.Type.DELETE, entity.getProductId(), null));

    List<ReviewEntity> entities = repository.findByProductId(1);
    assertEquals(0, entities.size());
  }
}
