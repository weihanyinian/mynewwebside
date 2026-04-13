package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.domain.Category;
import com.mywebside.blog.dto.CategoryDto;
import com.mywebside.blog.repo.CategoryRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepo;

  public CategoryService(CategoryRepository categoryRepo) {
    this.categoryRepo = categoryRepo;
  }

  @Transactional(readOnly = true)
  public List<CategoryDto> list() {
    return categoryRepo.findAll().stream()
        .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
        .map(c -> new CategoryDto(c.getId(), c.getName()))
        .toList();
  }

  @Transactional
  public CategoryDto create(String name) {
    categoryRepo.findByName(name).ifPresent(x -> {
      throw new BusinessException(409, "分类已存在");
    });
    Category c = new Category();
    c.setName(name);
    Instant now = Instant.now();
    c.setCreatedAt(now);
    c.setUpdatedAt(now);
    Category saved = categoryRepo.save(c);
    return new CategoryDto(saved.getId(), saved.getName());
  }

  @Transactional
  public CategoryDto update(long id, String name) {
    Category c = categoryRepo.findById(id).orElseThrow(() -> new BusinessException(404, "分类不存在"));
    categoryRepo.findByName(name).ifPresent(x -> {
      if (!x.getId().equals(c.getId())) {
        throw new BusinessException(409, "分类已存在");
      }
    });
    c.setName(name);
    c.setUpdatedAt(Instant.now());
    return new CategoryDto(c.getId(), c.getName());
  }

  @Transactional
  public void delete(long id) {
    if (!categoryRepo.existsById(id)) {
      return;
    }
    categoryRepo.deleteById(id);
  }
}
