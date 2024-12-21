package com.github.khshourov.microservices.core.review.services;

import com.github.khshourov.microservices.api.core.review.Review;
import com.github.khshourov.microservices.api.core.review.ReviewService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.core.review.persistence.ReviewEntity;
import com.github.khshourov.microservices.core.review.persistence.ReviewMapper;
import com.github.khshourov.microservices.core.review.persistence.ReviewRepository;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
public class ReviewServiceImpl implements ReviewService {
  private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ReviewRepository repository;
  private final ReviewMapper mapper;
  private final ServiceUtil serviceUtil;
  private final Scheduler jdbcScheduler;

  @Autowired
  public ReviewServiceImpl(
      ReviewRepository repository,
      ReviewMapper mapper,
      ServiceUtil serviceUtil,
      @Qualifier("jdbcScheduler") Scheduler jdbcScheduler) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
    this.jdbcScheduler = jdbcScheduler;
  }

  @Override
  public Mono<Review> createReview(Review request) {
    log.debug("POST /review : {}", request);

    return Mono.fromCallable(() -> this.internalCreateReview(request))
        .subscribeOn(this.jdbcScheduler);
  }

  private Review internalCreateReview(Review request) {
    try {
      ReviewEntity entity = this.mapper.apiToEntity(request);
      ReviewEntity createdEntity = this.repository.save(entity);
      return this.mapper.entityToApi(createdEntity);
    } catch (DataIntegrityViolationException exception) {
      throw new InvalidInputException("Product-Id and Review-Id, together must be unique");
    }
  }

  @Override
  public Mono<Void> deleteReviews(int productId) {
    log.debug("DELETE /reviews?productId={}", productId);

    return Mono.fromRunnable(() -> this.internalDeleteReviews(productId))
        .subscribeOn(this.jdbcScheduler)
        .then();
  }

  private void internalDeleteReviews(int productId) {
    this.repository.deleteAll(this.repository.findByProductId(productId));
  }

  @Override
  public Flux<Review> getReviews(int productId) {
    log.debug("GET /reviews?productId={}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    return Mono.fromCallable(() -> this.internalGetReviews(productId))
        .flatMapMany(Flux::fromIterable)
        .subscribeOn(this.jdbcScheduler);
  }

  private List<Review> internalGetReviews(int productId) {
    List<ReviewEntity> entities = this.repository.findByProductId(productId);
    List<Review> reviews =
        this.mapper.entityListToApiList(entities).stream()
            .map((review -> review.updateServiceAddress(this.serviceUtil.getServiceAddress())))
            .toList();

    log.debug("GET /reviews?productId={}: response size: {}", productId, reviews.size());

    return reviews;
  }
}
