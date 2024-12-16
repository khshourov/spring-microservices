package com.github.khshourov.microservices.core.review.persistence;

import static com.github.khshourov.microservices.core.review.testlib.Asserts.assertReviewEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.microservices.core.review.testlib.MySqlTestBase;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReviewEntityTest extends MySqlTestBase {
  @Autowired private ReviewRepository repository;

  private ReviewEntity savedEntity;

  @BeforeEach
  void init() {
    repository.deleteAll();

    ReviewEntity entity = new ReviewEntity(1, 1, "a1", "s1", "c1");
    savedEntity = repository.save(entity);

    assertReviewEntity(entity, savedEntity);
  }

  @Test
  void create() {
    ReviewEntity entity = new ReviewEntity(1, 2, "a2", "s2", "c2");

    ReviewEntity newEntity = repository.save(entity);

    assertReviewEntity(entity, newEntity);
  }

  @Test
  void productIdAndReviewIdCombinationNeedsToBeUnique() {
    ReviewEntity duplicateEntity = new ReviewEntity(1, 1, "a1", "s1", "c1");

    assertThrows(DataIntegrityViolationException.class, () -> repository.save(duplicateEntity));
  }

  @Test
  void update() {
    savedEntity.setAuthor("a2");

    repository.save(savedEntity);

    Optional<ReviewEntity> dbEntity = repository.findById(savedEntity.getId());

    assertTrue(dbEntity.isPresent());
    assertEquals(1, dbEntity.get().getVersion());
    assertEquals("a2", dbEntity.get().getAuthor());
  }

  @Test
  void staleVersionOfFetchedReviewEntityShouldBeRejected() {
    Optional<ReviewEntity> dbEntity1 = repository.findById(savedEntity.getId());
    Optional<ReviewEntity> dbEntity2 = repository.findById(savedEntity.getId());

    assertTrue(dbEntity1.isPresent());
    assertTrue(dbEntity2.isPresent());

    ReviewEntity entity1 = dbEntity1.get();
    ReviewEntity entity2 = dbEntity2.get();

    entity1.setAuthor("a11");
    repository.save(entity1);

    entity2.setAuthor("a12");
    assertThrows(OptimisticLockingFailureException.class, () -> repository.save(entity2));

    Optional<ReviewEntity> dbEntity = repository.findById(savedEntity.getId());
    assertTrue(dbEntity.isPresent());
    assertEquals("a11", dbEntity.get().getAuthor());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);

    Optional<ReviewEntity> dbEntity = repository.findById(savedEntity.getId());
    assertTrue(dbEntity.isEmpty());
  }
}
