package com.weihanyinian.website.controller;

import com.weihanyinian.website.common.ApiResponse;
import com.weihanyinian.website.entity.Category;
import com.weihanyinian.website.entity.Tag;
import com.weihanyinian.website.service.MetaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> categories() {
        return ApiResponse.success(metaService.getAllCategories());
    }

    @GetMapping("/tags")
    public ApiResponse<List<Tag>> tags() {
        return ApiResponse.success(metaService.getAllTags());
    }
}
