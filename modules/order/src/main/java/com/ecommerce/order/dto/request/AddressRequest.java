package com.ecommerce.order.dto.request;

public record AddressRequest(
        String province,
        String district,
        String ward,
        String street,
        String houseNumber
) {}