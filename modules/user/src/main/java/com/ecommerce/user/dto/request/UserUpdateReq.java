package com.ecommerce.user.dto.request;

import org.springframework.lang.Nullable;

public record UserUpdateReq(
        @Nullable String fullName,
        @Nullable String avatar,
        @Nullable Integer age,
        @Nullable String phone
) {}