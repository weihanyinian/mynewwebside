package com.mywebside.blog.oj;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.oj.domain.JudgeResult;
import com.mywebside.blog.oj.dto.JudgeRequest;
import com.mywebside.blog.oj.dto.OjSubmissionRowDto;
import com.mywebside.blog.persistence.entity.OjSubmissionEntity;
import com.mywebside.blog.persistence.mapper.OjSubmissionEntityMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OjSubmissionService {

  private final OjSubmissionEntityMapper submissionMapper;

  public OjSubmissionService(OjSubmissionEntityMapper submissionMapper) {
    this.submissionMapper = submissionMapper;
  }

  public void record(long userId, String username, JudgeRequest req, JudgeResult r) {
    OjSubmissionEntity e = new OjSubmissionEntity();
    e.setUserId(userId);
    e.setUsername(username);
    e.setProblemId(req.problemId());
    e.setLanguage(req.language());
    e.setSourceCode(req.sourceCode() == null ? "" : req.sourceCode());
    e.setSubmitted(Boolean.TRUE.equals(req.submit()));
    e.setVerdict(r.verdict());
    e.setMessage(trunc(r.message(), 4000));
    e.setTimeSeconds(r.timeSeconds());
    e.setMemoryKb(r.memoryKb());
    e.setCreatedAt(LocalDateTime.now());
    submissionMapper.insert(e);
  }

  public PageResponse<OjSubmissionRowDto> pageMine(long userId, int page, int size) {
    if (page < 0) {
      page = 0;
    }
    if (size < 1 || size > 100) {
      size = 20;
    }
    long total = submissionMapper.selectCount(new LambdaQueryWrapper<OjSubmissionEntity>().eq(OjSubmissionEntity::getUserId, userId));
    List<OjSubmissionEntity> rows = submissionMapper.selectList(
        new LambdaQueryWrapper<OjSubmissionEntity>()
            .eq(OjSubmissionEntity::getUserId, userId)
            .orderByDesc(OjSubmissionEntity::getId)
            .last("LIMIT " + size + " OFFSET " + (page * size))
    );
    return new PageResponse<>(rows.stream().map(this::toRow).toList(), total, page, size);
  }

  public PageResponse<OjSubmissionRowDto> pageAll(int page, int size) {
    if (page < 0) {
      page = 0;
    }
    if (size < 1 || size > 100) {
      size = 20;
    }
    long total = submissionMapper.selectCount(null);
    List<OjSubmissionEntity> rows = submissionMapper.selectList(
        new LambdaQueryWrapper<OjSubmissionEntity>()
            .orderByDesc(OjSubmissionEntity::getId)
            .last("LIMIT " + size + " OFFSET " + (page * size))
    );
    return new PageResponse<>(rows.stream().map(this::toRow).toList(), total, page, size);
  }

  private OjSubmissionRowDto toRow(OjSubmissionEntity e) {
    String at = e.getCreatedAt() == null
        ? ""
        : e.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString();
    return new OjSubmissionRowDto(
        e.getId(),
        e.getUsername(),
        e.getProblemId(),
        e.getLanguage(),
        e.getSubmitted(),
        e.getVerdict(),
        e.getMessage(),
        e.getTimeSeconds(),
        e.getMemoryKb(),
        at
    );
  }

  private static String trunc(String s, int max) {
    if (s == null) {
      return null;
    }
    return s.length() <= max ? s : s.substring(0, max);
  }
}
