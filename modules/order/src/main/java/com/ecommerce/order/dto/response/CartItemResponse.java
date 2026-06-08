package com.ecommerce.order.dto.response;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.product.domain.SellerProduct;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private Long sellerProductId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
    private String imageUrl;

    public static CartItemResponse fromEntity(CartItem item, SellerProduct product) {
        return CartItemResponse.builder()
                .id(item.getId()) // Gán ID
                .sellerProductId(item.getSellerProductId())
                .productName(product.getProductName())
                .quantity(item.getQuantity())
                .price(product.getPrice())
                .totalPrice(product.getPrice() * item.getQuantity())
                .imageUrl(product.getAvatar())
                .build();
    }
}