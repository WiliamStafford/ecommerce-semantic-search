package com.ecommerce.user.dto.response;

import com.ecommerce.user.domain.User;
import com.ecommerce.user.dto.response.RoleResponse;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        String avatar,
        Integer age,
        String phone,
        boolean enabled,
        LocalDateTime createdAt,
        List<RoleResponse> roles,
        String province,
        String district,
        String ward,
        String street,
        String houseNumber
) {
    public static UserProfileResponse fromEntity(User user) {
        var address = user.getAddresses().stream().findFirst().orElse(null);

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatar(),
                user.getAge(),
                user.getPhone(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getRoles().stream()
                        .map(r -> new RoleResponse(r.getName()))
                        .toList(),
                address != null ? address.getProvince() : null,
                address != null ? address.getDistrict() : null,
                address != null ? address.getWard() : null,
                address != null ? address.getStreet() : null,
                address != null ? address.getHouseNumber() : null
        );
    }
}