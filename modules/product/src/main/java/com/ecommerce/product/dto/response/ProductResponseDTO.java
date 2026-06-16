package com.ecommerce.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {
    private Long sellerProductId;
    private String productName;
    private double price;
    private String avatar;
    private String description;
    private double averageRating;
    private Integer stock;
    private boolean isFavorite;




}