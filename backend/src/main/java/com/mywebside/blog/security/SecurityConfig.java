package com.mywebside.blog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthFilter jwtAuthFilter,
      RestAuthEntryPoint restAuthEntryPoint,
      RestAccessDeniedHandler restAccessDeniedHandler
  ) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.cors(Customizer.withDefaults());
    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(restAuthEntryPoint)
        .accessDeniedHandler(restAccessDeniedHandler));
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
        .requestMatchers("/api/public/**").permitAll()
        .requestMatchers("/api/snippets/**").authenticated()
        .requestMatchers("/api/oj/**").authenticated()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().denyAll()
    );
    http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
