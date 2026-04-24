package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.dto.AdminCommentListItemDto;
import com.mywebside.blog.dto.CommentDto;
import com.mywebside.blog.dto.CommentUpdateRequest;
import com.mywebside.blog.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminCommentListItemDto>> page(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long articleId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(commentService.pageAdmin(keyword, articleId, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<CommentDto> updateComment(@PathVariable Long id, @Valid @RequestBody CommentUpdateRequest req) {
        return ApiResponse.ok(commentService.updateComment(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ApiResponse.ok();
    }
}