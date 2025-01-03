package com.github.khshourov.springcloud.servicediscovery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BasicAuthenticationConfig {
  private final String username;
  private final String password;

  public BasicAuthenticationConfig(
      @Value("${app.eureka-username}") String username,
      @Value("${app.eureka-password}") String password) {
    this.username = username;
    this.password = password;
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsManager(PasswordEncoder passwordEncoder) {
    UserDetails userDetails =
        User.builder()
            .username(this.username)
            .password(passwordEncoder.encode(this.password))
            .roles("USER")
            .build();

    return new InMemoryUserDetailsManager(userDetails);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorizeHttpRequests -> authorizeHttpRequests.anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
