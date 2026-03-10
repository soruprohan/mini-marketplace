package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.dto.OrderDTO;
import com.marketplace.mini_marketplace.service.OrderService;
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
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Place a new order — BUYER only.
     * The form on products/detail.html POSTs here with productId and quantity.
     */
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public String placeOrder(@Valid @ModelAttribute("orderDTO") OrderDTO dto,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails currentUser,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Invalid order data. Please check quantity.");
            return "redirect:/products/" + dto.getProductId();
        }
        try {
            orderService.placeOrder(dto, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Order placed successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products/" + dto.getProductId();
        }
        return "redirect:/orders/my";
    }

    /**
     * View own order history — BUYER only.
     */
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/my")
    public String myOrders(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        model.addAttribute("orders", orderService.getMyOrders(currentUser.getUsername()));
        return "orders/my-orders";
    }

    /**
     * View all orders — ADMIN only.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public String allOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "orders/all-orders";
    }

    /**
     * Cancel an order — BUYER (own) or ADMIN.
     * Uses POST form with hidden _method or direct POST since HTML forms don't support DELETE.
     */
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails currentUser,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect admins to all-orders, buyers to their own list
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/orders/all" : "redirect:/orders/my";
    }
}