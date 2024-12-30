package com.github.khshourov.springcloud.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            authorizeExchangeSpec ->
                authorizeExchangeSpec
                    .pathMatchers("/headerrouting/**")
                    .permitAll()
                    .pathMatchers("/actuator/**")
                    .permitAll()
                    .pathMatchers("/eureka/**")
                    .permitAll()
                    .pathMatchers("/oauth2/**")
                    .permitAll()
                    .pathMatchers("/login/**")
                    .permitAll()
                    .pathMatchers("/error/**")
                    .permitAll()
                    .pathMatchers("/openapi/**")
                    .permitAll()
                    .pathMatchers("/webjars/**")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(Customizer.withDefaults()));

    return http.build();
  }
}
