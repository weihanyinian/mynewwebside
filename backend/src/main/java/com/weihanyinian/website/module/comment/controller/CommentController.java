package com.weihanyinian.website.module.comment.controller;

import com.weihanyinian.website.common.ApiResponse;
import com.weihanyinian.website.module.comment.dto.CommentRequest;
import com.weihanyinian.website.module.comment.entity.Comment;
import com.weihanyinian.website.module.comment.service.CommentService;
import com.weihanyinian.website.module.blog.entity.Article;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<Comment> create(@Valid @RequestBody CommentRequest request) {
        Comment comment = Comment.builder()
                .article(Article.builder().id(request.getArticleId()).build())
                .nickname(request.getNickname().trim())
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .content(request.getContent().trim())
                .parent(request.getParentId() != null ? Comment.builder().id(request.getParentId()).build() : null)
                .build();
        Comment saved = commentService.createComment(comment);
        return ApiResponse.success("评论发表成功", saved);
    }

    @GetMapping
    public ApiResponse<List<Comment>> list(@RequestParam Long articleId) {
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        return ApiResponse.success(comments);
    }
}
