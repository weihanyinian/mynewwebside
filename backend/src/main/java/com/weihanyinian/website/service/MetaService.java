package com.weihanyinian.website.service;

import com.weihanyinian.website.entity.Category;
import com.weihanyinian.website.entity.Tag;
import com.weihanyinian.website.repository.CategoryRepository;
import com.weihanyinian.website.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetaService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public MetaService(CategoryRepository categoryRepository, TagRepository tagRepository) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}
