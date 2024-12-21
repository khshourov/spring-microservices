package com.github.khshourov.microservices.core.review.services;

import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.core.review.ReviewService;
import com.github.khshourov.microservices.api.event.Event;
import com.github.khshourov.microservices.api.exceptions.EventProcessingException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageProcessorConfig {
  private static final Logger log = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final ReviewService reviewService;

  @Autowired
  public MessageProcessorConfig(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @Bean
  public Consumer<Event<Integer, Review>> messageProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE -> {
          log.debug("Creating review with id: {}", event.getData().reviewId());
          this.reviewService.createReview(event.getData()).block();
        }
        case DELETE -> {
          log.debug("Deleting reviews for product-id: {}", event.getKey());
          this.reviewService.deleteReviews(event.getKey()).block();
        }
        default -> {
          log.warn("Incorrect event type: {}", event.getEventType());
          throw new EventProcessingException("Incorrect event-type: " + event.getEventType());
        }
      }
    };
  }
}
