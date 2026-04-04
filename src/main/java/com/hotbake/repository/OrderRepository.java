package com.hotbake.repository;

import com.hotbake.enums.OrderStatus;
import com.hotbake.model.Order;
import com.hotbake.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    List<Order> findByBuyerAndStatusOrderByCreatedAtDesc(User buyer, OrderStatus status);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.sellerProfile.id = :sellerId ORDER BY o.createdAt DESC")
    List<Order> findOrdersBySellerProfileId(@Param("sellerId") Long sellerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.sellerProfile.id = :sellerId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findOrdersBySellerProfileIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") OrderStatus status);

    long countByStatus(OrderStatus status);
}
