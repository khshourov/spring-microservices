package com.github.khshourov.microservices.core.product.services;

import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.product.ProductService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import com.github.khshourov.microservices.core.product.persistence.ProductEntity;
import com.github.khshourov.microservices.core.product.persistence.ProductMapper;
import com.github.khshourov.microservices.core.product.persistence.ProductRepository;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

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
  public Product createProduct(Product request) {
    log.debug("POST /product: {}", request);

    try {
      ProductEntity productEntity = this.mapper.apiToEntity(request);
      ProductEntity newEntity = this.repository.save(productEntity);

      log.debug("createProduct: entity created for product-id: {}", newEntity.getProductId());

      return this.mapper.entityToApi(newEntity);
    } catch (DuplicateKeyException exception) {
      throw new InvalidInputException("Duplicate product-id: " + request.productId());
    }
  }

  @Override
  public void deleteProduct(int productId) {
    log.debug("DELETE /product/{}", productId);

    this.repository.findByProductId(productId).ifPresent(this.repository::delete);
  }

  @Override
  public Product getProduct(int productId) {
    log.debug("GET /product/{}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    ProductEntity entity =
        this.repository
            .findByProductId(productId)
            .orElseThrow(
                () -> new NotFoundException("No product found for product-id: " + productId));

    Product response = this.mapper.entityToApi(entity);
    response = response.updateServiceAddress(serviceUtil.getServiceAddress());

    log.debug("getProduct: found product-id: {}", response.productId());

    return response;
  }
}
