package com.hotbake.service;

import com.hotbake.dto.CartItemDto;
import com.hotbake.model.Cart;
import com.hotbake.model.CartItem;
import com.hotbake.model.Product;
import com.hotbake.model.User;
import com.hotbake.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private ProductService productService;

    /**
     * Get or create cart for user from database
     */
    public Map<Long, CartItemDto> getCartForUser(User user) {
        Optional<Cart> optionalCart = cartRepository.findByUser(user);
        Map<Long, CartItemDto> result = new HashMap<>();

        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            for (CartItem item : cart.getItems()) {
                CartItemDto dto = new CartItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSellerProfile().getShopName(),
                        item.getProduct().getPrimaryImage() != null ? item.getProduct().getPrimaryImage().getId() : null,
                        item.getPricePerPound(),
                        item.getPounds(),
                        false  // persistent cart items are always pounds
                );
                result.put(item.getProduct().getId(), dto);
            }
        }
        return result;
    }

    /**
     * Save cart items to database for user
     */
    @Transactional
    public void saveCart(User user, Map<Long, CartItemDto> cartMap) {
        Optional<Cart> optionalCart = cartRepository.findByUser(user);
        Cart cart = optionalCart.orElseGet(() -> {
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        });

        // Clear existing items
        cart.getItems().clear();

        // Add new items
        for (CartItemDto dto : cartMap.values()) {
            Product product = productService.findById(dto.getProductId());
            CartItem item = new CartItem(cart, product, dto.getQuantity(), dto.getPricePerUnit());
            cart.getItems().add(item);
        }

        cartRepository.save(cart);
    }

    /**
     * Clear cart for user
     */
    @Transactional
    public void clearCart(User user) {
        Optional<Cart> optionalCart = cartRepository.findByUser(user);
        if (optionalCart.isPresent()) {
            cartRepository.delete(optionalCart.get());
        }
    }
}
