package com.github.khshourov.microservices.core.recommendation.services;

import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.recommendation.RecommendationService;
import com.github.khshourov.microservices.api.event.Event;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessorConfig {
  private static final Logger log = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final RecommendationService recommendationService;

  @Autowired
  public MessageProcessorConfig(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  @Bean
  public Consumer<Event<Integer, Recommendation>> messageProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE -> {
          Recommendation recommendation = event.getData();
          log.debug(
              "Creating recommendation with product-id: {} and recommendation-id: {}",
              recommendation.productId(),
              recommendation.recommendationId());
          this.recommendationService.createRecommendation(recommendation).block();
        }
        case DELETE -> {
          log.debug("Deleting recommendations for product-id: {}", event.getKey());
          this.recommendationService.deleteRecommendations(event.getKey()).block();
        }
        default -> {
          log.warn("Incorrect event type: {}", event.getEventType());
          throw new IllegalArgumentException("Incorrect event type: " + event.getEventType());
        }
      }
    };
  }
}
