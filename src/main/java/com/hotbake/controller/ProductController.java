package com.hotbake.controller;

import com.hotbake.dto.ReviewDto;
import com.hotbake.enums.ProductCategory;
import com.hotbake.model.Product;
import com.hotbake.model.User;
import com.hotbake.repository.OrderItemRepository;
import com.hotbake.service.ProductService;
import com.hotbake.service.QuestionService;
import com.hotbake.service.ReviewService;
import com.hotbake.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private ReviewService reviewService;
    @Autowired private QuestionService questionService;
    @Autowired private UserService userService;
    @Autowired private OrderItemRepository orderItemRepository;

    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String category,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "newest") String sort,
                               Model model) {
        ProductCategory cat = null;
        if (category != null && !category.isBlank()) {
            try { cat = ProductCategory.valueOf(category); } catch (Exception ignored) {}
        }
        Page<Product> products = productService.getProducts(page, 20, sort, cat);
        model.addAttribute("products", products);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("query", "");
        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String viewProduct(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.getProductReviews(id));
        model.addAttribute("questions", questionService.getProductQuestions(id));
        model.addAttribute("reviewDto", new ReviewDto());

        boolean isOwnProduct = false;
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            var reviewableItems = orderItemRepository
                    .findDeliveredItemsByProductAndBuyer(id, user.getId())
                    .stream()
                    .filter(oi -> oi.getReview() == null)
                    .toList();
            model.addAttribute("canReview", !reviewableItems.isEmpty());
            model.addAttribute("reviewableItem", reviewableItems.isEmpty() ? null : reviewableItems.get(0));
            
            // Check if user has already reviewed this product
            var allOrderItems = orderItemRepository.findDeliveredItemsByProductAndBuyer(id, user.getId());
            boolean alreadyReviewed = allOrderItems.stream().anyMatch(oi -> oi.getReview() != null);
            model.addAttribute("alreadyReviewed", alreadyReviewed);
            
            // Check if current user is the seller
            isOwnProduct = product.getSellerProfile().getUser().getId().equals(user.getId());
            model.addAttribute("isOwnProduct", isOwnProduct);
        } else {
            model.addAttribute("canReview", false);
            model.addAttribute("reviewableItem", null);
            model.addAttribute("alreadyReviewed", false);
            model.addAttribute("isOwnProduct", false);
        }
        return "product/detail";
    }
}
