package com.github.khshourov.microservices.core.review.testlib;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

public class MySqlTestBase {
  @ServiceConnection
  protected static final JdbcDatabaseContainer<?> database = new MySQLContainer<>("mysql:9.1.0");

  static {
    database.start();
  }
}
