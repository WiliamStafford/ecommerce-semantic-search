package com.ecommerce.product.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    private Long categoryId;
    private String origin;
    private Integer stock;
    private String size;
    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "json")
    private String embedding;


}