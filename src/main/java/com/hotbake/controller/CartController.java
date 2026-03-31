package com.hotbake.controller;

import com.hotbake.dto.CartItemDto;
import com.hotbake.model.Product;
import com.hotbake.model.User;
import com.hotbake.service.ProductService;
import com.hotbake.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired private ProductService productService;
    @Autowired private UserService userService;

    private BigDecimal normalizePounds(BigDecimal pounds) {
        if (pounds == null) return BigDecimal.valueOf(0.5);
        BigDecimal step = BigDecimal.valueOf(0.5);
        BigDecimal normalized = pounds.divide(step, 0, RoundingMode.HALF_UP).multiply(step);
        if (normalized.compareTo(step) < 0) {
            return step;
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private Map<String, CartItemDto> getCart(HttpSession session) {
        Map<String, CartItemDto> cart = (Map<String, CartItemDto>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
    


    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails ud, HttpSession session, Model model) {
        Map<String, CartItemDto> cart = getCart(session);
        BigDecimal total = cart.values().stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", total);
        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam BigDecimal quantity,
                            @AuthenticationPrincipal UserDetails ud,
                            HttpSession session,
                            RedirectAttributes flash) {
        try {
            Product product = productService.findById(productId);
            boolean isByPieces = product.getCategory() == com.hotbake.enums.ProductCategory.CUPCAKE 
                              || product.getCategory() == com.hotbake.enums.ProductCategory.TUB_CAKE;
            
            BigDecimal normalizedQuantity;
            String minErrorMsg;
            
            if (isByPieces) {
                // For pieces, must be whole number >= 1
                normalizedQuantity = quantity.setScale(0, java.math.RoundingMode.DOWN);
                if (normalizedQuantity.compareTo(BigDecimal.ONE) < 0) {
                    normalizedQuantity = BigDecimal.ONE;
                }
                minErrorMsg = "Minimum order is 1 piece.";
            } else {
                // For pounds, use 0.5 increments
                normalizedQuantity = normalizePounds(quantity);
                minErrorMsg = "Minimum order is 0.5 pounds.";
            }
            
            if (normalizedQuantity.compareTo(isByPieces ? BigDecimal.ONE : BigDecimal.valueOf(0.5)) < 0) {
                flash.addFlashAttribute("error", minErrorMsg);
                return "redirect:/products/" + productId;
            }
            
            Map<String, CartItemDto> cart = getCart(session);
            
            Long primaryImageId = null;
            if (product.getPrimaryImage() != null) {
                primaryImageId = product.getPrimaryImage().getId();
            }
            
            // Create a new cart item each time (don't combine with existing)
            CartItemDto item = new CartItemDto(
                    product.getId(),
                    product.getName(),
                    product.getSellerProfile().getShopName(),
                    primaryImageId,
                    product.getPricePerPound(),
                    normalizedQuantity,
                    isByPieces
            );
            cart.put(item.getCartItemId(), item);
            flash.addFlashAttribute("success", "Added to cart!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Could not add to cart: " + e.getMessage());
        }
        // Don't redirect to cart - stay on current page
        return "redirect:/products/" + productId;
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam String cartItemId,
                             @RequestParam String quantity,
                             @AuthenticationPrincipal UserDetails ud,
                             HttpSession session,
                             RedirectAttributes flash) {
        Map<String, CartItemDto> cart = getCart(session);
        CartItemDto item = cart.get(cartItemId);
        if (item == null) {
            return "redirect:/cart";
        }

        try {
            BigDecimal parsed = new BigDecimal(quantity);
            if (item.isByPieces()) {
                parsed = parsed.setScale(0, java.math.RoundingMode.DOWN);
                if (parsed.compareTo(BigDecimal.ONE) < 0) parsed = BigDecimal.ONE;
            } else {
                parsed = normalizePounds(parsed);
            }
            item.setQuantity(parsed);
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Invalid quantity value.");
        }

        return "redirect:/cart";
    }

    @PostMapping("/increment")
    public String incrementItem(@RequestParam String cartItemId,
                                @AuthenticationPrincipal UserDetails ud,
                                HttpSession session,
                                RedirectAttributes flash) {
        Map<String, CartItemDto> cart = getCart(session);
        CartItemDto item = cart.get(cartItemId);
        if (item == null) {
            flash.addFlashAttribute("error", "Cart item not found.");
            return "redirect:/cart";
        }
        BigDecimal increment = item.isByPieces() ? BigDecimal.ONE : BigDecimal.valueOf(0.5);
        item.setQuantity(item.getQuantity().add(increment));
        return "redirect:/cart";
    }

    @PostMapping("/decrement")
    public String decrementItem(@RequestParam String cartItemId,
                                @AuthenticationPrincipal UserDetails ud,
                                HttpSession session,
                                RedirectAttributes flash) {
        Map<String, CartItemDto> cart = getCart(session);
        CartItemDto item = cart.get(cartItemId);
        if (item == null) {
            flash.addFlashAttribute("error", "Cart item not found.");
            return "redirect:/cart";
        }
        BigDecimal decrement = item.isByPieces() ? BigDecimal.ONE : BigDecimal.valueOf(0.5);
        BigDecimal newValue = item.getQuantity().subtract(decrement);
        BigDecimal minimum = item.isByPieces() ? BigDecimal.ONE : BigDecimal.valueOf(0.5);
        if (newValue.compareTo(minimum) < 0) {
            newValue = minimum;
        }
        item.setQuantity(newValue);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeFromCart(@PathVariable String cartItemId,
                                 @AuthenticationPrincipal UserDetails ud,
                                 HttpSession session) {
        getCart(session).remove(cartItemId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal UserDetails ud, HttpSession session) {
        session.removeAttribute("cart");
        return "redirect:/cart";
    }

    @PostMapping("/ajax/increment")
    @ResponseBody
    public Map<String, Object> ajaxIncrement(@RequestParam String cartItemId,
                                             @AuthenticationPrincipal UserDetails ud,
                                             HttpSession session) {
        return handleAjaxUpdate(cartItemId, ud, session, true);
    }

    @PostMapping("/ajax/decrement")
    @ResponseBody
    public Map<String, Object> ajaxDecrement(@RequestParam String cartItemId,
                                             @AuthenticationPrincipal UserDetails ud,
                                             HttpSession session) {
        return handleAjaxUpdate(cartItemId, ud, session, false);
    }

    private Map<String, Object> handleAjaxUpdate(String cartItemId, @AuthenticationPrincipal UserDetails ud, HttpSession session, boolean increment) {
        Map<String, CartItemDto> cart = getCart(session);
        CartItemDto item = cart.get(cartItemId);
        Map<String, Object> response = new HashMap<>();

        if (item != null) {
            BigDecimal step = item.isByPieces() ? BigDecimal.ONE : BigDecimal.valueOf(0.5);
            BigDecimal newValue = increment
                    ? item.getQuantity().add(step)
                    : item.getQuantity().subtract(step);

            BigDecimal minimum = item.isByPieces() ? BigDecimal.ONE : BigDecimal.valueOf(0.5);
            if (newValue.compareTo(minimum) < 0) {
                newValue = minimum;
            }
            if (!item.isByPieces()) {
                newValue = normalizePounds(newValue);
            } else {
                newValue = newValue.setScale(0, java.math.RoundingMode.DOWN);
            }
            item.setQuantity(newValue);

            BigDecimal total = cart.values().stream()
                    .map(CartItemDto::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.put("success", true);
            response.put("pounds", item.getQuantity().toString());
            response.put("subtotalFormatted", String.format("%,.2f", item.getSubtotal()));
            response.put("cartTotalFormatted", String.format("%,.2f", total));
            response.put("cartSize", cart.size());
        } else {
            response.put("success", false);
            response.put("error", "Item not found");
        }
        return response;
    }
}

