package com.weihanyinian.website;

import com.weihanyinian.website.module.blog.service.ArticleService;
import com.weihanyinian.website.module.guestbook.dto.GuestbookRequest;
import com.weihanyinian.website.module.admin.dto.LoginRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic validation and service-layer unit tests.
 */
class ApplicationTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ─── Validation tests ───

    @Test
    void guestbookRequestShouldRejectBlankNickname() {
        GuestbookRequest req = new GuestbookRequest();
        req.setNickname("");
        req.setContent("Hello");
        var violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("昵称")));
    }

    @Test
    void guestbookRequestShouldRejectBlankContent() {
        GuestbookRequest req = new GuestbookRequest();
        req.setNickname("User");
        req.setContent("");
        var violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("内容")));
    }

    @Test
    void guestbookRequestShouldRejectOversizedNickname() {
        GuestbookRequest req = new GuestbookRequest();
        req.setNickname("A".repeat(51));
        req.setContent("Hello");
        var violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validGuestbookRequestShouldPass() {
        GuestbookRequest req = new GuestbookRequest();
        req.setNickname("正常用户");
        req.setContent("这是一条正常的留言内容");
        var violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequestShouldRejectBlankUsername() {
        LoginRequest req = new LoginRequest();
        req.setUsername("");
        req.setPassword("pass");
        var violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void loginRequestShouldRejectBlankPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("");
        var violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validLoginRequestShouldPass() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("password123");
        var violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }
}
