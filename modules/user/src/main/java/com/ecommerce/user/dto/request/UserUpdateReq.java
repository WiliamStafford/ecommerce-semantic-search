package com.ecommerce.user.dto.request;

import org.springframework.lang.Nullable;
public record UserUpdateReq(
        String fullName,
        String phone,
        String avatar,
        Integer age,
        String province,
        String district,
        String ward,
        String street,
        String houseNumber
) {}