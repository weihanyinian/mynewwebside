package com.weihanyinian.website.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtSecretValidator.class);
    private static final String DEFAULT_SECRET = "change-me-in-production-use-a-very-long-random-string";

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @EventListener(ApplicationReadyEvent.class)
    public void validate() {
        if (jwtSecret == null || jwtSecret.isBlank() || DEFAULT_SECRET.equals(jwtSecret)) {
            if ("local".equals(activeProfile) || "dev".equals(activeProfile)) {
                log.warn("============================================");
                log.warn("  JWT secret is using the default value.");
                log.warn("  This is OK for local development ONLY.");
                log.warn("  Set JWT_SECRET env var in production!");
                log.warn("============================================");
            } else {
                log.error("============================================");
                log.error("  JWT_SECRET must be set in profile: {}", activeProfile);
                log.error("  Using the default secret in production is");
                log.error("  a critical security risk.");
                log.error("============================================");
                throw new IllegalStateException(
                    "JWT_SECRET environment variable is required for profile '" + activeProfile + "'"
                );
            }
        }
    }
}
