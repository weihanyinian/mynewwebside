package com.weihanyinian.website.module.admin.service;

import com.weihanyinian.website.config.JwtTokenProvider;
import com.weihanyinian.website.module.admin.dto.DashboardStats;
import com.weihanyinian.website.module.admin.dto.LoginRequest;
import com.weihanyinian.website.module.admin.dto.LoginResponse;
import com.weihanyinian.website.module.blog.repository.ArticleRepository;
import com.weihanyinian.website.module.guestbook.repository.GuestbookRepository;
import com.weihanyinian.website.module.comment.repository.CommentRepository;
import com.weihanyinian.website.module.user.entity.User;
import com.weihanyinian.website.module.user.repository.UserRepository;
import com.weihanyinian.website.module.visitor.repository.VisitorLogRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ArticleRepository articleRepository;
    private final GuestbookRepository guestbookRepository;
    private final CommentRepository commentRepository;
    private final VisitorLogRepository visitorLogRepository;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider, ArticleRepository articleRepository,
                        GuestbookRepository guestbookRepository, CommentRepository commentRepository,
                        VisitorLogRepository visitorLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.articleRepository = articleRepository;
        this.guestbookRepository = guestbookRepository;
        this.commentRepository = commentRepository;
        this.visitorLogRepository = visitorLogRepository;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("无管理员权限");
        }

        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    public DashboardStats getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime weekStart = todayStart.minusDays(6);

        long articleCount = articleRepository.count();
        long guestbookCount = guestbookRepository.count();
        long commentCount = commentRepository.count();
        long todayVisits = visitorLogRepository.countByVisitTimeAfter(todayStart);
        long weekVisits = visitorLogRepository.countByVisitTimeAfter(weekStart);
        long totalVisits = visitorLogRepository.count();

        // Top paths (last 7 days)
        List<Object[]> topPathsRaw = visitorLogRepository.findTopPathsBetween(weekStart, now);
        List<Map<String, Object>> topPaths = new ArrayList<>();
        int pathLimit = Math.min(topPathsRaw.size(), 10);
        for (int i = 0; i < pathLimit; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("path", topPathsRaw.get(i)[0]);
            item.put("count", topPathsRaw.get(i)[1]);
            topPaths.add(item);
        }

        // Top IPs (last 7 days)
        List<Object[]> topIpsRaw = visitorLogRepository.findTopIpsBetween(weekStart, now);
        List<Map<String, Object>> topIps = new ArrayList<>();
        int ipLimit = Math.min(topIpsRaw.size(), 10);
        for (int i = 0; i < ipLimit; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ip", topIpsRaw.get(i)[0]);
            item.put("count", topIpsRaw.get(i)[1]);
            topIps.add(item);
        }

        // Daily visits (last 7 days)
        List<Map<String, Object>> dailyVisits = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(day, LocalTime.MAX);
            long count = visitorLogRepository.countByVisitTimeBetween(dayStart, dayEnd);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", day.toString());
            item.put("count", count);
            dailyVisits.add(item);
        }

        return DashboardStats.builder()
                .articleCount(articleCount)
                .guestbookCount(guestbookCount)
                .commentCount(commentCount)
                .todayVisits(todayVisits)
                .weekVisits(weekVisits)
                .totalVisits(totalVisits)
                .topPaths(topPaths)
                .topIps(topIps)
                .dailyVisits(dailyVisits)
                .build();
    }
}
