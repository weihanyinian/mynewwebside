package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.domain.ArticleLike;
import com.mywebside.blog.repo.ArticleLikeRepository;
import com.mywebside.blog.repo.ArticleRepository;
import com.mywebside.blog.persistence.mapper.UserEntityMapper;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article")
public class LikeController {
  private final ArticleLikeRepository likeRepo;
  private final ArticleRepository articleRepo;
  private final UserEntityMapper userMapper;

  public LikeController(ArticleLikeRepository likeRepo, ArticleRepository articleRepo, UserEntityMapper userMapper) {
    this.likeRepo = likeRepo; this.articleRepo = articleRepo; this.userMapper = userMapper;
  }

  @GetMapping("/{id}/like-status")
  public ApiResponse<Map<String, Object>> status(@PathVariable Long id, Principal principal) {
    long count = likeRepo.countByArticleId(id);
    boolean liked = principal != null && likeRepo.existsByArticleIdAndUserId(id, resolveUserId(principal));
    return ApiResponse.ok(Map.of("count", count, "liked", liked));
  }

  @PostMapping("/{id}/like")
  public ApiResponse<Map<String, Object>> toggle(@PathVariable Long id, Principal principal) {
    if (principal == null) throw new BusinessException(401, "请先登录");
    Long userId = resolveUserId(principal);
    if (!articleRepo.existsById(id)) throw new BusinessException(404, "文章不存在");
    var existing = likeRepo.findByArticleIdAndUserId(id, userId);
    if (existing.isPresent()) {
      likeRepo.delete(existing.get());
    } else {
      likeRepo.save(new ArticleLike(id, userId));
    }
    long count = likeRepo.countByArticleId(id);
    return ApiResponse.ok(Map.of("count", count, "liked", existing.isEmpty()));
  }

  private Long resolveUserId(Principal principal) {
    return userMapper.findByUsername(principal.getName())
        .orElseThrow(() -> new BusinessException(401, "用户不存在")).getId();
  }
}
