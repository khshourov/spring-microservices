package com.github.khshourov.microservices.composite.product;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("com.github.khshourov.microservices")
@ConfigurationPropertiesScan
public class ProductCompositeServiceApplication {
  private static final Logger log =
      LoggerFactory.getLogger(ProductCompositeServiceApplication.class);

  @Value("${api.common.version}")
  String apiVersion;

  @Value("${api.common.title}")
  String apiTitle;

  @Value("${api.common.description}")
  String apiDescription;

  @Value("${api.common.termsOfService}")
  String apiTermsOfService;

  @Value("${api.common.license}")
  String apiLicense;

  @Value("${api.common.licenseUrl}")
  String apiLicenseUrl;

  @Value("${api.common.externalDocDesc}")
  String apiExternalDocDesc;

  @Value("${api.common.externalDocUrl}")
  String apiExternalDocUrl;

  @Value("${api.common.contact.name}")
  String apiContactName;

  @Value("${api.common.contact.url}")
  String apiContactUrl;

  @Value("${api.common.contact.email}")
  String apiContactEmail;

  /**
   * Will exposed on $HOST:$PORT/swagger-ui.html
   *
   * @return the common OpenAPI documentation
   */
  @Bean
  public OpenAPI getOpenApiDocumentation() {
    return new OpenAPI()
        .info(
            new Info()
                .title(apiTitle)
                .description(apiDescription)
                .version(apiVersion)
                .contact(
                    new Contact().name(apiContactName).url(apiContactUrl).email(apiContactEmail))
                .termsOfService(apiTermsOfService)
                .license(new License().name(apiLicense).url(apiLicenseUrl)))
        .externalDocs(
            new ExternalDocumentation().description(apiExternalDocDesc).url(apiExternalDocUrl));
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  private final Integer threadPoolSize;
  private final Integer taskQueueSize;

  @Autowired
  public ProductCompositeServiceApplication(
      @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
      @Value("${app.taskQueueSize:100}") Integer taskQueueSize) {
    this.threadPoolSize = threadPoolSize;
    this.taskQueueSize = taskQueueSize;
  }

  @Bean
  public Scheduler publishEventScheduler() {
    log.info("Creates a messagingScheduler with connectionPoolSize = {}", threadPoolSize);
    return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
  }

  @Autowired private ReactorLoadBalancerExchangeFilterFunction lbFunction;

  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    return builder.filter(lbFunction).build();
  }

  public static void main(String[] args) {
    Hooks.enableAutomaticContextPropagation();

    SpringApplication.run(ProductCompositeServiceApplication.class, args);
  }
}
