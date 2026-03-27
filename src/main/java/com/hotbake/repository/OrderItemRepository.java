package com.hotbake.repository;

import com.hotbake.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.buyer.id = :buyerId AND oi.order.status = com.hotbake.enums.OrderStatus.DELIVERED")
    List<OrderItem> findDeliveredItemsByProductAndBuyer(@Param("productId") Long productId, @Param("buyerId") Long buyerId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.review IS NULL AND oi.order.buyer.id = :buyerId AND oi.order.status = com.hotbake.enums.OrderStatus.DELIVERED")
    List<OrderItem> findUnreviewedDeliveredItemsByBuyer(@Param("buyerId") Long buyerId);
}
