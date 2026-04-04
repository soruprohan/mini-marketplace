package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.dto.OrderDTO;
import com.marketplace.mini_marketplace.dto.ProductDTO;
import com.marketplace.mini_marketplace.dto.ReviewDTO;
import com.marketplace.mini_marketplace.model.Product;
import com.marketplace.mini_marketplace.service.CategoryService;
import com.marketplace.mini_marketplace.service.ProductService;
import com.marketplace.mini_marketplace.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
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
    private final ReviewService reviewService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             ReviewService reviewService) {
        this.productService  = productService;
        this.categoryService = categoryService;
        this.reviewService   = reviewService;
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
        model.addAttribute("reviews", reviewService.getReviewsByProduct(id));
        // Required so th:object="${reviewDTO}" on the review form does not throw NPE
        model.addAttribute("reviewDTO", new ReviewDTO());
        model.addAttribute("orderDTO", new OrderDTO());
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

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/my")
    public String myProducts(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        model.addAttribute("products", productService.getProductsBySeller(currentUser.getUsername()));
        return "products/my-products";
    }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails currentUser,
                                  Model model) {
        Product product = productService.getProductById(id);

        // Bug fix #3: gate the edit FORM on ownership, not just the POST
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = product.getSeller().getUsername().equals(currentUser.getUsername());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not own this product");
        }

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
        // ProductService.updateProduct already checks ownership — no duplication needed
        productService.updateProduct(id, dto, currentUser.getUsername());
        return "redirect:/products";
    }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails currentUser) {
        // ProductService.deleteProduct already checks ownership
        productService.deleteProduct(id, currentUser.getUsername());
        return "redirect:/products";
    }
}