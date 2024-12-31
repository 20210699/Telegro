package com.telegro.telegro.global.auth.config;

import com.telegro.telegro.global.auth.exception.JWTAuthenticationEntryPoint;
import com.telegro.telegro.global.auth.filter.JWTFilter;
import com.telegro.telegro.global.auth.jwt.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JWTUtil jwtUtil;

  public SecurityConfig(
      JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JWTUtil jwtUtil) {
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.jwtUtil = jwtUtil;
  }
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .formLogin(formLogin -> formLogin.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
        )
        .exceptionHandling(handler -> handler.authenticationEntryPoint(jwtAuthenticationEntryPoint));

    return http.build();
  }
}
