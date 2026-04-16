package com.mywebside.blog.oj;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.oj.domain.Problem;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** 从 classpath 读取 {@code oj/problems.json}，无数据库。 */
@Service
public class ProblemJsonStore {

  private final ObjectMapper objectMapper;
  private List<Problem> problems = List.of();

  public ProblemJsonStore(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  void load() throws IOException {
    var res = new ClassPathResource("oj/problems.json");
    problems = objectMapper.readValue(res.getInputStream(), new TypeReference<>() {});
  }

  public List<Problem> all() {
    return problems;
  }

  public Optional<Problem> find(String id) {
    return problems.stream().filter(p -> p.id().equals(id)).findFirst();
  }
}
