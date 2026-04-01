package com.hotbake.controller;

import com.hotbake.model.Product;
import com.hotbake.model.User;
import com.hotbake.service.ProductService;
import com.hotbake.service.QuestionService;
import com.hotbake.service.SellerService;
import com.hotbake.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    @Autowired private QuestionService questionService;
    @Autowired private ProductService productService;
    @Autowired private UserService userService;
    @Autowired private SellerService sellerService;

    @PostMapping("/ask")
    public String askQuestion(@AuthenticationPrincipal UserDetails ud,
                              @RequestParam Long productId,
                              @RequestParam String questionText,
                              RedirectAttributes flash) {
        try {
            User user = userService.findByEmail(ud.getUsername());
            Product product = productService.findById(productId);
            questionService.askQuestion(questionText, product, user);
            flash.addFlashAttribute("success", "Question posted!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/answer/{id}")
    public String answerQuestion(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails ud,
                                 @RequestParam String answerText,
                                 @RequestParam(defaultValue = "/seller/dashboard") String redirect,
                                 RedirectAttributes flash) {
        try {
            User user = userService.findByEmail(ud.getUsername());
            var seller = sellerService.getSellerProfileByUser(user);
            questionService.answerQuestion(id, answerText, seller);
            flash.addFlashAttribute("success", "Answer posted!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + redirect;
    }
}
