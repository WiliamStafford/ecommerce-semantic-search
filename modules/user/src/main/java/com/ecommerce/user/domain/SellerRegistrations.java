package com.ecommerce.user.domain;

import com.ecommerce.user.enums.RegistrationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRegistrations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private String shopName;
    private String address;
    private String description;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}