package com.mywebside.blog.persistence.mapper;

import com.mywebside.blog.persistence.entity.OjSubmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OjSubmissionEntityMapper extends JpaRepository<OjSubmissionEntity, Long> {

  long countByUserId(Long userId);

  Page<OjSubmissionEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
