package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.domain.Article;
import com.mywebside.blog.domain.Comment;
import com.mywebside.blog.dto.CommentCreateRequest;
import com.mywebside.blog.dto.CommentDto;
import com.mywebside.blog.repo.ArticleRepository;
import com.mywebside.blog.repo.CommentRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
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
        return commentRepository.findByArticleIdOrderByCreateTimeDesc(articleId).stream()
                .map(c -> new CommentDto(c.getId(), c.getAuthor(), c.getContent(), c.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto createComment(CommentCreateRequest req) {
        Article article = articleRepository.findById(req.articleId())
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        
        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthor((req.author() == null || req.author().isBlank()) ? "匿名魔法使" : req.author().trim());
        comment.setContent(req.content());
        comment.setCreateTime(Instant.now());
        
        comment = commentRepository.save(comment);
        
        return new CommentDto(comment.getId(), comment.getAuthor(), comment.getContent(), comment.getCreateTime());
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new BusinessException(404, "评论不存在");
        }
        commentRepository.deleteById(id);
    }
}