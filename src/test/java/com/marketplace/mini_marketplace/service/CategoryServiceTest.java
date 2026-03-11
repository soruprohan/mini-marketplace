package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.CategoryDTO;
import com.marketplace.mini_marketplace.exception.ResourceNotFoundException;
import com.marketplace.mini_marketplace.model.Category;
import com.marketplace.mini_marketplace.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;

    @InjectMocks CategoryService categoryService;

    @Test
    void createCategory_shouldSaveAndReturn() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Books");
        dto.setDescription("All kinds of books");

        Category saved = new Category();
        saved.setId(1L);
        saved.setName("Books");

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        Category result = categoryService.createCategory(dto);

        assertThat(result.getName()).isEqualTo("Books");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deleteCategory_shouldThrow_whenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(999L));

        verify(categoryRepository, never()).delete(any());
    }
}