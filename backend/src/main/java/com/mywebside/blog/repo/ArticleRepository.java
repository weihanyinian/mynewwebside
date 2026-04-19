package com.mywebside.blog.repo;

import com.mywebside.blog.domain.Article;
import com.mywebside.blog.domain.ArticleStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
  @EntityGraph(attributePaths = {"category", "tags"})
  @Query("""
      select distinct a from Article a
      left join a.tags t
      where a.status = 'PUBLISHED'
        and (:categoryId is null or a.category.id = :categoryId)
        and (:tagId is null or t.id = :tagId)
        and (:keyword is null or a.title like concat('%', :keyword, '%')
             or a.summary like concat('%', :keyword, '%'))
      order by a.publishedAt desc, a.id desc
      """)
  Page<Article> pagePublic(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId,
      @Param("tagId") Long tagId,
      Pageable pageable
  );

  @EntityGraph(attributePaths = {"category", "tags"})
  @Query("""
      select distinct a from Article a
      left join a.tags t
      where a.deletedAt is null
        and (:status is null or a.status = :status)
        and (:keyword is null or a.title like concat('%', :keyword, '%')
             or a.summary like concat('%', :keyword, '%'))
      order by a.updatedAt desc, a.id desc
      """)
  Page<Article> pageAdmin(
      @Param("keyword") String keyword,
      @Param("status") ArticleStatus status,
      Pageable pageable
  );

  @EntityGraph(attributePaths = {"category", "tags"})
  @Query("select a from Article a where a.id = :id")
  Article getWithAllById(@Param("id") Long id);

  @Transactional
  @Modifying
  @Query("update Article a set a.views = a.views + 1 where a.id = :id")
  int incViews(@Param("id") Long id);

  @Transactional
  @Modifying
  @Query("update Article a set a.views = a.views + :delta where a.id = :id")
  int incrementViewsBy(@Param("id") long id, @Param("delta") long delta);
}
