package com.github.khshourov.microservices.core.review;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("com.github.khshourov.microservices")
public class ReviewServiceApplication {
  private final int threadPoolSize;
  private final int taskQueueSize;

  public ReviewServiceApplication(
      @Value("${app.threadPoolSize:10}") int threadPoolSize,
      @Value("${app.taskQueueSize:100}") int taskQueueSize) {
    this.threadPoolSize = threadPoolSize;
    this.taskQueueSize = taskQueueSize;
  }

  @Bean
  public Scheduler jdbcScheduler() {
    return Schedulers.newBoundedElastic(this.threadPoolSize, this.taskQueueSize, "jdbc-pool");
  }

  public static void main(String[] args) {
    SpringApplication.run(ReviewServiceApplication.class, args);
  }
}
