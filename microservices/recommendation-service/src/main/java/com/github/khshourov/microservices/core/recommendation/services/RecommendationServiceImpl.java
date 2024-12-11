package com.github.khshourov.microservices.core.recommendation.services;

import com.github.khshourov.microservices.api.core.recommendation.Recommendation;
import com.github.khshourov.microservices.api.core.recommendation.RecommendationService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
  private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final ServiceUtil serviceUtil;

  @Autowired
  public RecommendationServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {
    log.debug("GET /recommendations?productId={}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    if (productId == 13) {
      log.debug("No recommendation found for product-id: {}", productId);
      return List.of();
    }

    List<Recommendation> recommendations =
        List.of(
            new Recommendation(
                productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()),
            new Recommendation(
                productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()),
            new Recommendation(
                productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));

    log.debug(
        "GET /recommendations?productId={}: response size: {}", productId, recommendations.size());

    return recommendations;
  }
}
