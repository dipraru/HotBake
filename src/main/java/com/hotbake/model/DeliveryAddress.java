package com.hotbake.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "delivery_addresses")
@Getter @Setter @NoArgsConstructor
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String addressLine;

    @Column(nullable = false)
    private String city;

    private String district;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private boolean isDefault = false;

    public String getFullAddress() {
        return addressLine + ", " + city + (district != null ? ", " + district : "");
    }
}
