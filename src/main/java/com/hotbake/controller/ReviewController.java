package com.hotbake.controller;

import com.hotbake.dto.ReviewDto;
import com.hotbake.model.User;
import com.hotbake.service.ReviewService;
import com.hotbake.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired private ReviewService reviewService;
    @Autowired private UserService userService;

    @PostMapping("/add")
    public String addReview(@AuthenticationPrincipal UserDetails ud,
                            @Valid @ModelAttribute ReviewDto dto,
                            @RequestParam(defaultValue = "/") String redirect,
                            RedirectAttributes flash) {
        try {
            User user = userService.findByEmail(ud.getUsername());
            reviewService.addReview(dto, user);
            flash.addFlashAttribute("success", "Review submitted successfully!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + redirect;
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes flash) {
        try {
            User user = userService.findByEmail(ud.getUsername());
            reviewService.deleteReview(id, user);
            flash.addFlashAttribute("success", "Review deleted.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/buyer/reviews";
    }
}
