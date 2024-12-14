package com.github.khshourov.microservices.core.recommendation.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class RecommendationMapperTest {
  private final RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

  @Test
  void apiToEntity() {
    Recommendation recommendation = new Recommendation(1, 1, "Author 1", 1, "Content 1", "SA");

    RecommendationEntity entity = mapper.apiToEntity(recommendation);

    assertEquals(recommendation.productId(), entity.getProductId());
    assertEquals(recommendation.recommendationId(), entity.getRecommendationId());
    assertEquals(recommendation.author(), entity.getAuthor());
    assertEquals(recommendation.rate(), entity.getRating());
    assertEquals(recommendation.content(), entity.getContent());
  }

  @Test
  void entityToApi() {
    RecommendationEntity entity = new RecommendationEntity(1, 1, "Author 1", 1, "Content 1");

    Recommendation recommendation = mapper.entityToApi(entity);

    assertEquals(entity.getProductId(), recommendation.productId());
    assertEquals(entity.getRecommendationId(), recommendation.recommendationId());
    assertEquals(entity.getAuthor(), recommendation.author());
    assertEquals(entity.getRating(), recommendation.rate());
    assertEquals(entity.getContent(), recommendation.content());
    assertNull(recommendation.serviceAddress());
  }

  @Test
  void apiListToEntityList() {
    List<Recommendation> recommendations =
        List.of(new Recommendation(1, 1, "Author 1", 1, "Content 1", "SA"));

    List<RecommendationEntity> entries = mapper.apiListToEntityList(recommendations);

    assertEquals(1, entries.size());

    Recommendation recommendation = recommendations.getFirst();
    RecommendationEntity entity = entries.getFirst();

    assertEquals(recommendation.productId(), entity.getProductId());
    assertEquals(recommendation.recommendationId(), entity.getRecommendationId());
    assertEquals(recommendation.author(), entity.getAuthor());
    assertEquals(recommendation.rate(), entity.getRating());
    assertEquals(recommendation.content(), entity.getContent());
  }

  @Test
  void entityListToApiList() {
    List<RecommendationEntity> entities =
        List.of(new RecommendationEntity(1, 1, "Author 1", 1, "Content 1"));

    List<Recommendation> recommendations = mapper.entityListToApiList(entities);

    assertEquals(1, recommendations.size());

    RecommendationEntity entity = entities.getFirst();
    Recommendation recommendation = recommendations.getFirst();

    assertEquals(entity.getProductId(), recommendation.productId());
    assertEquals(entity.getRecommendationId(), recommendation.recommendationId());
    assertEquals(entity.getAuthor(), recommendation.author());
    assertEquals(entity.getRating(), recommendation.rate());
    assertEquals(entity.getContent(), recommendation.content());
    assertNull(recommendation.serviceAddress());
  }
}
