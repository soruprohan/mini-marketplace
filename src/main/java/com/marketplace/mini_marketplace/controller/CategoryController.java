package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.dto.CategoryDTO;
import com.marketplace.mini_marketplace.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("categoryDTO", new CategoryDTO());  // add this line
        return "categories/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String createCategory(@Valid @ModelAttribute("categoryDTO") CategoryDTO dto,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "categories/list";
        }
        categoryService.createCategory(dto);
        return "redirect:/categories";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/update")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryDTO") CategoryDTO dto,
                                 BindingResult result) {
        if (result.hasErrors()) return "categories/list";
        categoryService.updateCategory(id, dto);
        return "redirect:/categories";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }
}