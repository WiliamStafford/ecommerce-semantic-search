package com.ecommerce.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponse {
    private Long id;
    private String productName;
    private String sku;
    private Double price;
    private String avatar;
    private String description;
    private Integer stock;

    private Long sellerId;
    private String sellerName;
    private String sellerEmail;
}