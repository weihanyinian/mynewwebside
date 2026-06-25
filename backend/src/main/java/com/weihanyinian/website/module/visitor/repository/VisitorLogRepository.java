package com.weihanyinian.website.module.visitor.repository;

import com.weihanyinian.website.module.visitor.entity.VisitorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {

    Page<VisitorLog> findAllByOrderByVisitTimeDesc(Pageable pageable);

    Page<VisitorLog> findByVisitTimeBetweenOrderByVisitTimeDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    long countByVisitTimeAfter(LocalDateTime since);

    long countByVisitTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT v.path, COUNT(v) FROM VisitorLog v WHERE v.visitTime BETWEEN :start AND :end GROUP BY v.path ORDER BY COUNT(v) DESC")
    List<Object[]> findTopPathsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT v.ip, COUNT(v) FROM VisitorLog v WHERE v.visitTime BETWEEN :start AND :end GROUP BY v.ip ORDER BY COUNT(v) DESC")
    List<Object[]> findTopIpsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
