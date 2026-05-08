package com.mywebside.blog.repo;

import com.mywebside.blog.domain.WallMessage;
import com.mywebside.blog.domain.WallMessageStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WallMessageRepository extends JpaRepository<WallMessage, Long> {

  List<WallMessage> findByStatusOrderByIdDesc(WallMessageStatus status);

  Page<WallMessage> findByStatus(WallMessageStatus status, Pageable pageable);

  Page<WallMessage> findByStatusOrderByCreatedAtDesc(WallMessageStatus status, Pageable pageable);
}
