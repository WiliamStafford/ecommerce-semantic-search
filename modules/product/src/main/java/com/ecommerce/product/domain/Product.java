package com.ecommerce.product.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
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

    @Column(columnDefinition = "TEXT")
    private String embedding;

    @Transient
    private float[] vector;

    @PostLoad
    public void convertJsonToFloatVector() {
        if (this.embedding != null && !this.embedding.equals("[]") && !this.embedding.isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                this.vector = mapper.readValue(this.embedding, float[].class);
            } catch (Exception e) {
                this.vector = new float[384];
            }
        } else {
            this.vector = new float[384];
        }
    }
}