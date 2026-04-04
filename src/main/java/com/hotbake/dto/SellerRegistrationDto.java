package com.hotbake.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SellerRegistrationDto {

    @NotBlank(message = "Shop name is required")
    private String shopName;

    @NotBlank(message = "Shop description is required")
    private String shopDescription;

    @NotBlank(message = "Shop address is required")
    private String shopAddress;

    @NotBlank(message = "Business phone is required")
    private String businessPhone;

    @NotBlank(message = "NID number is required")
    private String nidNumber;
}
