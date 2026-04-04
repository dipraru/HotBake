package com.hotbake.service;

import com.hotbake.dto.ReviewDto;
import com.hotbake.enums.OrderStatus;
import com.hotbake.exception.UnauthorizedException;
import com.hotbake.model.*;
import com.hotbake.repository.OrderItemRepository;
import com.hotbake.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @InjectMocks private ReviewService reviewService;

    private User buyer;
    private OrderItem orderItem;
    private ReviewDto reviewDto;

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setId(1L);

        User otherBuyer = new User();
        otherBuyer.setId(2L);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.DELIVERED);

        Product product = new Product();
        product.setId(1L);

        orderItem = new OrderItem();
        orderItem.setId(10L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);

        reviewDto = new ReviewDto();
        reviewDto.setOrderItemId(10L);
        reviewDto.setRating(5);
        reviewDto.setComment("Absolutely delicious!");
    }

    @Test
    void addReview_notDelivered_throwsIllegalArgument() {
        orderItem.getOrder().setStatus(OrderStatus.PENDING);
        when(orderItemRepository.findById(10L)).thenReturn(Optional.of(orderItem));

        assertThatThrownBy(() -> reviewService.addReview(reviewDto, buyer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("delivered");
    }

    @Test
    void addReview_wrongBuyer_throwsUnauthorized() {
        User wrongBuyer = new User();
        wrongBuyer.setId(999L);
        when(orderItemRepository.findById(10L)).thenReturn(Optional.of(orderItem));

        assertThatThrownBy(() -> reviewService.addReview(reviewDto, wrongBuyer))
                .isInstanceOf(UnauthorizedException.class);
    }
}
