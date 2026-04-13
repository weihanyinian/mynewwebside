package com.mywebside.blog.repo;

import com.mywebside.blog.domain.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
  Optional<Tag> findByName(String name);
}
