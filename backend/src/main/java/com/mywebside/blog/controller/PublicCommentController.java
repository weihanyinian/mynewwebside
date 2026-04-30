package com.mywebsite.blog.controller;

import com.mywebsite.blog.common.ApiResponse;
import com.mywebsite.blog.dto.CommentCreateRequest;
import com.mywebsite.blog.dto.CommentDto;
import com.mywebsite.blog.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/comments")
public class PublicCommentController {
    private final CommentService commentService;

    public PublicCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/article/{articleId}")
    public ApiResponse<List<CommentDto>> getComments(@PathVariable Long articleId) {
        return ApiResponse.ok(commentService.getCommentsByArticle(articleId));
    }

    @PostMapping
    public ApiResponse<CommentDto> addComment(@Valid @RequestBody CommentCreateRequest req) {
        return ApiResponse.ok(commentService.createComment(req));
    }
}