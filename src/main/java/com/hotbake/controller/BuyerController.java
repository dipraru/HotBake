package com.hotbake.controller;

import com.hotbake.model.DeliveryAddress;
import com.hotbake.model.Order;
import com.hotbake.model.User;
import com.hotbake.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/buyer")
public class BuyerController {

    @Autowired private UserService userService;
    @Autowired private OrderService orderService;
    @Autowired private AddressService addressService;
    @Autowired private ReviewService reviewService;

    private User getUser(UserDetails ud) {
        return userService.findByEmail(ud.getUsername());
    }

    // ── PROFILE ──────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("user", getUser(ud));
        return "buyer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails ud,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String phone,
                                RedirectAttributes flash) {
        userService.updateProfile(getUser(ud), firstName, lastName, phone);
        flash.addFlashAttribute("success", "Profile updated successfully.");
        return "redirect:/buyer/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails ud,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes flash) {
        try {
            userService.changePassword(getUser(ud), currentPassword, newPassword, confirmPassword);
            flash.addFlashAttribute("success", "Password changed successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/buyer/profile";
    }

    // ── ORDERS ───────────────────────────────────────────────
    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = getUser(ud);
        var orders = orderService.getBuyerOrders(user);
        // Set hasReview flag for each order (if any item in the order has been reviewed)
        java.util.Set<Long> reviewedProductIds = new java.util.HashSet<>();
        for (Order order : orders) {
            boolean hasAnyReview = order.getItems().stream()
                    .anyMatch(item -> reviewService.hasReviewForOrderItem(item.getId()));
            order.setHasReview(hasAnyReview);
            // Track all reviewed product IDs
            order.getItems().stream()
                    .filter(item -> reviewService.hasReviewForOrderItem(item.getId()))
                    .forEach(item -> reviewedProductIds.add(item.getProduct().getId()));
        }
        model.addAttribute("orders", orders);
        model.addAttribute("reviewableItems", reviewService.getReviewableItems(user));
        model.addAttribute("reviewedProductIds", reviewedProductIds);
        return "buyer/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails ud,
                              Model model) {
        Order order = orderService.findById(id);
        if (!order.getBuyer().getEmail().equals(ud.getUsername())) {
            return "redirect:/buyer/orders";
        }
        model.addAttribute("order", order);
        return "buyer/order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails ud,
                              RedirectAttributes flash) {
        try {
            orderService.cancelOrder(id, getUser(ud));
            flash.addFlashAttribute("success", "Order cancelled.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/buyer/orders";
    }

    // ── ADDRESSES ────────────────────────────────────────────
    @GetMapping("/addresses")
    public String addresses(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("addresses", addressService.getUserAddresses(getUser(ud)));
        return "buyer/addresses";
    }

    @PostMapping("/addresses/add")
    public String addAddress(@AuthenticationPrincipal UserDetails ud,
                             @RequestParam String recipientName,
                             @RequestParam String addressLine,
                             @RequestParam String city,
                             @RequestParam(required = false) String district,
                             @RequestParam String phone,
                             @RequestParam(defaultValue = "false") boolean makeDefault,
                             RedirectAttributes flash) {
        addressService.addAddress(getUser(ud), recipientName, addressLine, city, district, phone, makeDefault);
        flash.addFlashAttribute("success", "Address added.");
        return "redirect:/buyer/addresses";
    }

    @PostMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails ud,
                                RedirectAttributes flash) {
        try {
            addressService.deleteAddress(id, getUser(ud));
            flash.addFlashAttribute("success", "Address removed.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/buyer/addresses";
    }

    @PostMapping("/addresses/set-default/{id}")
    public String setDefault(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes flash) {
        addressService.setDefault(id, getUser(ud));
        flash.addFlashAttribute("success", "Default address updated.");
        return "redirect:/buyer/addresses";
    }

    // ── REVIEWS ──────────────────────────────────────────────
    @GetMapping("/reviews")
    public String myReviews(@AuthenticationPrincipal UserDetails ud, 
                           @RequestParam(required = false) Long orderItemId,
                           Model model) {
        User user = getUser(ud);
        
        // If a specific orderItemId is provided, filter to show only that item and its reviews
        if (orderItemId != null) {
            var allReviewableItems = reviewService.getReviewableItems(user);
            var filteredItems = allReviewableItems.stream()
                    .filter(item -> item.getId().equals(orderItemId))
                    .toList();
            model.addAttribute("reviewableItems", filteredItems);
            
            // Show only reviews for this specific orderItemId's product
            var allReviews = reviewService.getBuyerReviews(user.getId());
            if (!filteredItems.isEmpty()) {
                var productId = filteredItems.get(0).getProduct().getId();
                var filteredReviews = allReviews.stream()
                        .filter(review -> review.getProduct().getId().equals(productId))
                        .toList();
                model.addAttribute("reviews", filteredReviews);
            } else {
                model.addAttribute("reviews", new java.util.ArrayList<>());
            }
            model.addAttribute("orderItemId", orderItemId);
        } else {
            model.addAttribute("reviews", reviewService.getBuyerReviews(user.getId()));
            model.addAttribute("reviewableItems", reviewService.getReviewableItems(user));
        }
        
        return "buyer/reviews";
    }
}
