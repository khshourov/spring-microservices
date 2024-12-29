package com.github.khshourov.springcloud.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.csrf(ServerHttpSecurity.CsrfSpec::disable);

    return http.build();
  }
}
