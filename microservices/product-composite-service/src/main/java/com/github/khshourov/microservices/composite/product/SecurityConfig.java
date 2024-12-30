package com.github.khshourov.microservices.composite.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  @Bean
  SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
    httpSecurity
        .authorizeExchange(
            authorizeExchangeSpec ->
                authorizeExchangeSpec
                    .pathMatchers("/openapi/**")
                    .permitAll()
                    .pathMatchers("/webjars/**")
                    .permitAll()
                    .pathMatchers("/actuator/**")
                    .permitAll()
                    .pathMatchers(HttpMethod.POST, "/composite/product/**")
                    .hasAuthority("SCOPE_product:write")
                    .pathMatchers(HttpMethod.DELETE, "/composite/product/**")
                    .hasAuthority("SCOPE_product:write")
                    .pathMatchers(HttpMethod.GET, "/composite/product/**")
                    .hasAuthority("SCOPE_product:read")
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(Customizer.withDefaults()));
    return httpSecurity.build();
  }
}
