package com.github.khshourov.microservices.core.product.services;

import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.product.ProductService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import com.github.khshourov.microservices.core.product.persistence.ProductEntity;
import com.github.khshourov.microservices.core.product.persistence.ProductMapper;
import com.github.khshourov.microservices.core.product.persistence.ProductRepository;
import com.github.khshourov.microservices.util.http.ServiceUtil;
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
  public Mono<Product> getProduct(int productId) {
    log.debug("GET /product/{}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    return this.repository
        .findByProductId(productId)
        .switchIfEmpty(
            Mono.error(new NotFoundException("No product found for product-id: " + productId)))
        .log(log.getName(), Level.FINE)
        .map(this.mapper::entityToApi)
        .map(product -> product.updateServiceAddress(serviceUtil.getServiceAddress()));
  }
}
