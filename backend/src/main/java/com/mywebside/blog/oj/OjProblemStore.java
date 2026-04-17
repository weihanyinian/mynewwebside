package com.mywebside.blog.oj;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.oj.domain.Problem;
import com.mywebside.blog.persistence.entity.OjProblemEntity;
import com.mywebside.blog.persistence.mapper.OjProblemEntityMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** OJ 题目：持久化在 oj_problem 表，结构与 {@link Problem} / problems.json 一致。 */
@Service
public class OjProblemStore {

  private final OjProblemEntityMapper problemMapper;
  private final ObjectMapper objectMapper;

  public OjProblemStore(OjProblemEntityMapper problemMapper, ObjectMapper objectMapper) {
    this.problemMapper = problemMapper;
    this.objectMapper = objectMapper;
  }

  public List<Problem> all() {
    return problemMapper.selectList(new LambdaQueryWrapper<OjProblemEntity>().orderByAsc(OjProblemEntity::getId))
        .stream()
        .map(this::toProblem)
        .toList();
  }

  public Optional<Problem> find(String id) {
    OjProblemEntity e = problemMapper.selectById(id);
    return e == null ? Optional.empty() : Optional.of(toProblem(e));
  }

  public Problem require(String id) {
    return find(id).orElseThrow(() -> new BusinessException(404, "题目不存在"));
  }

  public void upsertFromProblem(Problem p) {
    OjProblemEntity e = fromProblem(p);
    LocalDateTime now = LocalDateTime.now();
    e.setUpdatedAt(now);
    if (problemMapper.selectById(p.id()) == null) {
      e.setCreatedAt(now);
      problemMapper.insert(e);
    } else {
      problemMapper.updateById(e);
    }
  }

  public void deleteById(String id) {
    problemMapper.deleteById(id);
  }

  public Problem toProblem(OjProblemEntity e) {
    try {
      List<String> langs = objectMapper.readValue(e.getSupportedLangsJson(), new TypeReference<>() {});
      List<Problem.TestCase> samples = objectMapper.readValue(e.getSamplesJson(), new TypeReference<>() {});
      List<Problem.TestCase> tests = objectMapper.readValue(e.getTestCasesJson(), new TypeReference<>() {});
      Map<String, String> ref = e.getReferenceSolutionJson() == null || e.getReferenceSolutionJson().isBlank()
          ? new LinkedHashMap<>()
          : objectMapper.readValue(e.getReferenceSolutionJson(), new TypeReference<>() {});
      return new Problem(
          e.getId(),
          e.getTitle(),
          e.getDifficulty(),
          e.getJudgeMode(),
          e.getDescription(),
          e.getInputDesc(),
          e.getOutputDesc(),
          e.getTimeLimitSec(),
          e.getMemoryLimitMb(),
          langs,
          samples,
          tests,
          ref
      );
    } catch (Exception ex) {
      throw new BusinessException(500, "题目数据损坏：" + e.getId());
    }
  }

  public OjProblemEntity fromProblem(Problem p) {
    try {
      OjProblemEntity e = new OjProblemEntity();
      e.setId(p.id());
      e.setTitle(p.title());
      e.setDifficulty(p.difficulty());
      e.setJudgeMode(p.judgeMode());
      e.setDescription(p.description());
      e.setInputDesc(p.inputDesc());
      e.setOutputDesc(p.outputDesc());
      e.setTimeLimitSec(p.timeLimitSec());
      e.setMemoryLimitMb(p.memoryLimitMb());
      e.setSupportedLangsJson(objectMapper.writeValueAsString(p.supportedLangs()));
      e.setSamplesJson(objectMapper.writeValueAsString(p.samples()));
      e.setTestCasesJson(objectMapper.writeValueAsString(p.testCases()));
      e.setReferenceSolutionJson(
          p.referenceSolution() == null || p.referenceSolution().isEmpty()
              ? "{}"
              : objectMapper.writeValueAsString(p.referenceSolution())
      );
      return e;
    } catch (Exception ex) {
      throw new BusinessException(400, "题目序列化失败");
    }
  }
}
