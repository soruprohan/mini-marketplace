package com.marketplace.mini_marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }
}
