package com.mywebsite.blog.controller;

import com.mywebsite.blog.common.ApiResponse;
import com.mywebsite.blog.dto.FriendLinkPublicDto;
import com.mywebsite.blog.service.FriendLinkService;
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
