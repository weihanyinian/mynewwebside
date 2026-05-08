package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.dto.PortfolioWorkAdminDto;
import com.mywebside.blog.dto.PortfolioWorkUpsertRequest;
import com.mywebside.blog.service.PortfolioWorkService;
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
@RequestMapping("/api/admin/portfolio/works")
public class AdminPortfolioWorkController {

  private final PortfolioWorkService portfolioWorkService;

  public AdminPortfolioWorkController(PortfolioWorkService portfolioWorkService) {
    this.portfolioWorkService = portfolioWorkService;
  }

  @GetMapping
  public ApiResponse<List<PortfolioWorkAdminDto>> list() {
    return ApiResponse.ok(portfolioWorkService.listAdminWorks());
  }

  @PostMapping
  public ApiResponse<PortfolioWorkAdminDto> create(@Valid @RequestBody PortfolioWorkUpsertRequest req) {
    return ApiResponse.ok(portfolioWorkService.create(req));
  }

  @PutMapping("/{id}")
  public ApiResponse<PortfolioWorkAdminDto> update(
      @PathVariable long id,
      @Valid @RequestBody PortfolioWorkUpsertRequest req
  ) {
    return ApiResponse.ok(portfolioWorkService.update(id, req));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    portfolioWorkService.delete(id);
    return ApiResponse.ok();
  }
}
