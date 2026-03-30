package com.hotbake.controller;

import com.hotbake.enums.ProductCategory;
import com.hotbake.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired private ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("latestProducts", productService.getLatestProducts(12));
        model.addAttribute("categories", ProductCategory.values());
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q,
                         @RequestParam(required = false) String category,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        ProductCategory cat = null;
        if (category != null && !category.isBlank()) {
            try { cat = ProductCategory.valueOf(category); } catch (Exception ignored) {}
        }
        var results = productService.searchProducts(q, cat, page, 20);
        model.addAttribute("products", results);
        model.addAttribute("query", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", results.getTotalPages());
        return "product/list";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
