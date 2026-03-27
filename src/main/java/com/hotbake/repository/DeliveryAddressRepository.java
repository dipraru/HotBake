package com.hotbake.repository;

import com.hotbake.model.DeliveryAddress;
import com.hotbake.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    List<DeliveryAddress> findByUser(User user);
    Optional<DeliveryAddress> findByUserAndIsDefaultTrue(User user);
}
