package com.hotbake.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CheckoutDto {
    private Long savedAddressId;
    private boolean saveAddress = false;

    private String recipientName;
    private String addressLine;
    private String city;
    private String district;
    private String phone;

    private String deliveryNote;
    private boolean useNewAddress = false;
}
