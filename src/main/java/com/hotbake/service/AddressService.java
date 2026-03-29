package com.hotbake.service;

import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.exception.UnauthorizedException;
import com.hotbake.model.DeliveryAddress;
import com.hotbake.model.User;
import com.hotbake.repository.DeliveryAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {

    @Autowired private DeliveryAddressRepository addressRepository;

    public List<DeliveryAddress> getUserAddresses(User user) {
        return addressRepository.findByUser(user);
    }

    public DeliveryAddress getDefaultAddress(User user) {
        return addressRepository.findByUserAndIsDefaultTrue(user).orElse(null);
    }

    public DeliveryAddress addAddress(User user, String recipientName, String addressLine,
                                      String city, String district, String phone, boolean makeDefault) {
        if (makeDefault) {
            addressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(a -> { a.setDefault(false); addressRepository.save(a); });
        }
        DeliveryAddress address = new DeliveryAddress();
        address.setUser(user);
        address.setRecipientName(recipientName);
        address.setAddressLine(addressLine);
        address.setCity(city);
        address.setDistrict(district);
        address.setPhone(phone);
        address.setDefault(makeDefault || addressRepository.findByUser(user).isEmpty());
        return addressRepository.save(address);
    }

    public void deleteAddress(Long addressId, User user) {
        DeliveryAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't own this address.");
        }
        addressRepository.delete(address);
    }

    public void setDefault(Long addressId, User user) {
        addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(a -> { a.setDefault(false); addressRepository.save(a); });
        DeliveryAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't own this address.");
        }
        address.setDefault(true);
        addressRepository.save(address);
    }

    public DeliveryAddress findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }
}
