package com.weihanyinian.website.module.admin.controller;

import com.weihanyinian.website.common.ApiResponse;
import com.weihanyinian.website.config.JwtTokenProvider;
import com.weihanyinian.website.module.admin.dto.DashboardStats;
import com.weihanyinian.website.module.admin.dto.LoginRequest;
import com.weihanyinian.website.module.admin.dto.LoginResponse;
import com.weihanyinian.website.module.admin.service.AdminService;
import com.weihanyinian.website.module.visitor.entity.VisitorLog;
import com.weihanyinian.website.module.visitor.repository.VisitorLogRepository;
import com.weihanyinian.website.module.blog.entity.Article;
import com.weihanyinian.website.module.blog.repository.ArticleRepository;
import com.weihanyinian.website.module.guestbook.entity.Guestbook;
import com.weihanyinian.website.module.guestbook.repository.GuestbookRepository;
import com.weihanyinian.website.module.user.entity.User;
import com.weihanyinian.website.module.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final VisitorLogRepository visitorLogRepository;
    private final ArticleRepository articleRepository;
    private final GuestbookRepository guestbookRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminController(AdminService adminService, VisitorLogRepository visitorLogRepository,
                           ArticleRepository articleRepository, GuestbookRepository guestbookRepository,
                           UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.adminService = adminService;
        this.visitorLogRepository = visitorLogRepository;
        this.articleRepository = articleRepository;
        this.guestbookRepository = guestbookRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = adminService.login(request);
            return ApiResponse.success("登录成功", response);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.error(401, "用户不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return ApiResponse.success(result);
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStats> dashboard() {
        return ApiResponse.success(adminService.getDashboardStats());
    }

    // ─── Visitor logs ───

    @GetMapping("/visitors")
    public ApiResponse<Page<VisitorLog>> visitors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "visitTime"));
        return ApiResponse.success(visitorLogRepository.findAllByOrderByVisitTimeDesc(pageable));
    }

    @GetMapping("/visitors/stats")
    public ApiResponse<DashboardStats> visitorStats() {
        return ApiResponse.success(adminService.getDashboardStats());
    }

    // ─── Article management ───

    @GetMapping("/articles")
    public ApiResponse<Page<Article>> articles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(articleRepository.findAll(pageable));
    }

    @PostMapping("/articles")
    public ApiResponse<Article> createArticle(@RequestBody Article article) {
        if (article.getStatus() == null) {
            article.setStatus(Article.ArticleStatus.DRAFT);
        }
        Article saved = articleRepository.save(article);
        return ApiResponse.success("文章创建成功", saved);
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        Article existing = articleRepository.findById(id).orElse(null);
        if (existing == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        existing.setTitle(article.getTitle());
        existing.setSummary(article.getSummary());
        existing.setContent(article.getContent());
        existing.setCoverImage(article.getCoverImage());
        existing.setStatus(article.getStatus());
        existing.setCategory(article.getCategory());
        existing.setTags(article.getTags());
        Article saved = articleRepository.save(existing);
        return ApiResponse.success("文章更新成功", saved);
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        if (!articleRepository.existsById(id)) {
            return ApiResponse.error(404, "文章不存在");
        }
        articleRepository.deleteById(id);
        return ApiResponse.success("文章删除成功", null);
    }

    // ─── Guestbook management ───

    @GetMapping("/guestbooks")
    public ApiResponse<Page<Guestbook>> guestbooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(guestbookRepository.findAll(pageable));
    }

    @DeleteMapping("/guestbooks/{id}")
    public ApiResponse<Void> deleteGuestbook(@PathVariable Long id) {
        if (!guestbookRepository.existsById(id)) {
            return ApiResponse.error(404, "留言不存在");
        }
        guestbookRepository.deleteById(id);
        return ApiResponse.success("留言删除成功", null);
    }
}
