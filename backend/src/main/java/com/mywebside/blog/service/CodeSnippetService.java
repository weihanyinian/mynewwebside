package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.domain.CodeSnippet;
import com.mywebside.blog.dto.CodeSnippetDto;
import com.mywebside.blog.dto.CodeSnippetUpsertRequest;
import com.mywebside.blog.repo.CodeSnippetRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeSnippetService {

  private final CodeSnippetRepository snippetRepository;

  public CodeSnippetService(CodeSnippetRepository snippetRepository) {
    this.snippetRepository = snippetRepository;
  }

  @Transactional(readOnly = true)
  public List<CodeSnippetDto> listMine(long userId) {
    return snippetRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
        .map(CodeSnippetService::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public CodeSnippetDto getMine(long userId, long id) {
    CodeSnippet s = snippetRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new BusinessException(404, "片段不存在"));
    return toDto(s);
  }

  @Transactional
  public CodeSnippetDto create(long userId, CodeSnippetUpsertRequest req) {
    CodeSnippet s = new CodeSnippet();
    s.setUserId(userId);
    apply(s, req);
    s.setUpdatedAt(Instant.now());
    return toDto(snippetRepository.save(s));
  }

  @Transactional
  public CodeSnippetDto update(long userId, long id, CodeSnippetUpsertRequest req) {
    CodeSnippet s = snippetRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new BusinessException(404, "片段不存在"));
    apply(s, req);
    s.setUpdatedAt(Instant.now());
    return toDto(snippetRepository.save(s));
  }

  @Transactional
  public void delete(long userId, long id) {
    CodeSnippet s = snippetRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new BusinessException(404, "片段不存在"));
    snippetRepository.delete(s);
  }

  private static void apply(CodeSnippet s, CodeSnippetUpsertRequest req) {
    s.setTitle(req.title().trim());
    s.setLanguage(req.language().trim());
    s.setContent(req.content());
  }

  private static CodeSnippetDto toDto(CodeSnippet s) {
    return new CodeSnippetDto(s.getId(), s.getTitle(), s.getLanguage(), s.getContent(), s.getUpdatedAt());
  }
}
