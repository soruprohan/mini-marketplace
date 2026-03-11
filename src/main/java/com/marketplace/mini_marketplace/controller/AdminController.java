package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.service.CategoryService;
import com.marketplace.mini_marketplace.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OrderService orderService;
    private final CategoryService categoryService;

    public AdminController(OrderService orderService, CategoryService categoryService) {
        this.orderService    = orderService;
        this.categoryService = categoryService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("orders",     orderService.getAllOrders());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/dashboard";
    }
}
