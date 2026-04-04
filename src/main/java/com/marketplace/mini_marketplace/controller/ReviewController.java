package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.dto.ReviewDTO;
import com.marketplace.mini_marketplace.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Submit a review for a product — BUYER only.
     * POSTs from the review form on products/detail.html.
     */
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/products/{id}/reviews")
    public String addReview(@PathVariable Long id,
                            @Valid @ModelAttribute("reviewDTO") ReviewDTO dto,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails currentUser,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("reviewError",
                    "Please provide a valid rating (1–5) and a comment.");
            return "redirect:/products/" + id;
        }
        // Ensure the DTO's productId matches the path variable
        dto.setProductId(id);
        try {
            reviewService.addReview(dto, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        }
        return "redirect:/products/" + id;
    }

    /**
     * List reviews for a product — public.
     * Rendered as a fragment inside products/detail.html; also accessible standalone.
     */
    @GetMapping("/products/{id}/reviews")
    public String getReviews(@PathVariable Long id, Model model) {
        model.addAttribute("reviews", reviewService.getReviewsByProduct(id));
        model.addAttribute("productId", id);
        return "reviews/list";
    }

    /**
     * Delete a review — owner or ADMIN.
     * Called via a POST form on the product detail page or the reviews list page.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id,
                               @RequestParam(required = false) Long productId,
                               @AuthenticationPrincipal UserDetails currentUser,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // Redirect back to the product detail page if productId was passed, else products list
        if (productId != null) {
            return "redirect:/products/" + productId;
        }
        return "redirect:/products";
    }
}