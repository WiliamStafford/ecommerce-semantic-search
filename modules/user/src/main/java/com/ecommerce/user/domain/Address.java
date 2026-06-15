package com.ecommerce.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;      // Người nhận
    private String phone;         // Số điện thoại nhận
    private String province;      // Tỉnh/Thành phố
    private String district;      // Quận/Huyện
    private String ward;          // Phường/Xã
    private String street;        // Đường
    private String houseNumber;   // Số nhà

    private boolean isDefault;    // Đánh dấu địa chỉ mặc định

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}