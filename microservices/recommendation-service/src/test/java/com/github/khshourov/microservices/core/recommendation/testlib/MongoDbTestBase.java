package com.github.khshourov.microservices.core.recommendation.testlib;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;

public class MongoDbTestBase {
  @ServiceConnection
  private static final MongoDBContainer database = new MongoDBContainer("mongo:8.0.4");

  static {
    database.start();
  }
}
