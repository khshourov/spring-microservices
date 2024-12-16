package com.github.khshourov.microservices.core.review.persistence;

import com.github.khshourov.microservices.api.core.review.Review;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  ReviewEntity apiToEntity(Review api);

  @Mapping(target = "serviceAddress", ignore = true)
  Review entityToApi(ReviewEntity entity);

  List<ReviewEntity> apiListToEntityList(List<Review> reviews);

  List<Review> entityListToReviewList(List<ReviewEntity> entities);
}
