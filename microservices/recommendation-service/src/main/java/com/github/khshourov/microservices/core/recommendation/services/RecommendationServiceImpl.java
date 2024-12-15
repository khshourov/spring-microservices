package com.github.khshourov.microservices.core.recommendation.services;

import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.recommendation.RecommendationService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationEntity;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationMapper;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationRepository;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
  private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final RecommendationRepository repository;
  private final RecommendationMapper mapper;
  private final ServiceUtil serviceUtil;

  @Autowired
  public RecommendationServiceImpl(
      RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Recommendation createRecommendation(Recommendation request) {
    log.debug("POST /recommendation: {}", request);

    try {
      RecommendationEntity entity = this.mapper.apiToEntity(request);
      RecommendationEntity dbEntity = this.repository.save(entity);

      return this.mapper.entityToApi(dbEntity);
    } catch (DuplicateKeyException exception) {
      throw new InvalidInputException(
          "Duplicate combination of product-id and recommendation-id: ("
              + request.productId()
              + ", "
              + request.recommendationId()
              + ")");
    }
  }

  @Override
  public void deleteRecommendations(int productId) {
    this.repository.deleteAll(this.repository.findByProductId(productId));
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {
    log.debug("GET /recommendations?productId={}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    List<RecommendationEntity> entities = this.repository.findByProductId(productId);
    List<Recommendation> recommendations =
        this.mapper.entityListToApiList(entities).stream()
            .map(
                (recommendation ->
                    recommendation.updateServiceAddress(serviceUtil.getServiceAddress())))
            .toList();

    log.debug(
        "GET /recommendations?productId={}: response size: {}", productId, recommendations.size());

    return recommendations;
  }
}
