package com.mywebsite.blog.persistence.mapper;

import com.mywebsite.blog.persistence.entity.OjSubmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OjSubmissionEntityMapper extends JpaRepository<OjSubmissionEntity, Long> {

  long countByUserId(Long userId);

  Page<OjSubmissionEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
