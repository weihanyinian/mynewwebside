package com.mywebside.blog.mindmap;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.mindmap.dto.MindMapCreateRequest;
import com.mywebside.blog.mindmap.dto.MindMapDetailDto;
import com.mywebside.blog.mindmap.dto.MindMapListItemDto;
import com.mywebside.blog.mindmap.dto.MindMapPatchRequest;
import com.mywebside.blog.service.UserAccountService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mind-maps")
public class MindMapController {

  private final MindMapService mindMapService;
  private final UserAccountService userAccountService;

  public MindMapController(MindMapService mindMapService, UserAccountService userAccountService) {
    this.mindMapService = mindMapService;
    this.userAccountService = userAccountService;
  }

  @GetMapping
  public ApiResponse<List<MindMapListItemDto>> list(Authentication auth) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(mindMapService.listMine(uid));
  }

  @GetMapping("/{id}")
  public ApiResponse<MindMapDetailDto> get(Authentication auth, @PathVariable long id) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(mindMapService.getMine(uid, id));
  }

  @PostMapping
  public ApiResponse<MindMapDetailDto> create(
      Authentication auth,
      @Valid @RequestBody MindMapCreateRequest body
  ) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(mindMapService.create(uid, body));
  }

  /** 快速创建空白画布 */
  @PostMapping("/blank")
  public ApiResponse<MindMapDetailDto> createBlank(
      Authentication auth,
      @RequestParam(required = false) String title
  ) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(mindMapService.createBlank(uid, title));
  }

  @PatchMapping("/{id}")
  public ApiResponse<MindMapDetailDto> patch(
      Authentication auth,
      @PathVariable long id,
      @Valid @RequestBody MindMapPatchRequest body
  ) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(mindMapService.update(uid, id, body));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(Authentication auth, @PathVariable long id) {
    long uid = userAccountService.requireUserId(auth.getName());
    mindMapService.delete(uid, id);
    return ApiResponse.ok();
  }
}
