package com.ecommerce.user.dto.response;

import com.ecommerce.user.domain.SellerRegistrations;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerRegistrationDTO {
    private Long id;
    private String shopName;
    private String address;
    private String description;
    private String status;
    private String userEmail;
    private String userId;

    public static SellerRegistrationDTO fromEntity(SellerRegistrations reg) {
        return SellerRegistrationDTO.builder()
                .id(reg.getId())
                .shopName(reg.getShopName())
                .address(reg.getAddress())
                .description(reg.getDescription())
                .status(String.valueOf(reg.getStatus()))
                .userEmail(reg.getUser().getEmail())
                .userId(String.valueOf(reg.getUser().getId()))
                .build();
    }
}