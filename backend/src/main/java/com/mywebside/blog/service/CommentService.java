package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.domain.Article;
import com.mywebside.blog.domain.Comment;
import com.mywebside.blog.dto.AdminCommentListItemDto;
import com.mywebside.blog.dto.CommentCreateRequest;
import com.mywebside.blog.dto.CommentDto;
import com.mywebside.blog.dto.CommentUpdateRequest;
import com.mywebside.blog.repo.ArticleRepository;
import com.mywebside.blog.repo.CommentRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    public CommentService(CommentRepository commentRepository, ArticleRepository articleRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
    }

    public List<CommentDto> getCommentsByArticle(Long articleId) {
        List<Comment> all = commentRepository.findByArticleIdOrderByCreateTimeDesc(articleId);
        List<Comment> topLevel = all.stream().filter(c -> c.getParentId() == null).toList();
        return topLevel.stream()
            .map(c -> toNestedDto(c, all))
            .collect(Collectors.toList());
    }

    private CommentDto toNestedDto(Comment c, List<Comment> all) {
        List<CommentDto> replies = all.stream()
            .filter(r -> c.getId().equals(r.getParentId()))
            .map(r -> toNestedDto(r, all))
            .collect(Collectors.toList());
        return new CommentDto(c.getId(), c.getAuthor(), c.getContent(), c.getCreateTime(), c.getParentId(), replies);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminCommentListItemDto> pageAdmin(String keyword, Long articleId, int page, int size) {
        page = Math.max(0, page);
        size = (size < 1 || size > 100) ? 10 : size;
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime", "id"));
        var rows = commentRepository.pageAdmin(blankToNull(keyword), articleId, pageable);
        return new PageResponse<>(
                rows.getContent().stream()
                        .map(c -> new AdminCommentListItemDto(
                                c.getId(),
                                c.getArticle().getId(),
                                c.getArticle().getTitle(),
                                c.getAuthor(),
                                c.getContent(),
                                c.getCreateTime()
                        ))
                        .toList(),
                rows.getTotalElements(),
                page,
                size
        );
    }

    @Transactional
    public CommentDto createComment(CommentCreateRequest req) {
        Article article = articleRepository.findById(req.articleId())
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        
        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthor((req.author() == null || req.author().isBlank()) ? "匿名用户" : req.author().trim());
        comment.setContent(req.content());
        comment.setParentId(req.parentId());
        comment.setCreateTime(Instant.now());

        comment = commentRepository.save(comment);

        return CommentDto.flat(comment.getId(), comment.getAuthor(), comment.getContent(), comment.getCreateTime(), comment.getParentId());
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new BusinessException(404, "评论不存在");
        }
        commentRepository.deleteById(id);
    }

    @Transactional
    public CommentDto updateComment(Long id, CommentUpdateRequest req) {
        Comment c = commentRepository.findById(id).orElseThrow(() -> new BusinessException(404, "评论不存在"));
        c.setAuthor(req.author().trim());
        c.setContent(req.content().trim());
        return CommentDto.flat(c.getId(), c.getAuthor(), c.getContent(), c.getCreateTime(), c.getParentId());
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}