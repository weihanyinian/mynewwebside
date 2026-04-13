package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.domain.Article;
import com.mywebside.blog.domain.ArticleStatus;
import com.mywebside.blog.dto.AdminArticleListItemDto;
import com.mywebside.blog.dto.ArticleDetailDto;
import com.mywebside.blog.dto.ArticleListItemDto;
import com.mywebside.blog.dto.ArticleUpsertRequest;
import com.mywebside.blog.dto.CategoryDto;
import com.mywebside.blog.dto.TagDto;
import com.mywebside.blog.repo.ArticleRepository;
import com.mywebside.blog.repo.CategoryRepository;
import com.mywebside.blog.repo.TagRepository;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleService {
  private final ArticleRepository articleRepo;
  private final CategoryRepository categoryRepo;
  private final TagRepository tagRepo;

  public ArticleService(ArticleRepository articleRepo, CategoryRepository categoryRepo, TagRepository tagRepo) {
    this.articleRepo = articleRepo;
    this.categoryRepo = categoryRepo;
    this.tagRepo = tagRepo;
  }

  @Transactional(readOnly = true)
  public PageResponse<ArticleListItemDto> pagePublic(String keyword, Long categoryId, Long tagId, int page, int size) {
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt", "id"));
    var p = articleRepo.pagePublic(blankToNull(keyword), categoryId, tagId, pageable);
    return new PageResponse<>(
        p.getContent().stream().map(this::toPublicListItem).toList(),
        p.getTotalElements(),
        page,
        size
    );
  }

  @Transactional
  public ArticleDetailDto getPublicDetail(long id) {
    articleRepo.incViews(id);
    Article a = articleRepo.getWithAllById(id);
    if (a == null || a.getStatus() != ArticleStatus.PUBLISHED) {
      throw new BusinessException(404, "文章不存在");
    }
    return toPublicDetail(a);
  }

  @Transactional(readOnly = true)
  public PageResponse<AdminArticleListItemDto> pageAdmin(String keyword, String status, int page, int size) {
    ArticleStatus st = status == null ? null : ArticleStatus.valueOf(status.toUpperCase());
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "id"));
    var p = articleRepo.pageAdmin(blankToNull(keyword), st, pageable);
    return new PageResponse<>(
        p.getContent().stream().map(this::toAdminListItem).toList(),
        p.getTotalElements(),
        page,
        size
    );
  }

  @Transactional(readOnly = true)
  public ArticleDetailDto getAdminDetail(long id) {
    Article a = articleRepo.getWithAllById(id);
    if (a == null) {
      throw new BusinessException(404, "文章不存在");
    }
    return toPublicDetail(a);
  }

  @Transactional
  public ArticleDetailDto create(ArticleUpsertRequest req) {
    Article a = new Article();
    applyUpsert(a, req);
    Instant now = Instant.now();
    a.setCreatedAt(now);
    a.setUpdatedAt(now);
    if (a.getStatus() == ArticleStatus.PUBLISHED) {
      a.setPublishedAt(now);
    }
    Article saved = articleRepo.save(a);
    return toPublicDetail(saved);
  }

  @Transactional
  public ArticleDetailDto update(long id, ArticleUpsertRequest req) {
    Article a = articleRepo.getWithAllById(id);
    if (a == null) {
      throw new BusinessException(404, "文章不存在");
    }
    ArticleStatus before = a.getStatus();
    applyUpsert(a, req);
    a.setUpdatedAt(Instant.now());
    if (before != ArticleStatus.PUBLISHED && a.getStatus() == ArticleStatus.PUBLISHED) {
      a.setPublishedAt(Instant.now());
    }
    return toPublicDetail(a);
  }

  @Transactional
  public void delete(long id) {
    if (!articleRepo.existsById(id)) {
      return;
    }
    articleRepo.deleteById(id);
  }

  private void applyUpsert(Article a, ArticleUpsertRequest req) {
    a.setTitle(req.title());
    a.setSummary(req.summary());
    a.setContentMd(req.contentMd());
    a.setCoverUrl(req.coverUrl());
    a.setStatus(ArticleStatus.valueOf(req.status().toUpperCase()));

    if (req.categoryId() == null) {
      a.setCategory(null);
    } else {
      a.setCategory(categoryRepo.findById(req.categoryId())
          .orElseThrow(() -> new BusinessException(404, "分类不存在")));
    }

    Set<Long> tagIds = req.tagIds() == null ? Set.of() : new LinkedHashSet<>(req.tagIds());
    if (tagIds.isEmpty()) {
      a.setTags(Set.of());
    } else {
      var tags = tagRepo.findAllById(tagIds);
      if (tags.size() != tagIds.size()) {
        throw new BusinessException(404, "部分标签不存在");
      }
      a.setTags(new LinkedHashSet<>(tags));
    }
  }

  private ArticleListItemDto toPublicListItem(Article a) {
    return new ArticleListItemDto(
        a.getId(),
        a.getTitle(),
        a.getSummary(),
        a.getCoverUrl(),
        a.getViews(),
        a.getPublishedAt(),
        a.getCategory() == null ? null : new CategoryDto(a.getCategory().getId(), a.getCategory().getName()),
        a.getTags().stream()
            .sorted((x, y) -> Long.compare(x.getId(), y.getId()))
            .map(t -> new TagDto(t.getId(), t.getName()))
            .toList()
    );
  }

  private AdminArticleListItemDto toAdminListItem(Article a) {
    return new AdminArticleListItemDto(
        a.getId(),
        a.getTitle(),
        a.getSummary(),
        a.getCoverUrl(),
        a.getStatus().name(),
        a.getViews(),
        a.getCreatedAt(),
        a.getUpdatedAt(),
        a.getPublishedAt(),
        a.getCategory() == null ? null : new CategoryDto(a.getCategory().getId(), a.getCategory().getName()),
        a.getTags().stream()
            .sorted((x, y) -> Long.compare(x.getId(), y.getId()))
            .map(t -> new TagDto(t.getId(), t.getName()))
            .toList()
    );
  }

  private ArticleDetailDto toPublicDetail(Article a) {
    return new ArticleDetailDto(
        a.getId(),
        a.getTitle(),
        a.getSummary(),
        a.getCoverUrl(),
        a.getStatus().name(),
        a.getContentMd(),
        a.getViews(),
        a.getPublishedAt(),
        a.getCategory() == null ? null : new CategoryDto(a.getCategory().getId(), a.getCategory().getName()),
        a.getTags().stream()
            .sorted((x, y) -> Long.compare(x.getId(), y.getId()))
            .map(t -> new TagDto(t.getId(), t.getName()))
            .toList()
    );
  }

  private static String blankToNull(String s) {
    if (s == null) {
      return null;
    }
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}
