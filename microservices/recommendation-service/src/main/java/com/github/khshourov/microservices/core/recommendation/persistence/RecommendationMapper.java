package com.github.khshourov.microservices.core.recommendation.persistence;

import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "rating", source = "recommendation.rate")
  RecommendationEntity apiToEntity(Recommendation recommendation);

  @Mapping(target = "serviceAddress", ignore = true)
  @Mapping(target = "rate", source = "recommendationEntity.rating")
  Recommendation entityToApi(RecommendationEntity recommendationEntity);

  List<RecommendationEntity> apiListToEntityList(List<Recommendation> apiList);

  List<Recommendation> entityListToApiList(List<RecommendationEntity> entityList);
}
