package com.hotbake.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hotbake.dto.SellerRegistrationDto;
import com.hotbake.dto.UserRegistrationDto;
import com.hotbake.model.User;
import com.hotbake.service.SellerService;
import com.hotbake.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private SellerService sellerService;

    // ── LOGIN ─────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(@AuthenticationPrincipal UserDetails userDetails) {
        // Redirect already logged-in users to home
        if (userDetails != null) {
            return "redirect:/";
        }
        return "auth/login";
    }

    // ── BUYER REGISTRATION ────────────────────────────────
    @GetMapping("/register")
    public String registerPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Redirect already logged-in users to home
        if (userDetails != null) {
            return "redirect:/";
        }
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRegistrationDto dto,
                           BindingResult result,
                           RedirectAttributes flash) {
        if (result.hasErrors()) return "auth/register";
        try {
            userService.registerBuyer(dto);
            flash.addFlashAttribute("success", "Account created! Please log in.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.user", e.getMessage());
            return "auth/register";
        }
    }

    // ── SELLER APPLICATION ────────────────────────────────
    @GetMapping("/seller-apply")
    public String sellerApplyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        if (sellerService.findByUser(user).isPresent()) {
            model.addAttribute("existingApplication", sellerService.findByUser(user).get());
        }
        model.addAttribute("sellerDto", new SellerRegistrationDto());
        return "auth/seller-apply";
    }

    @PostMapping("/seller-apply")
    public String submitSellerApplication(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @ModelAttribute("sellerDto") SellerRegistrationDto dto,
                                          BindingResult result,
                                          RedirectAttributes flash) {
        if (result.hasErrors()) return "auth/seller-apply";
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            sellerService.applyAsSeller(dto, user);
            flash.addFlashAttribute("success", "Application submitted! Please wait for admin approval.");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/seller-apply";
        }
    }
}
