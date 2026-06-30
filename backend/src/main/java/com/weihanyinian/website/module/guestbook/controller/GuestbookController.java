package com.weihanyinian.website.module.guestbook.controller;

import com.weihanyinian.website.common.ApiResponse;
import com.weihanyinian.website.module.guestbook.dto.GuestbookRequest;
import com.weihanyinian.website.module.guestbook.entity.Guestbook;
import com.weihanyinian.website.module.guestbook.service.GuestbookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guestbook")
public class GuestbookController {

    private final GuestbookService guestbookService;

    public GuestbookController(GuestbookService guestbookService) {
        this.guestbookService = guestbookService;
    }

    @PostMapping
    public ApiResponse<Guestbook> create(@Valid @RequestBody GuestbookRequest request) {
        Guestbook message = Guestbook.builder()
                .nickname(request.getNickname().trim())
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .content(request.getContent().trim())
                .build();
        Guestbook saved = guestbookService.createMessage(message);
        return ApiResponse.success("留言成功", saved);
    }

    @GetMapping
    public ApiResponse<List<Guestbook>> list() {
        List<Guestbook> messages = guestbookService.getAllMessages();
        return ApiResponse.success(messages);
    }
}
