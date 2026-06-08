package com.ecommerce.product.dto.document;

import com.ecommerce.product.domain.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String productName;

    @Builder.Default
    private Double price = 0.0;

    private Long categoryId;
    private String categoryName;
    private String description;
    private String avatar;

    @JsonProperty("vector")
    private float[] vector;

    public ProductDocument(Product product, float[] vector, Double price) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.price = (price != null) ? price : 0.0;
        this.categoryId = product.getCategoryId();
        this.description = product.getDescription();
        this.avatar = product.getAvatar();
        this.vector = (vector != null && vector.length == 384) ? vector : new float[384];
    }
}