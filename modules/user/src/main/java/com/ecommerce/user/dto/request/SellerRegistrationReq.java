package com.ecommerce.user.dto.request;

public record SellerRegistrationReq(
        String shopName,
        String address,
        String description
) {
}