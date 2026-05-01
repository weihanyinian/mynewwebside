package com.mywebsite.blog.service;

import com.mywebsite.blog.common.BusinessException;
import com.mywebsite.blog.domain.PortfolioWork;
import com.mywebsite.blog.dto.PortfolioWorkAdminDto;
import com.mywebsite.blog.dto.PortfolioWorkDetailDto;
import com.mywebsite.blog.dto.PortfolioWorkPublicDto;
import com.mywebsite.blog.dto.PortfolioWorkUpsertRequest;
import com.mywebsite.blog.repo.PortfolioWorkRepository;
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
            w.getTag(),
            w.getCoverUrl()
        ))
        .toList();
  }

  @Cacheable("portfolioWorkDetail")
  @Transactional(readOnly = true)
  public PortfolioWorkDetailDto getPublicWorkDetail(long id) {
    PortfolioWork w = portfolioWorkRepository.findByIdAndEnabledTrue(id)
        .orElseThrow(() -> new BusinessException(404, "作品不存在或未发布"));
    return new PortfolioWorkDetailDto(
        w.getId(),
        w.getTitle(),
        w.getShortDesc(),
        w.getDetail(),
        w.getContentMd(),
        w.getTag(),
        w.getLink(),
        w.getDemoUrl(),
        w.getRepoUrl(),
        w.getTechStack(),
        w.getCoverUrl()
    );
  }

  @Transactional(readOnly = true)
  public List<PortfolioWorkAdminDto> listAdminWorks() {
    return portfolioWorkRepository.findAllByOrderBySortOrderAscIdAsc().stream()
        .map(this::toAdminDto)
        .toList();
  }

  @CacheEvict(value = {"portfolioWorks", "portfolioWorkDetail"}, allEntries = true)
  @Transactional
  public PortfolioWorkAdminDto create(PortfolioWorkUpsertRequest req) {
    PortfolioWork entity = new PortfolioWork();
    Instant now = Instant.now();
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    applyUpsert(entity, req);
    return toAdminDto(portfolioWorkRepository.save(entity));
  }

  @CacheEvict(value = {"portfolioWorks", "portfolioWorkDetail"}, allEntries = true)
  @Transactional
  public PortfolioWorkAdminDto update(long id, PortfolioWorkUpsertRequest req) {
    PortfolioWork entity = portfolioWorkRepository.findById(id)
        .orElseThrow(() -> new BusinessException(404, "作品不存在"));
    applyUpsert(entity, req);
    entity.setUpdatedAt(Instant.now());
    return toAdminDto(entity);
  }

  @CacheEvict(value = {"portfolioWorks", "portfolioWorkDetail"}, allEntries = true)
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
    entity.setContentMd(req.contentMd().trim());
    entity.setTag(req.tag().trim());
    entity.setLink(req.link().trim());
    entity.setDemoUrl(trimToNull(req.demoUrl()));
    entity.setRepoUrl(trimToNull(req.repoUrl()));
    entity.setTechStack(trimToNull(req.techStack()));
    entity.setCoverUrl(req.cover().trim());
    entity.setEnabled(Boolean.TRUE.equals(req.enabled()));
    entity.setSortOrder(req.sortOrder());
  }

  private String trimToNull(String raw) {
    if (raw == null) return null;
    String v = raw.trim();
    return v.isEmpty() ? null : v;
  }

  private PortfolioWorkAdminDto toAdminDto(PortfolioWork w) {
    return new PortfolioWorkAdminDto(
        w.getId(),
        w.getTitle(),
        w.getShortDesc(),
        w.getDetail(),
        w.getContentMd(),
        w.getTag(),
        w.getLink(),
        w.getDemoUrl(),
        w.getRepoUrl(),
        w.getTechStack(),
        w.getCoverUrl(),
        w.isEnabled(),
        w.getSortOrder(),
        w.getCreatedAt(),
        w.getUpdatedAt()
    );
  }
}
