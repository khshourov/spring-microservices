package com.github.khshourov.microservices.core.recommendation.testlib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationEntity;

public class Asserts {
  public static void assertRecommendationEntity(
      RecommendationEntity expected, RecommendationEntity actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getVersion(), actual.getVersion());
    assertEquals(expected.getProductId(), actual.getProductId());
    assertEquals(expected.getRecommendationId(), actual.getRecommendationId());
    assertEquals(expected.getAuthor(), actual.getAuthor());
    assertEquals(expected.getRating(), actual.getRating());
    assertEquals(expected.getContent(), actual.getContent());
  }
}
