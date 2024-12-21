package com.github.khshourov.microservices.core.recommendation.services;

import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.recommendation.RecommendationService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationEntity;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationMapper;
import com.github.khshourov.microservices.core.recommendation.persistence.RecommendationRepository;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
  public Mono<Recommendation> createRecommendation(Recommendation request) {
    log.debug("POST /recommendation: {}", request);

    RecommendationEntity entity = this.mapper.apiToEntity(request);
    return this.repository
        .save(entity)
        .onErrorMap(
            DuplicateKeyException.class,
            exception ->
                new InvalidInputException(
                    "Duplicate combination of product-id and recommendation-id: ("
                        + request.productId()
                        + ", "
                        + request.recommendationId()
                        + ")"))
        .log(log.getName(), Level.FINE)
        .map(this.mapper::entityToApi)
        .map(
            recommendation -> recommendation.updateServiceAddress(serviceUtil.getServiceAddress()));
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {
    return this.repository.deleteAll(this.repository.findByProductId(productId));
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    log.debug("GET /recommendations?productId={}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    return this.repository
        .findByProductId(productId)
        .log(log.getName(), Level.FINE)
        .map(this.mapper::entityToApi)
        .map(
            recommendation -> recommendation.updateServiceAddress(serviceUtil.getServiceAddress()));
  }
}
