package com.mywebside.blog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

  /**
   * 网易云 NCM 代理依赖 {@link jakarta.servlet.http.HttpSession} 在多次请求间合并上游 Set-Cookie。
   * 全局 STATELESS 会禁止创建会话，导致扫码 / 会话式登录在 {@code setAttribute} 等环节出现 500。
   */
  @Bean
  @Order(1)
  public SecurityFilterChain ncmProxySessionChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/ncm/**");
    http.csrf(csrf -> csrf.disable());
    http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
    http.cors(Customizer.withDefaults());
    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthFilter jwtAuthFilter,
      RestAuthEntryPoint restAuthEntryPoint,
      RestAccessDeniedHandler restAccessDeniedHandler
  ) throws Exception {
    http.securityMatcher("/**");
    http.csrf(csrf -> csrf.disable());
    http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.cors(Customizer.withDefaults());
    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(restAuthEntryPoint)
        .accessDeniedHandler(restAccessDeniedHandler));
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
        .requestMatchers("/api/ncm/**").permitAll()
        .requestMatchers("/api/music/**").authenticated()
        .requestMatchers("/api/public/**").permitAll()
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
        .requestMatchers("/api/snippets/**").authenticated()
        .requestMatchers("/api/oj/**").authenticated()
        .requestMatchers("/api/stock/**").authenticated()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().denyAll()
    );
    http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
