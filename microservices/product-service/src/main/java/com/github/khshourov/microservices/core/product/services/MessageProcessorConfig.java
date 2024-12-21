package com.github.khshourov.microservices.core.product.services;

import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.product.ProductService;
import com.github.khshourov.microservices.api.event.Event;
import com.github.khshourov.microservices.api.exceptions.EventProcessingException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessorConfig {
  private static final Logger log = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final ProductService productService;

  @Autowired
  public MessageProcessorConfig(ProductService productService) {
    this.productService = productService;
  }

  @Bean
  public Consumer<Event<Integer, Product>> messageProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE -> {
          log.debug("Creating product with id: {}", event.getData().productId());
          this.productService.createProduct(event.getData()).block();
        }
        case DELETE -> {
          log.debug("Deleting product with id: {}", event.getKey());
          this.productService.deleteProduct(event.getKey()).block();
        }
        default -> {
          log.warn("Incorrect event type: {}", event.getEventType());
          throw new EventProcessingException("Incorrect event type: " + event.getEventType());
        }
      }
    };
  }
}
