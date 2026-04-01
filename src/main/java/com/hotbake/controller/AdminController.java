package com.hotbake.controller;

import com.hotbake.enums.OrderStatus;
import com.hotbake.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private SellerService sellerService;
    @Autowired private ProductService productService;
    @Autowired private ReviewService reviewService;
    @Autowired private OrderService orderService;

    // ── DASHBOARD ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers",    userService.countAll());
        model.addAttribute("totalSellers",  sellerService.countAll());
        model.addAttribute("pendingSellers",sellerService.countPending());
        model.addAttribute("totalProducts", productService.countAll());
        model.addAttribute("totalOrders",   orderService.countAll());
        model.addAttribute("pendingOrders", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("pendingSellerList", sellerService.getPendingSellers());
        return "admin/dashboard";
    }

    // ── SELLERS ───────────────────────────────────────────────
    @GetMapping("/sellers")
    public String sellers(Model model) {
        model.addAttribute("sellers", sellerService.getAllSellers());
        return "admin/sellers";
    }

    @PostMapping("/sellers/{id}/approve")
    public String approveSeller(@PathVariable Long id, RedirectAttributes flash) {
        sellerService.approveSeller(id);
        flash.addFlashAttribute("success", "Seller approved successfully!");
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/{id}/reject")
    public String rejectSeller(@PathVariable Long id,
                               @RequestParam(defaultValue = "Application did not meet requirements.") String reason,
                               RedirectAttributes flash) {
        sellerService.rejectSeller(id, reason);
        flash.addFlashAttribute("success", "Seller application rejected.");
        return "redirect:/admin/sellers";
    }

    // ── USERS ─────────────────────────────────────────────────
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/ban")
    public String banUser(@PathVariable Long id, RedirectAttributes flash) {
        userService.banUser(id);
        flash.addFlashAttribute("success", "User has been banned.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unban")
    public String unbanUser(@PathVariable Long id, RedirectAttributes flash) {
        userService.unbanUser(id);
        flash.addFlashAttribute("success", "User has been unbanned.");
        return "redirect:/admin/users";
    }

    // ── PRODUCTS ──────────────────────────────────────────────
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAllForAdmin());
        return "admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes flash) {
        productService.adminDeleteProduct(id);
        flash.addFlashAttribute("success", "Product deleted.");
        return "redirect:/admin/products";
    }

    // ── REVIEWS ───────────────────────────────────────────────
    @GetMapping("/reviews")
    public String reviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes flash) {
        reviewService.adminDeleteReview(id);
        flash.addFlashAttribute("success", "Review deleted.");
        return "redirect:/admin/reviews";
    }
}
