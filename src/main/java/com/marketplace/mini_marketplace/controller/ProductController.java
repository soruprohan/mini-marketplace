package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.dto.ProductDTO;
import com.marketplace.mini_marketplace.service.CategoryService;
import com.marketplace.mini_marketplace.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService  = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(@RequestParam(required = false) Long categoryId, Model model) {
        if (categoryId != null) {
            model.addAttribute("products", productService.getProductsByCategory(categoryId));
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "products/detail";
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/form";
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public String createProduct(@Valid @ModelAttribute("productDTO") ProductDTO dto,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails currentUser,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/form";
        }
        productService.createProduct(dto, currentUser.getUsername());
        return "redirect:/products";
    }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        var product = productService.getProductById(id);
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategory().getId());
        model.addAttribute("productDTO", dto);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/form";
    }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping("/{id}/update")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productDTO") ProductDTO dto,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails currentUser,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/form";
        }
        productService.updateProduct(id, dto, currentUser.getUsername());
        return "redirect:/products";
    }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails currentUser) {
        productService.deleteProduct(id, currentUser.getUsername());
        return "redirect:/products";
    }
}