package com.mywebside.blog.service;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.domain.WallMessage;
import com.mywebside.blog.domain.WallMessageStatus;
import com.mywebside.blog.dto.WallMessageAdminDto;
import com.mywebside.blog.dto.WallMessageCreateRequest;
import com.mywebside.blog.dto.WallMessagePublicDto;
import com.mywebside.blog.dto.WallMessageSubmitVo;
import com.mywebside.blog.repo.WallMessageRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WallMessageService {

  private final WallMessageRepository wallMessageRepository;

  public WallMessageService(WallMessageRepository wallMessageRepository) {
    this.wallMessageRepository = wallMessageRepository;
  }

  /** 前台：仅展示已通过审核的留言，按创建时间倒序，支持分页。 */
  public PageResponse<WallMessagePublicDto> listApproved(int page, int size) {
    PageRequest pr = PageRequest.of(page, size);
    Page<WallMessage> pg =
        wallMessageRepository.findByStatusOrderByCreatedAtDesc(WallMessageStatus.APPROVED, pr);
    List<WallMessagePublicDto> items = pg.getContent().stream()
        .map(m -> new WallMessagePublicDto(
            m.getId(),
            m.getNickname(),
            m.getContent(),
            m.getAdminReply(),
            m.getCreatedAt()))
        .toList();
    return new PageResponse<>(items, pg.getTotalElements(), page, size);
  }

  /** 访客提交：默认待审核。 */
  @Transactional
  public WallMessageSubmitVo submit(WallMessageCreateRequest req) {
    WallMessage m = new WallMessage();
    String nick = req.nickname() == null ? "" : req.nickname().trim();
    m.setNickname(nick.isEmpty() ? "匿名" : nick);
    m.setContent(req.content().trim());
    m.setStatus(WallMessageStatus.PENDING);
    m.setCreatedAt(Instant.now());
    wallMessageRepository.save(m);
    return new WallMessageSubmitVo(m.getId(), "PENDING");
  }

  /** 后台分页；status 为空表示全部。 */
  public PageResponse<WallMessageAdminDto> pageAdmin(WallMessageStatus status, int page, int size) {
    PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    Page<WallMessage> pg =
        status == null ? wallMessageRepository.findAll(pr) : wallMessageRepository.findByStatus(status, pr);
    List<WallMessageAdminDto> items = pg.getContent().stream()
        .map(m -> new WallMessageAdminDto(
            m.getId(),
            m.getNickname(),
            m.getContent(),
            m.getStatus(),
            m.getAdminReply(),
            m.getCreatedAt(),
            m.getReviewedAt()))
        .toList();
    return new PageResponse<>(items, pg.getTotalElements(), page, size);
  }

  @Transactional
  public void approve(Long id) {
    WallMessage m = wallMessageRepository.findById(id).orElseThrow(() -> new BusinessException(404, "留言不存在"));
    m.setStatus(WallMessageStatus.APPROVED);
    m.setReviewedAt(Instant.now());
  }

  @Transactional
  public void reject(Long id) {
    WallMessage m = wallMessageRepository.findById(id).orElseThrow(() -> new BusinessException(404, "留言不存在"));
    m.setStatus(WallMessageStatus.REJECTED);
    m.setReviewedAt(Instant.now());
  }

  @Transactional
  public void reply(Long id, String reply) {
    WallMessage m = wallMessageRepository.findById(id).orElseThrow(() -> new BusinessException(404, "留言不存在"));
    m.setAdminReply(reply.trim());
  }

  @Transactional
  public void delete(Long id) {
    if (!wallMessageRepository.existsById(id)) {
      throw new BusinessException(404, "留言不存在");
    }
    wallMessageRepository.deleteById(id);
  }
}
