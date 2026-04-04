package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.CategoryDTO;
import com.marketplace.mini_marketplace.exception.ResourceNotFoundException;
import com.marketplace.mini_marketplace.model.Category;
import com.marketplace.mini_marketplace.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Transactional
    public Category createCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryDTO dto) {
        Category category = getCategoryById(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}