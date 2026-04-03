package com.hotbake.service;

import com.hotbake.enums.OrderStatus;
import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.exception.UnauthorizedException;
import com.hotbake.model.Order;
import com.hotbake.model.User;
import com.hotbake.repository.OrderRepository;
import com.hotbake.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @InjectMocks private OrderService orderService;

    private User buyer;
    private Order order;

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setId(1L);
        buyer.setFirstName("Karim");
        buyer.setLastName("Ali");

        order = new Order();
        order.setId(1L);
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setItems(new ArrayList<>());
    }

    @Test
    void findById_existingOrder_returnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_nonExisting_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelOrder_pendingOrderByOwner_cancelsSuccessfully() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        orderService.cancelOrder(1L, buyer);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_byWrongUser_throwsUnauthorized() {
        User anotherUser = new User();
        anotherUser.setId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, anotherUser))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void cancelOrder_deliveredOrder_throwsIllegalArgument() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, buyer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot cancel");
    }
}
