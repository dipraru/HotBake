package com.hotbake.repository;

import com.hotbake.enums.SellerStatus;
import com.hotbake.model.SellerProfile;
import com.hotbake.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUser(User user);
    Optional<SellerProfile> findByUserId(Long userId);
    List<SellerProfile> findByStatus(SellerStatus status);
    boolean existsByUser(User user);
}
