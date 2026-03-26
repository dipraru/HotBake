package com.hotbake.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pounds;

    @Column(name = "price_per_pound", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerPound;

    public CartItem() {}

    public CartItem(Cart cart, Product product, BigDecimal pounds, BigDecimal pricePerPound) {
        this.cart = cart;
        this.product = product;
        this.pounds = pounds;
        this.pricePerPound = pricePerPound;
    }

    public BigDecimal getSubtotal() {
        return pounds.multiply(pricePerPound);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getPounds() { return pounds; }
    public void setPounds(BigDecimal pounds) { this.pounds = pounds; }

    public BigDecimal getPricePerPound() { return pricePerPound; }
    public void setPricePerPound(BigDecimal pricePerPound) { this.pricePerPound = pricePerPound; }
}
