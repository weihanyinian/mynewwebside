package com.weihanyinian.website.controller;

import com.weihanyinian.website.common.ApiResponse;
import com.weihanyinian.website.entity.Comment;
import com.weihanyinian.website.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<Comment> create(@RequestBody Comment comment) {
        Comment saved = commentService.createComment(comment);
        return ApiResponse.success("评论发表成功", saved);
    }

    @GetMapping
    public ApiResponse<List<Comment>> list(@RequestParam Long articleId) {
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        return ApiResponse.success(comments);
    }
}
