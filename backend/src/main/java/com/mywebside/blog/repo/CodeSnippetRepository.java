package com.mywebsite.blog.repo;

import com.mywebsite.blog.domain.CodeSnippet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {
  List<CodeSnippet> findByUserIdOrderByUpdatedAtDesc(Long userId);

  Optional<CodeSnippet> findByIdAndUserId(long id, long userId);
}
