package com.github.khshourov.microservices.core.product.services;

import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.core.product.ProductService;
import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import com.github.khshourov.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {
  private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final ServiceUtil serviceUtil;

  @Autowired
  public ProductServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Product getProduct(int productId) {
    log.debug("GET /product/{}", productId);

    if (productId < 1) {
      throw new InvalidInputException("Invalid product-id: " + productId);
    }

    if (productId == 13) {
      throw new NotFoundException("No product found for product-id: " + productId);
    }

    return new Product(productId, "product-" + productId, 123, serviceUtil.getServiceAddress());
  }
}
