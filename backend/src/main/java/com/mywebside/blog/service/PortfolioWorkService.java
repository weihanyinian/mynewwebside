package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.domain.PortfolioWork;
import com.mywebside.blog.dto.PortfolioWorkAdminDto;
import com.mywebside.blog.dto.PortfolioWorkPublicDto;
import com.mywebside.blog.dto.PortfolioWorkUpsertRequest;
import com.mywebside.blog.repo.PortfolioWorkRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioWorkService {

  private final PortfolioWorkRepository portfolioWorkRepository;

  public PortfolioWorkService(PortfolioWorkRepository portfolioWorkRepository) {
    this.portfolioWorkRepository = portfolioWorkRepository;
  }

  @Cacheable("portfolioWorks")
  @Transactional(readOnly = true)
  public List<PortfolioWorkPublicDto> listPublicWorks() {
    return portfolioWorkRepository.findAllByEnabledTrueOrderBySortOrderAscIdAsc().stream()
        .map(w -> new PortfolioWorkPublicDto(
            w.getId(),
            w.getTitle(),
            w.getShortDesc(),
            w.getDetail(),
            w.getTag(),
            w.getLink(),
            w.getCoverUrl()
        ))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PortfolioWorkAdminDto> listAdminWorks() {
    return portfolioWorkRepository.findAllByOrderBySortOrderAscIdAsc().stream()
        .map(this::toAdminDto)
        .toList();
  }

  @CacheEvict(value = "portfolioWorks", allEntries = true)
  @Transactional
  public PortfolioWorkAdminDto create(PortfolioWorkUpsertRequest req) {
    PortfolioWork entity = new PortfolioWork();
    Instant now = Instant.now();
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    applyUpsert(entity, req);
    return toAdminDto(portfolioWorkRepository.save(entity));
  }

  @CacheEvict(value = "portfolioWorks", allEntries = true)
  @Transactional
  public PortfolioWorkAdminDto update(long id, PortfolioWorkUpsertRequest req) {
    PortfolioWork entity = portfolioWorkRepository.findById(id)
        .orElseThrow(() -> new BusinessException(404, "作品不存在"));
    applyUpsert(entity, req);
    entity.setUpdatedAt(Instant.now());
    return toAdminDto(entity);
  }

  @CacheEvict(value = "portfolioWorks", allEntries = true)
  @Transactional
  public void delete(long id) {
    if (!portfolioWorkRepository.existsById(id)) {
      throw new BusinessException(404, "作品不存在");
    }
    portfolioWorkRepository.deleteById(id);
  }

  private void applyUpsert(PortfolioWork entity, PortfolioWorkUpsertRequest req) {
    entity.setTitle(req.title().trim());
    entity.setShortDesc(req.desc().trim());
    entity.setDetail(req.detail().trim());
    entity.setTag(req.tag().trim());
    entity.setLink(req.link().trim());
    entity.setCoverUrl(req.cover().trim());
    entity.setEnabled(Boolean.TRUE.equals(req.enabled()));
    entity.setSortOrder(req.sortOrder());
  }

  private PortfolioWorkAdminDto toAdminDto(PortfolioWork w) {
    return new PortfolioWorkAdminDto(
        w.getId(),
        w.getTitle(),
        w.getShortDesc(),
        w.getDetail(),
        w.getTag(),
        w.getLink(),
        w.getCoverUrl(),
        w.isEnabled(),
        w.getSortOrder(),
        w.getCreatedAt(),
        w.getUpdatedAt()
    );
  }
}
