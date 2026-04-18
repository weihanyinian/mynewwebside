package com.mywebside.blog.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 确保 users / oj_problem / oj_submission 存在（与 JPA 管理的 blog 表并存）。
 * 使用 CREATE IF NOT EXISTS，兼容 MySQL 与 H2（MODE=MySQL）。
 */
@Component
@Order(0)
public class AuthOjSchemaRunner implements ApplicationRunner {

  private final JdbcTemplate jdbc;

  public AuthOjSchemaRunner(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void run(ApplicationArguments args) {
    jdbc.execute(
        """
        CREATE TABLE IF NOT EXISTS users (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          username VARCHAR(64) NOT NULL UNIQUE,
          password VARCHAR(255) NOT NULL,
          nickname VARCHAR(64) NOT NULL,
          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
        """
    );
    jdbc.execute(
        """
        CREATE TABLE IF NOT EXISTS oj_problem (
          id VARCHAR(64) PRIMARY KEY,
          title VARCHAR(255) NOT NULL,
          difficulty VARCHAR(32) NOT NULL,
          judge_mode VARCHAR(32) NOT NULL,
          description LONGTEXT NOT NULL,
          input_desc TEXT NOT NULL,
          output_desc TEXT NOT NULL,
          time_limit_sec INT NOT NULL,
          memory_limit_mb INT NOT NULL,
          supported_langs_json LONGTEXT NOT NULL,
          samples_json LONGTEXT NOT NULL,
          test_cases_json LONGTEXT NOT NULL,
          reference_solution_json LONGTEXT,
          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
        """
    );
    jdbc.execute(
        """
        CREATE TABLE IF NOT EXISTS oj_submission (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          user_id BIGINT NOT NULL,
          username VARCHAR(64) NOT NULL,
          problem_id VARCHAR(64) NOT NULL,
          language VARCHAR(32) NOT NULL,
          source_code LONGTEXT NOT NULL,
          submitted BOOLEAN NOT NULL,
          verdict VARCHAR(32) NOT NULL,
          message TEXT,
          time_seconds DOUBLE,
          memory_kb INT,
          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
        """
    );
  }
}
