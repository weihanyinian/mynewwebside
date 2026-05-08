package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.domain.Tag;
import com.mywebside.blog.dto.TagDto;
import com.mywebside.blog.repo.TagRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {
  private final TagRepository tagRepo;

  public TagService(TagRepository tagRepo) {
    this.tagRepo = tagRepo;
  }

  @Cacheable("tags")
  @Transactional(readOnly = true)
  public List<TagDto> list() {
    return tagRepo.findAll().stream()
        .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
        .map(t -> new TagDto(t.getId(), t.getName()))
        .toList();
  }

  @CacheEvict(value = "tags", allEntries = true)
  @Transactional
  public TagDto create(String name) {
    tagRepo.findByName(name).ifPresent(x -> {
      throw new BusinessException(409, "标签已存在");
    });
    Tag t = new Tag();
    t.setName(name);
    Instant now = Instant.now();
    t.setCreatedAt(now);
    t.setUpdatedAt(now);
    Tag saved = tagRepo.save(t);
    return new TagDto(saved.getId(), saved.getName());
  }

  @CacheEvict(value = "tags", allEntries = true)
  @Transactional
  public TagDto update(long id, String name) {
    Tag t = tagRepo.findById(id).orElseThrow(() -> new BusinessException(404, "标签不存在"));
    tagRepo.findByName(name).ifPresent(x -> {
      if (!x.getId().equals(t.getId())) {
        throw new BusinessException(409, "标签已存在");
      }
    });
    t.setName(name);
    t.setUpdatedAt(Instant.now());
    return new TagDto(t.getId(), t.getName());
  }

  @CacheEvict(value = "tags", allEntries = true)
  @Transactional
  public void delete(long id) {
    if (!tagRepo.existsById(id)) {
      return;
    }
    tagRepo.deleteById(id);
  }
}
