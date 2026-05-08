package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.dto.FriendLinkPublicDto;
import com.mywebside.blog.service.FriendLinkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/friends")
public class PublicFriendController {

  private final FriendLinkService friendLinkService;

  public PublicFriendController(FriendLinkService friendLinkService) {
    this.friendLinkService = friendLinkService;
  }

  @GetMapping
  public ApiResponse<List<FriendLinkPublicDto>> list() {
    return ApiResponse.ok(friendLinkService.listPublic());
  }
}
