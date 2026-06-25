package com.weihanyinian.website.module.visitor;

import com.weihanyinian.website.module.visitor.entity.VisitorLog;
import com.weihanyinian.website.module.visitor.repository.VisitorLogRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VisitorFilter implements Filter {

    private final VisitorLogRepository visitorLogRepository;

    public VisitorFilter(VisitorLogRepository visitorLogRepository) {
        this.visitorLogRepository = visitorLogRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String path = httpRequest.getRequestURI();

            // Skip static resources and health checks
            if (shouldSkip(path)) {
                chain.doFilter(request, response);
                return;
            }

            VisitorLog log = VisitorLog.builder()
                    .ip(getClientIp(httpRequest))
                    .userAgent(truncate(httpRequest.getHeader("User-Agent"), 500))
                    .referer(truncate(httpRequest.getHeader("Referer"), 200))
                    .path(truncate(path, 500))
                    .method(httpRequest.getMethod())
                    .build();

            // Async save to not block request
            visitorLogRepository.save(log);
        } catch (Exception ignored) {
            // Never let logging break the request
        }
        chain.doFilter(request, response);
    }

    private boolean shouldSkip(String path) {
        return path == null
                || path.startsWith("/assets/")
                || path.startsWith("/videos/")
                || path.startsWith("/favicon")
                || path.startsWith("/actuator")
                || path.startsWith("/h2-console")
                || path.contains(".");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Multiple IPs in X-Forwarded-For, take the first
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String truncate(String value, int maxLen) {
        if (value == null) return null;
        return value.length() > maxLen ? value.substring(0, maxLen) : value;
    }
}
