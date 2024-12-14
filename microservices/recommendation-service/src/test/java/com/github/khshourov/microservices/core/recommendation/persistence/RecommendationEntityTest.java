package com.github.khshourov.microservices.core.recommendation.persistence;

import static com.github.khshourov.microservices.core.recommendation.testlib.Asserts.assertRecommendationEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.microservices.core.recommendation.testlib.MongoDbTestBase;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

@DataMongoTest
class RecommendationEntityTest extends MongoDbTestBase {
  @Autowired RecommendationRepository repository;

  private RecommendationEntity savedEntity;

  @BeforeEach
  void init() {
    repository.deleteAll();

    RecommendationEntity entity = new RecommendationEntity(1, 1, "Author 1", 1, "Content 1");
    savedEntity = repository.save(entity);

    assertRecommendationEntity(entity, savedEntity);
  }

  @Test
  void createRecommendationEntity() {
    RecommendationEntity newEntity = new RecommendationEntity(1, 2, "Author 2", 2, "Content 2");

    repository.save(newEntity);

    Optional<RecommendationEntity> dbEntity = repository.findById(newEntity.getId());

    assertTrue(dbEntity.isPresent());
    assertRecommendationEntity(newEntity, dbEntity.get());
  }

  @Test
  void updateRecommendationEntity() {
    savedEntity.setAuthor("Author 1-prime");

    repository.save(savedEntity);

    Optional<RecommendationEntity> dbEntity = repository.findById(savedEntity.getId());

    assertTrue(dbEntity.isPresent());
    assertEquals("Author 1-prime", dbEntity.get().getAuthor());
    assertEquals(1, dbEntity.get().getVersion());
    assertRecommendationEntity(savedEntity, dbEntity.get());
  }

  @Test
  void deleteRecommendationEntity() {
    repository.delete(savedEntity);

    Optional<RecommendationEntity> dbEntity = repository.findById(savedEntity.getId());

    assertTrue(dbEntity.isEmpty());
  }

  @Test
  void testCompoundIndex() {
    RecommendationEntity duplicateEntity =
        new RecommendationEntity(
            savedEntity.getProductId(),
            savedEntity.getRecommendationId(),
            "Author 2",
            2,
            "Content 2");

    assertThrows(DuplicateKeyException.class, () -> repository.save(duplicateEntity));
  }

  @Test
  void testOptimisticLock() {
    Optional<RecommendationEntity> dbEntity1 = repository.findById(savedEntity.getId());
    Optional<RecommendationEntity> dbEntity2 = repository.findById(savedEntity.getId());

    assertTrue(dbEntity1.isPresent());
    assertTrue(dbEntity2.isPresent());

    RecommendationEntity entity1 = dbEntity1.get();
    RecommendationEntity entity2 = dbEntity2.get();

    entity1.setAuthor("Author 1-prime");
    repository.save(entity1);

    entity2.setAuthor("Author 2-prime");
    assertThrows(OptimisticLockingFailureException.class, () -> repository.save(entity2));

    Optional<RecommendationEntity> dbEntity = repository.findById(savedEntity.getId());

    assertTrue(dbEntity.isPresent());
    assertEquals("Author 1-prime", dbEntity.get().getAuthor());
    assertEquals(1, dbEntity.get().getVersion());
  }
}
