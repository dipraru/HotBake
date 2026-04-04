package com.hotbake.controller;

import com.hotbake.dto.ProductDto;
import com.hotbake.enums.OrderStatus;
import com.hotbake.enums.ProductCategory;
import com.hotbake.model.Product;
import com.hotbake.model.SellerProfile;
import com.hotbake.model.User;
import com.hotbake.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller")
public class SellerController {

    @Autowired private SellerService sellerService;
    @Autowired private ProductService productService;
    @Autowired private OrderService orderService;
    @Autowired private QuestionService questionService;
    @Autowired private UserService userService;

    private SellerProfile getSeller(UserDetails ud) {
        User user = userService.findByEmail(ud.getUsername());
        return sellerService.getSellerProfileByUser(user);
    }

    // ── DASHBOARD ────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        SellerProfile seller = getSeller(ud);
        var products = productService.getSellerProducts(seller);
        var orders = orderService.getSellerOrders(seller.getId());
        var unanswered = questionService.getUnansweredQuestionsForSeller(seller.getId());

        model.addAttribute("seller", seller);
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("pendingOrders", orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        model.addAttribute("unansweredQuestions", unanswered);
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        return "seller/dashboard";
    }

    // ── PRODUCTS ─────────────────────────────────────────────
    @GetMapping("/products")
    public String products(@AuthenticationPrincipal UserDetails ud, Model model) {
        SellerProfile seller = getSeller(ud);
        model.addAttribute("products", productService.getSellerProducts(seller));
        return "seller/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("categories", ProductCategory.values());
        return "seller/add-product";
    }

    @PostMapping("/products/add")
    public String addProduct(@AuthenticationPrincipal UserDetails ud,
                             @Valid @ModelAttribute("productDto") ProductDto dto,
                             BindingResult result,
                             RedirectAttributes flash,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", ProductCategory.values());
            return "seller/add-product";
        }
        try {
            SellerProfile seller = getSeller(ud);
            productService.createProduct(dto, seller);
            flash.addFlashAttribute("success", "Product added successfully!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error adding product: " + e.getMessage());
        }
        return "redirect:/seller/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails ud,
                                  Model model) {
        Product product = productService.findById(id);
        ProductDto dto = new ProductDto();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setPricePerPound(product.getPricePerPound());
        dto.setAvailable(product.isAvailable());
        model.addAttribute("productDto", dto);
        model.addAttribute("product", product);
        model.addAttribute("categories", ProductCategory.values());
        return "seller/edit-product";
    }

    @PostMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails ud,
                              @Valid @ModelAttribute("productDto") ProductDto dto,
                              BindingResult result,
                              RedirectAttributes flash,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", ProductCategory.values());
            return "seller/edit-product";
        }
        try {
            SellerProfile seller = getSeller(ud);
            productService.updateProduct(id, dto, seller);
            flash.addFlashAttribute("success", "Product updated!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails ud,
                                RedirectAttributes flash) {
        try {
            productService.deleteProduct(id, getSeller(ud));
            flash.addFlashAttribute("success", "Product deleted.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/products";
    }

    // ── ORDERS ───────────────────────────────────────────────
    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam(required = false) String status,
                         Model model) {
        SellerProfile seller = getSeller(ud);
        var orders = (status != null && !status.isBlank())
                ? orderService.getSellerOrdersByStatus(seller.getId(), OrderStatus.valueOf(status))
                : orderService.getSellerOrders(seller.getId());
        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        return "seller/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam OrderStatus status,
                                    @RequestParam(required = false) String cancellationReason,
                                    @AuthenticationPrincipal UserDetails ud,
                                    RedirectAttributes flash) {
        try {
            orderService.updateOrderStatus(id, status, cancellationReason, getSeller(ud));
            flash.addFlashAttribute("success", "Order status updated to: " + status.getDisplayName());
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/orders";
    }
}
