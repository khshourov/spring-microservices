package com.github.khshourov.microservices.core.recommendation.persistence;

import static com.github.khshourov.microservices.core.recommendation.testlib.Asserts.assertRecommendationEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.microservices.core.recommendation.testlib.MongoDbTestBase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

@DataMongoTest
public class RecommendationRepositoryTest extends MongoDbTestBase {
  @Autowired private RecommendationRepository repository;

  private RecommendationEntity savedEntity;

  @BeforeEach
  void init() {
    repository.deleteAll();

    RecommendationEntity entity = new RecommendationEntity(1, 1, "Author 1", 1, "Content 1");
    savedEntity = repository.save(entity);

    assertRecommendationEntity(entity, savedEntity);
  }

  @Test
  void getEntityByProductId() {
    List<RecommendationEntity> dbEntity = repository.findByProductId(1);

    assertEquals(1, dbEntity.size());
    assertRecommendationEntity(savedEntity, dbEntity.getFirst());
  }
}
