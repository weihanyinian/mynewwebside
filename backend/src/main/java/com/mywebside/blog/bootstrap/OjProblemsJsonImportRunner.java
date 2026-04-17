package com.mywebside.blog.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.oj.OjProblemStore;
import com.mywebside.blog.oj.domain.Problem;
import com.mywebside.blog.persistence.mapper.OjProblemEntityMapper;
import java.io.InputStream;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** 当 oj_problem 为空时，从 classpath {@code oj/problems.json} 导入。 */
@Component
@Order(2)
public class OjProblemsJsonImportRunner implements ApplicationRunner {

  private final OjProblemEntityMapper problemMapper;
  private final OjProblemStore ojProblemStore;
  private final ObjectMapper objectMapper;

  public OjProblemsJsonImportRunner(
      OjProblemEntityMapper problemMapper,
      OjProblemStore ojProblemStore,
      ObjectMapper objectMapper
  ) {
    this.problemMapper = problemMapper;
    this.ojProblemStore = ojProblemStore;
    this.objectMapper = objectMapper;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    Long n = problemMapper.selectCount(null);
    if (n != null && n > 0) {
      return;
    }
    ClassPathResource res = new ClassPathResource("oj/problems.json");
    try (InputStream in = res.getInputStream()) {
      java.util.List<Problem> list = objectMapper.readValue(in, new TypeReference<>() {});
      for (Problem p : list) {
        ojProblemStore.upsertFromProblem(p);
      }
    }
  }
}
