package com.ecommerce.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Integer age;
    private boolean enabled;
    private String province;
    private String district;
    private String ward;
    private String street;
    private String houseNumber;
}