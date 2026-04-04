package com.hotbake.repository;

import com.hotbake.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndDeletedFalse(Long productId);
    List<Review> findByBuyerIdAndDeletedFalse(Long buyerId);
    boolean existsByOrderItemIdAndDeletedFalse(Long orderItemId);
    List<Review> findByDeletedFalse();
}
