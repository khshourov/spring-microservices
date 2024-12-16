package com.github.khshourov.microservices.core.review.testlib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.microservices.core.review.persistence.ReviewEntity;

public class Asserts {
  public static void assertReviewEntity(ReviewEntity expected, ReviewEntity actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getVersion(), actual.getVersion());
    assertEquals(expected.getProductId(), actual.getProductId());
    assertEquals(expected.getReviewId(), actual.getReviewId());
    assertEquals(expected.getAuthor(), actual.getAuthor());
    assertEquals(expected.getSubject(), actual.getSubject());
    assertEquals(expected.getContent(), actual.getContent());
  }
}
