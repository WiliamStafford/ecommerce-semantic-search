package com.ecommerce.product.domain;

import com.ecommerce.product.enums.SellerProductStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@EntityListeners(ProductSyncListener.class)
@Table(name = "seller_products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SellerProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name")
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "sku", nullable = false, length = 255)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SellerProductStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = SellerProductStatus.ACTIVE;
        }
        if (this.unit == null || this.unit.trim().isEmpty()) {
            this.unit = "kg";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getSellerProductId() {
        return productId;
    }

    public String getProductName() {
        return this.name;
    }

    public String getAvatar() {
        return this.imageUrl;
    }
}