package com.hotbake.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hotbake.dto.CartItemDto;
import com.hotbake.dto.CheckoutDto;
import com.hotbake.model.DeliveryAddress;
import com.hotbake.model.Order;
import com.hotbake.model.User;
import com.hotbake.service.AddressService;
import com.hotbake.service.OrderService;
import com.hotbake.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private AddressService addressService;

    @SuppressWarnings("unchecked")
    private Map<String, CartItemDto> getCart(HttpSession session) {
        return (Map<String, CartItemDto>) session.getAttribute("cart");
    }

    private void populateCheckoutModel(User user, Map<String, CartItemDto> cart, Model model, CheckoutDto checkoutDto) {
        List<DeliveryAddress> addresses = addressService.getUserAddresses(user);
        BigDecimal total = cart.values().stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", total);
        model.addAttribute("addresses", addresses);
        model.addAttribute("checkoutDto", checkoutDto);
    }

    @GetMapping
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session, Model model) {
        Map<String, CartItemDto> cart = getCart(session);
        if (cart == null || cart.isEmpty()) return "redirect:/cart";

        User user = userService.findByEmail(userDetails.getUsername());
        populateCheckoutModel(user, cart, model, new CheckoutDto());
        return "checkout/checkout";
    }

    @PostMapping("/place")
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute("checkoutDto") CheckoutDto dto,
                             BindingResult result,
                             HttpSession session,
                             Model model,
                             RedirectAttributes flash) {
        Map<String, CartItemDto> cart = getCart(session);
        if (cart == null || cart.isEmpty()) return "redirect:/cart";

        User user = userService.findByEmail(userDetails.getUsername());
        boolean usingNewAddress = dto.getSavedAddressId() == null;

        // If using saved address, populate DTO fields
        if (!usingNewAddress) {
            DeliveryAddress saved = addressService.findById(dto.getSavedAddressId());
            dto.setRecipientName(saved.getRecipientName());
            dto.setAddressLine(saved.getAddressLine());
            dto.setCity(saved.getCity());
            dto.setDistrict(saved.getDistrict());
            dto.setPhone(saved.getPhone());
        } else {
            // Validate address fields only when using new address
            if (dto.getRecipientName() == null || dto.getRecipientName().isBlank()) {
                result.rejectValue("recipientName", "error.recipientName", "Recipient name is required");
            }
            if (dto.getAddressLine() == null || dto.getAddressLine().isBlank()) {
                result.rejectValue("addressLine", "error.addressLine", "Address line is required");
            }
            if (dto.getCity() == null || dto.getCity().isBlank()) {
                result.rejectValue("city", "error.city", "City is required");
            }
            if (dto.getPhone() == null || dto.getPhone().isBlank()) {
                result.rejectValue("phone", "error.phone", "Phone is required");
            }
        }

        if (result.hasErrors()) {
            populateCheckoutModel(user, cart, model, dto);
            return "checkout/checkout";
        }

        try {
            Order order = orderService.placeOrder(cart, dto, user);

            if (usingNewAddress && dto.isSaveAddress()) {
                addressService.addAddress(
                        user,
                        dto.getRecipientName(),
                        dto.getAddressLine(),
                        dto.getCity(),
                        dto.getDistrict(),
                        dto.getPhone(),
                        false
                );
            }

            session.removeAttribute("cart");
            flash.addFlashAttribute("success", "Order placed successfully!");
            return "redirect:/checkout/success/" + order.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            populateCheckoutModel(user, cart, model, dto);
            return "checkout/checkout";
        }
    }

    @GetMapping("/success/{orderId}")
    public String orderSuccess(@PathVariable Long orderId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Order order = orderService.findById(orderId);
        model.addAttribute("order", order);
        return "checkout/success";
    }
}
