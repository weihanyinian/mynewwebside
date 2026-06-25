package com.weihanyinian.website.module.visitor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_logs", indexes = {
    @Index(name = "idx_visit_time", columnList = "visitTime"),
    @Index(name = "idx_ip", columnList = "ip")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 45)
    private String ip;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 200)
    private String referer;

    @Column(length = 500)
    private String path;

    @Column(length = 10)
    private String method;

    @Column(nullable = false, updatable = false)
    private LocalDateTime visitTime;

    @PrePersist
    protected void onCreate() {
        visitTime = LocalDateTime.now();
    }
}
