package com.mywebside.blog.repo;

import com.mywebside.blog.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleIdOrderByCreateTimeDesc(Long articleId);

    @Query("""
        select c from Comment c
        join c.article a
        where (:articleId is null or a.id = :articleId)
          and (:keyword is null or lower(c.content) like lower(concat('%', :keyword, '%'))
              or lower(c.author) like lower(concat('%', :keyword, '%'))
              or lower(a.title) like lower(concat('%', :keyword, '%')))
        """)
    Page<Comment> pageAdmin(@Param("keyword") String keyword, @Param("articleId") Long articleId, Pageable pageable);
}