package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.model.Role;
import com.marketplace.mini_marketplace.service.CategoryService;
import com.marketplace.mini_marketplace.service.OrderService;
import com.marketplace.mini_marketplace.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OrderService orderService;
    private final CategoryService categoryService;
    private final UserService userService;

    public AdminController(OrderService orderService,
                           CategoryService categoryService,
                           UserService userService) {
        this.orderService    = orderService;
        this.categoryService = categoryService;
        this.userService     = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("orders",     orderService.getAllOrders());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String setRole(@PathVariable Long id,
                          @RequestParam String role,
                          RedirectAttributes redirectAttributes) {
        try {
            userService.setRole(id, Role.ERole.valueOf(role));
            redirectAttributes.addFlashAttribute("success", "Role updated successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            // Capture the username before deletion so we can compare it to the current principal
            String deletedUsername = userService.getUsernameById(id);
            userService.deleteUser(id);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals(deletedUsername)) {
                // The logged-in admin just deleted their own account — kill the session
                session.invalidate();
                SecurityContextHolder.clearContext();
                return "redirect:/auth/login?logout=true";
            }

            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}