package com.github.khshourov.microservices.core.review.services;

import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.core.review.ReviewService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewServiceImpl implements ReviewService {
  private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ServiceUtil serviceUtil;

  @Autowired
  public ReviewServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }

  @Override
  public List<Review> getReviews(int productId) {
    log.debug("GET /reviews?productId={}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    if (productId == 13) {
      log.debug("No review found for product-id: {}", productId);
      return List.of();
    }

    List<Review> reviews =
        List.of(
            new Review(
                productId,
                1,
                "Author 1",
                "Subject 1",
                "Content 1",
                serviceUtil.getServiceAddress()),
            new Review(
                productId,
                2,
                "Author 2",
                "Subject 2",
                "Content 2",
                serviceUtil.getServiceAddress()),
            new Review(
                productId,
                3,
                "Author 3",
                "Subject 3",
                "Content 3",
                serviceUtil.getServiceAddress()));

    log.debug("GET /reviews?productId={}: response size: {}", productId, reviews.size());

    return reviews;
  }
}
