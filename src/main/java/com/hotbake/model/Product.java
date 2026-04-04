package com.hotbake.model;

import com.hotbake.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.LazyInitializationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerProfile sellerProfile;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerPound;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    // Helpers
    public ProductImage getPrimaryImage() {
        try {
            return images.stream()
                    .filter(ProductImage::isPrimary)
                    .findFirst()
                    .orElse(images.isEmpty() ? null : images.get(0));
        } catch (LazyInitializationException e) {
            return null;
        }
    }

    public double getAverageRating() {
        try {
            List<Review> activeReviews = reviews.stream()
                    .filter(r -> !r.isDeleted())
                    .toList();
            if (activeReviews.isEmpty()) return 0;
            return activeReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0);
        } catch (LazyInitializationException e) {
            return 0;
        }
    }

    public long getReviewCount() {
        try {
            return reviews.stream().filter(r -> !r.isDeleted()).count();
        } catch (LazyInitializationException e) {
            return 0;
        }
    }
}
