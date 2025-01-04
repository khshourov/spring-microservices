package com.github.khshourov.microservices.core.product.services;

import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.product.ProductService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import com.github.khshourov.microservices.core.product.persistence.ProductEntity;
import com.github.khshourov.microservices.core.product.persistence.ProductMapper;
import com.github.khshourov.microservices.core.product.persistence.ProductRepository;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import java.time.Duration;
import java.util.Random;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProductServiceImpl implements ProductService {
  private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final Random randomNumberGenerator = new Random();

  private final ServiceUtil serviceUtil;
  private final ProductRepository repository;
  private final ProductMapper mapper;

  @Autowired
  public ProductServiceImpl(
      ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> createProduct(Product request) {
    log.debug("POST /product: {}", request);

    ProductEntity productEntity = this.mapper.apiToEntity(request);
    return this.repository
        .save(productEntity)
        .onErrorMap(
            DuplicateKeyException.class,
            e -> new InvalidInputException("Duplicate product-id: " + request.productId()))
        .log(log.getName(), Level.FINE)
        .map(this.mapper::entityToApi);
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {
    log.debug("DELETE /product/{}", productId);

    return this.repository
        .findByProductId(productId)
        .log(log.getName(), Level.FINE)
        .map(this.repository::delete)
        .flatMap(entity -> entity);
  }

  @Override
  public Mono<Product> getProduct(int productId, int delay, int faultPercent) {
    log.debug("GET /product/{}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    return this.repository
        .findByProductId(productId)
        .map(entity -> this.throwErrorIfBadLuck(entity, faultPercent))
        .delayElement(Duration.ofSeconds(delay))
        .switchIfEmpty(
            Mono.error(new NotFoundException("No product found for product-id: " + productId)))
        .log(log.getName(), Level.FINE)
        .map(this.mapper::entityToApi)
        .map(product -> product.updateServiceAddress(serviceUtil.getServiceAddress()));
  }

  private ProductEntity throwErrorIfBadLuck(ProductEntity entity, int faultPercent) {
    if (faultPercent == 0) {
      return entity;
    }

    int randomThreshold = getRandomNumber(1, 100);

    if (faultPercent < randomThreshold) {
      log.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold);
    } else {
      log.info("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
      throw new RuntimeException("Something went wrong...");
    }

    return entity;
  }

  private int getRandomNumber(int min, int max) {
    if (max < min) {
      throw new IllegalArgumentException("Max must be greater than min");
    }

    return randomNumberGenerator.nextInt((max - min) + 1) + min;
  }
}
