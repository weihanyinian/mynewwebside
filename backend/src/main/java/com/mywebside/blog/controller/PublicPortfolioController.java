package com.mywebsite.blog.controller;

import com.mywebsite.blog.common.ApiResponse;
import com.mywebsite.blog.dto.PortfolioWorkDetailDto;
import com.mywebsite.blog.dto.PortfolioWorkPublicDto;
import com.mywebsite.blog.service.PortfolioWorkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/portfolio")
public class PublicPortfolioController {

  private final PortfolioWorkService portfolioWorkService;

  public PublicPortfolioController(PortfolioWorkService portfolioWorkService) {
    this.portfolioWorkService = portfolioWorkService;
  }

  @GetMapping("/works")
  public ApiResponse<List<PortfolioWorkPublicDto>> works() {
    return ApiResponse.ok(portfolioWorkService.listPublicWorks());
  }

  @GetMapping("/works/{id}")
  public ApiResponse<PortfolioWorkDetailDto> workDetail(@PathVariable long id) {
    return ApiResponse.ok(portfolioWorkService.getPublicWorkDetail(id));
  }
}
