package com.hotbake.service;

import com.hotbake.dto.ReviewDto;
import com.hotbake.enums.OrderStatus;
import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.exception.UnauthorizedException;
import com.hotbake.model.OrderItem;
import com.hotbake.model.Review;
import com.hotbake.model.User;
import com.hotbake.repository.OrderItemRepository;
import com.hotbake.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    public Review addReview(ReviewDto dto, User buyer) {
        OrderItem orderItem = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        if (!orderItem.getOrder().getBuyer().getId().equals(buyer.getId())) {
            throw new UnauthorizedException("This order does not belong to you.");
        }
        if (orderItem.getOrder().getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("You can only review delivered orders.");
        }
        if (reviewRepository.existsByOrderItemIdAndDeletedFalse(dto.getOrderItemId())) {
            throw new IllegalArgumentException("You have already reviewed this item.");
        }

        Review review = new Review();
        review.setProduct(orderItem.getProduct());
        review.setBuyer(buyer);
        review.setOrderItem(orderItem);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        return reviewRepository.save(review);
    }

    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndDeletedFalse(productId);
    }

    public List<Review> getBuyerReviews(Long buyerId) {
        return reviewRepository.findByBuyerIdAndDeletedFalse(buyerId);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findByDeletedFalse();
    }

    public void deleteReview(Long reviewId, User buyer) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getBuyer().getId().equals(buyer.getId())) {
            throw new UnauthorizedException("You can only delete your own reviews.");
        }
        review.setDeleted(true);
        reviewRepository.save(review);
    }

    public void adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setDeleted(true);
        reviewRepository.save(review);
    }

    public List<OrderItem> getReviewableItems(User buyer) {
        return orderItemRepository.findUnreviewedDeliveredItemsByBuyer(buyer.getId());
    }

    public boolean hasReviewForOrderItem(Long orderItemId) {
        return reviewRepository.existsByOrderItemIdAndDeletedFalse(orderItemId);
    }
}
