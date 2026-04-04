package com.hotbake.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CheckoutDto {
    private Long savedAddressId;
    private boolean saveAddress = false;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String deliveryNote;

    private boolean useNewAddress = false;
}
