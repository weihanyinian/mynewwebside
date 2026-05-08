package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.domain.FriendLink;
import com.mywebside.blog.dto.FriendLinkPublicDto;
import com.mywebside.blog.dto.FriendLinkUpsertRequest;
import com.mywebside.blog.repo.FriendLinkRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendLinkService {

  private final FriendLinkRepository friendLinkRepository;

  public FriendLinkService(FriendLinkRepository friendLinkRepository) {
    this.friendLinkRepository = friendLinkRepository;
  }

  @Cacheable("friends")
  @Transactional(readOnly = true)
  public List<FriendLinkPublicDto> listPublic() {
    return friendLinkRepository.findAllByOrderBySortOrderAscIdAsc().stream()
        .map(f -> new FriendLinkPublicDto(
            f.getId(),
            f.getTitle(),
            f.getUrl(),
            f.getDescription() == null ? "" : f.getDescription(),
            f.getAvatarUrl() == null ? "" : f.getAvatarUrl()
        ))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FriendLink> listAllEntities() {
    return friendLinkRepository.findAllByOrderBySortOrderAscIdAsc();
  }

  @CacheEvict(value = "friends", allEntries = true)
  @Transactional
  public FriendLink create(FriendLinkUpsertRequest req) {
    FriendLink f = new FriendLink();
    f.setCreatedAt(Instant.now());
    apply(f, req);
    return friendLinkRepository.save(f);
  }

  @CacheEvict(value = "friends", allEntries = true)
  @Transactional
  public FriendLink update(long id, FriendLinkUpsertRequest req) {
    FriendLink f = friendLinkRepository.findById(id)
        .orElseThrow(() -> new BusinessException(404, "友链不存在"));
    apply(f, req);
    return friendLinkRepository.save(f);
  }

  @CacheEvict(value = "friends", allEntries = true)
  @Transactional
  public void delete(long id) {
    if (!friendLinkRepository.existsById(id)) {
      throw new BusinessException(404, "友链不存在");
    }
    friendLinkRepository.deleteById(id);
  }

  private static void apply(FriendLink f, FriendLinkUpsertRequest req) {
    f.setTitle(req.title().trim());
    f.setUrl(req.url().trim());
    f.setDescription(req.description() == null ? "" : req.description().trim());
    f.setAvatarUrl(req.avatarUrl() == null ? "" : req.avatarUrl().trim());
    f.setSortOrder(req.sortOrder());
  }
}
