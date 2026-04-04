package com.hotbake.service;

import com.hotbake.dto.CartItemDto;
import com.hotbake.dto.CheckoutDto;
import com.hotbake.enums.OrderStatus;
import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.exception.UnauthorizedException;
import com.hotbake.model.*;
import com.hotbake.repository.OrderRepository;
import com.hotbake.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    public Order placeOrder(Map<String, CartItemDto> cart, CheckoutDto dto, User buyer) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }

        // Check if buyer is trying to order their own products
        for (CartItemDto item : cart.values()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            if (product.getSellerProfile().getUser().getId().equals(buyer.getId())) {
                throw new IllegalArgumentException("You cannot purchase your own products.");
            }
        }

        String address;
        String phone;

        if (dto.getSavedAddressId() != null && !dto.isUseNewAddress()) {
            // Address is already resolved in controller; use dto fields
            address = dto.getAddressLine() + ", " + dto.getCity()
                    + (dto.getDistrict() != null && !dto.getDistrict().isBlank() ? ", " + dto.getDistrict() : "");
            phone = dto.getPhone();
        } else {
            address = dto.getAddressLine() + ", " + dto.getCity()
                    + (dto.getDistrict() != null && !dto.getDistrict().isBlank() ? ", " + dto.getDistrict() : "");
            phone = dto.getPhone();
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setDeliveryAddress(address);
        order.setDeliveryPhone(phone);
        order.setDeliveryNote(dto.getDeliveryNote());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItemDto item : cart.values()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPounds(item.getQuantity());
            orderItem.setPricePerPound(item.getPricePerUnit());
            BigDecimal subtotal = item.getPricePerUnit().multiply(item.getQuantity());
            orderItem.setSubtotal(subtotal);
            total = total.add(subtotal);
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<Order> getBuyerOrders(User buyer) {
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    public List<Order> getSellerOrders(Long sellerId) {
        return orderRepository.findOrdersBySellerProfileId(sellerId);
    }

    public List<Order> getSellerOrdersByStatus(Long sellerId, OrderStatus status) {
        return orderRepository.findOrdersBySellerProfileIdAndStatus(sellerId, status);
    }

    public void updateOrderStatus(Long orderId, OrderStatus newStatus, String cancellationReason, SellerProfile seller) {
        Order order = findById(orderId);
        boolean sellerOwnsOrder = order.getItems().stream()
                .anyMatch(i -> i.getProduct().getSellerProfile().getId().equals(seller.getId()));
        if (!sellerOwnsOrder) {
            throw new UnauthorizedException("You don't have access to this order.");
        }

        OrderStatus current = order.getStatus();
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("This order can no longer be updated.");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            String reason = cancellationReason == null ? "" : cancellationReason.trim();
            if (reason.isBlank()) {
                throw new IllegalArgumentException("Cancellation reason is required.");
            }
            if (reason.length() > 50) {
                throw new IllegalArgumentException("Cancellation reason must be 50 characters or less.");
            }
            if (!(current == OrderStatus.PENDING || current == OrderStatus.CONFIRMED)) {
                throw new IllegalArgumentException("Only pending or confirmed orders can be cancelled.");
            }
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancellationReason(reason);
            order.setCancelledAt(LocalDateTime.now());
            orderRepository.save(order);
            return;
        }

        OrderStatus expectedNext = order.getNextStatusForSeller();
        if (expectedNext == null || newStatus != expectedNext) {
            throw new IllegalArgumentException("Invalid status transition.");
        }

        order.setStatus(newStatus);
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(now);
            case PROCESSING -> order.setProcessingAt(now);
            case SHIPPED -> order.setShippedAt(now);
            case DELIVERED -> order.setDeliveredAt(now);
            default -> {
            }
        }
        orderRepository.save(order);
    }

    public void cancelOrder(Long orderId, User buyer) {
        Order order = findById(orderId);
        if (!order.getBuyer().equals(buyer)) {
            throw new UnauthorizedException("You don't own this order.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot cancel this order.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason("Cancelled by buyer");
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    public long countAll() { return orderRepository.count(); }
    public long countByStatus(OrderStatus status) { return orderRepository.countByStatus(status); }
}
