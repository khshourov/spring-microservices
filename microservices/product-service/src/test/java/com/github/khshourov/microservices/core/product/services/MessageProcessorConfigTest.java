package com.github.khshourov.microservices.core.product.services;

import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.event.Event;
import com.github.khshourov.microservices.core.product.persistence.ProductEntity;
import com.github.khshourov.microservices.core.product.persistence.ProductRepository;
import com.github.khshourov.microservices.core.product.testlib.MongoDbTestBase;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class MessageProcessorConfigTest extends MongoDbTestBase {
  @Autowired
  @Qualifier("messageProcessor") private Consumer<Event<Integer, Product>> eventConsumer;

  @Autowired private ProductRepository productRepository;

  @BeforeEach
  void init() {
    StepVerifier.create(productRepository.deleteAll()).verifyComplete();
  }

  @Test
  void testCreateEvent() {
    Product product = new Product(1, "Product 1", 1, "SA");
    Event<Integer, Product> createEvent =
        new Event<>(Event.Type.CREATE, product.productId(), product);

    eventConsumer.accept(createEvent);

    StepVerifier.create(productRepository.findByProductId(product.productId()))
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testDeleteEvent() {
    ProductEntity existingEntity = new ProductEntity(1, "Product 1", 1);
    productRepository.save(existingEntity).block();
    Event<Integer, Product> deleteEvent =
        new Event<>(Event.Type.DELETE, existingEntity.getProductId(), null);

    eventConsumer.accept(deleteEvent);

    StepVerifier.create(productRepository.findByProductId(existingEntity.getProductId()))
        .expectNextCount(0)
        .verifyComplete();
  }
}
