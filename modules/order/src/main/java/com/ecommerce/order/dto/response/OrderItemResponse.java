package com.ecommerce.order.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long sellerProductId;
    private Integer quantity;
    private Double price;

    private String productName;
    private String imageUrl;

    @JsonProperty("reviewed")
    private boolean reviewed;

    @JsonProperty("hasReturn")
    private boolean hasReturn;
}