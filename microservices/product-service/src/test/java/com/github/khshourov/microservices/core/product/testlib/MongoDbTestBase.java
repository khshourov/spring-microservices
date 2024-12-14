package com.github.khshourov.microservices.core.product.testlib;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;

public class MongoDbTestBase {
  @ServiceConnection
  private static final MongoDBContainer database = new MongoDBContainer("mongo:8.0.4");
}
