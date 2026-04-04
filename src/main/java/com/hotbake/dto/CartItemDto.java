package com.hotbake.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
public class CartItemDto {
    private String cartItemId; // Unique ID for this cart line item
    private Long productId;
    private String productName;
    private String shopName;
    private Long primaryImageId;
    private BigDecimal pricePerUnit; // Price per pound or per piece
    private BigDecimal quantity; // Pounds or pieces
    private boolean isByPieces; // True if sold by pieces (cupcake/tubcake)

    public BigDecimal getSubtotal() {
        if (pricePerUnit == null || quantity == null) return BigDecimal.ZERO;
        return pricePerUnit.multiply(quantity);
    }

    public CartItemDto(Long productId,
                       String productName,
                       String shopName,
                       Long primaryImageId,
                       BigDecimal pricePerUnit,
                       BigDecimal quantity,
                       boolean isByPieces) {
        this.cartItemId = UUID.randomUUID().toString();
        this.productId = productId;
        this.productName = productName;
        this.shopName = shopName;
        this.primaryImageId = primaryImageId;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
        this.isByPieces = isByPieces;
    }

    public String getQuantityLabel() { 
        return isByPieces ? "piece(s)" : "lb(s)"; 
    }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}

