package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.domain.FriendLink;
import com.mywebside.blog.dto.FriendLinkUpsertRequest;
import com.mywebside.blog.service.FriendLinkService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/friends")
public class AdminFriendController {

  private final FriendLinkService friendLinkService;

  public AdminFriendController(FriendLinkService friendLinkService) {
    this.friendLinkService = friendLinkService;
  }

  @GetMapping
  public ApiResponse<List<FriendLink>> list() {
    return ApiResponse.ok(friendLinkService.listAllEntities());
  }

  @PostMapping
  public ApiResponse<FriendLink> create(@Valid @RequestBody FriendLinkUpsertRequest body) {
    return ApiResponse.ok(friendLinkService.create(body));
  }

  @PutMapping("/{id}")
  public ApiResponse<FriendLink> update(
      @PathVariable long id,
      @Valid @RequestBody FriendLinkUpsertRequest body
  ) {
    return ApiResponse.ok(friendLinkService.update(id, body));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    friendLinkService.delete(id);
    return ApiResponse.ok();
  }
}
