package com.hotbake.service;

import com.hotbake.dto.SellerRegistrationDto;
import com.hotbake.enums.SellerStatus;
import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.model.SellerProfile;
import com.hotbake.model.User;
import com.hotbake.repository.SellerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SellerService {

    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private UserService userService;

    public SellerProfile applyAsSeller(SellerRegistrationDto dto, User user) {
        if (sellerProfileRepository.existsByUser(user)) {
            throw new IllegalArgumentException("You have already submitted a seller application.");
        }
        SellerProfile profile = new SellerProfile();
        profile.setUser(user);
        profile.setShopName(dto.getShopName());
        profile.setShopDescription(dto.getShopDescription());
        profile.setShopAddress(dto.getShopAddress());
        profile.setBusinessPhone(dto.getBusinessPhone());
        profile.setNidNumber(dto.getNidNumber());
        profile.setStatus(SellerStatus.PENDING);
        return sellerProfileRepository.save(profile);
    }

    public SellerProfile getSellerProfileByUser(User user) {
        return sellerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
    }

    public Optional<SellerProfile> findByUser(User user) {
        return sellerProfileRepository.findByUser(user);
    }

    public SellerProfile findById(Long id) {
        return sellerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
    }

    public List<SellerProfile> getPendingSellers() {
        return sellerProfileRepository.findByStatus(SellerStatus.PENDING);
    }

    public List<SellerProfile> getAllSellers() {
        return sellerProfileRepository.findAll();
    }

    public void approveSeller(Long sellerId) {
        SellerProfile profile = findById(sellerId);
        profile.setStatus(SellerStatus.APPROVED);
        sellerProfileRepository.save(profile);
        userService.grantSellerRole(profile.getUser());
    }

    public void rejectSeller(Long sellerId, String reason) {
        SellerProfile profile = findById(sellerId);
        profile.setStatus(SellerStatus.REJECTED);
        profile.setRejectionReason(reason);
        sellerProfileRepository.save(profile);
    }

    public long countAll() {
        return sellerProfileRepository.count();
    }

    public long countPending() {
        return sellerProfileRepository.findByStatus(SellerStatus.PENDING).size();
    }
}
