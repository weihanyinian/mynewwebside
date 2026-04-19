package com.mywebside.blog.persistence.mapper;

import com.mywebside.blog.persistence.entity.OjProblemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OjProblemEntityMapper extends JpaRepository<OjProblemEntity, String> {

  List<OjProblemEntity> findAllByOrderByIdAsc();
}
