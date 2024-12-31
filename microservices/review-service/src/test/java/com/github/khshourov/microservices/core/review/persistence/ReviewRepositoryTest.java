package com.github.khshourov.microservices.core.review.persistence;

import static com.github.khshourov.microservices.core.review.testlib.Asserts.assertReviewEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.microservices.core.review.testlib.MySqlTestBase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(properties = {"spring.cloud.config.enabled=false"})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReviewRepositoryTest extends MySqlTestBase {
  @Autowired private ReviewRepository repository;

  private ReviewEntity savedEntity;

  @BeforeEach
  void init() {
    repository.deleteAll();

    ReviewEntity entity = new ReviewEntity(1, 1, "a1", "s1", "c1");
    savedEntity = repository.save(entity);

    assertReviewEntity(savedEntity, entity);
  }

  @Test
  void getByProductId() {
    List<ReviewEntity> entities = repository.findByProductId(1);

    assertThat(entities, hasSize(1));
    assertReviewEntity(savedEntity, entities.getFirst());
  }

  @Test
  void emptyListShouldReturnIfProductIdIsNotFound() {
    final int notFoundProductId = 100;

    List<ReviewEntity> entities = repository.findByProductId(notFoundProductId);

    assertTrue(entities.isEmpty());
  }
}
