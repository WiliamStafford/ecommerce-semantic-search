package com.ecommerce.user.dto.response;

import com.ecommerce.user.domain.User;

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
        List<RoleResponse> roles
) {
    public static UserProfileResponse fromEntity(User user) {
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
                        .toList()
        );
    }
}

