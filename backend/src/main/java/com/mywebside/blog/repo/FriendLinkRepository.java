package com.mywebside.blog.repo;

import com.mywebside.blog.domain.FriendLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendLinkRepository extends JpaRepository<FriendLink, Long> {
  List<FriendLink> findAllByOrderBySortOrderAscIdAsc();
}
