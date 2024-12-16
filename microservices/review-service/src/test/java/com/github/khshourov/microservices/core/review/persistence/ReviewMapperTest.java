package com.github.khshourov.microservices.core.review.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.khshourov.microservices.api.core.review.Review;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class ReviewMapperTest {
  private final ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

  @Test
  void apiToEntity() {
    Review review = new Review(1, 1, "a1", "s1", "c1", "sa");

    ReviewEntity entity = mapper.apiToEntity(review);

    assertEquals(review.productId(), entity.getProductId());
    assertEquals(review.reviewId(), entity.getReviewId());
    assertEquals(review.author(), entity.getAuthor());
    assertEquals(review.subject(), entity.getSubject());
    assertEquals(review.content(), entity.getContent());
  }

  @Test
  void entityToApi() {
    ReviewEntity entity = new ReviewEntity(1, 1, "a1", "s1", "c1");

    Review review = mapper.entityToApi(entity);

    assertEquals(entity.getProductId(), review.productId());
    assertEquals(entity.getReviewId(), review.reviewId());
    assertEquals(entity.getAuthor(), review.author());
    assertEquals(entity.getSubject(), review.subject());
    assertEquals(entity.getContent(), review.content());
    assertNull(review.serviceAddress());
  }

  @Test
  void apiListToEntityList() {
    List<Review> reviews = Collections.singletonList(new Review(1, 1, "a1", "s1", "c1", "sa"));

    List<ReviewEntity> entities = mapper.apiListToEntityList(reviews);

    assertEquals(reviews.size(), entities.size());

    Review review = reviews.getFirst();
    ReviewEntity entity = entities.getFirst();

    assertEquals(review.productId(), entity.getProductId());
    assertEquals(review.reviewId(), entity.getReviewId());
    assertEquals(review.author(), entity.getAuthor());
    assertEquals(review.subject(), entity.getSubject());
    assertEquals(review.content(), entity.getContent());
  }

  @Test
  void entityListToApiList() {
    List<ReviewEntity> entities =
        Collections.singletonList(new ReviewEntity(1, 1, "a1", "s1", "c1"));

    List<Review> reviews = mapper.entityListToReviewList(entities);

    assertEquals(entities.size(), reviews.size());

    ReviewEntity entity = entities.getFirst();
    Review review = reviews.getFirst();

    assertEquals(entity.getProductId(), review.productId());
    assertEquals(entity.getReviewId(), review.reviewId());
    assertEquals(entity.getAuthor(), review.author());
    assertEquals(entity.getSubject(), review.subject());
    assertEquals(entity.getContent(), review.content());
    assertNull(review.serviceAddress());
  }
}
