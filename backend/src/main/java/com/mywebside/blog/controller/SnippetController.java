package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.dto.CodeSnippetDto;
import com.mywebside.blog.dto.CodeSnippetUpsertRequest;
import com.mywebside.blog.service.CodeSnippetService;
import com.mywebside.blog.service.UserAccountService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/snippets")
public class SnippetController {

  private final CodeSnippetService snippetService;
  private final UserAccountService userAccountService;

  public SnippetController(CodeSnippetService snippetService, UserAccountService userAccountService) {
    this.snippetService = snippetService;
    this.userAccountService = userAccountService;
  }

  @GetMapping
  public ApiResponse<List<CodeSnippetDto>> list(Authentication auth) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(snippetService.listMine(uid));
  }

  @GetMapping("/{id}")
  public ApiResponse<CodeSnippetDto> get(Authentication auth, @PathVariable long id) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(snippetService.getMine(uid, id));
  }

  @PostMapping
  public ApiResponse<CodeSnippetDto> create(
      Authentication auth,
      @Valid @RequestBody CodeSnippetUpsertRequest body
  ) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(snippetService.create(uid, body));
  }

  @PutMapping("/{id}")
  public ApiResponse<CodeSnippetDto> update(
      Authentication auth,
      @PathVariable long id,
      @Valid @RequestBody CodeSnippetUpsertRequest body
  ) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(snippetService.update(uid, id, body));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(Authentication auth, @PathVariable long id) {
    long uid = userAccountService.requireUserId(auth.getName());
    snippetService.delete(uid, id);
    return ApiResponse.ok();
  }
}
