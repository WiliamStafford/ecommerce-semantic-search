package com.ecommerce.payment.domain;


import com.ecommerce.payment.enums.PaymentProvider;
import com.ecommerce.payment.enums.StoredPaymentMethodStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "stored_payment_methods",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_external_token",
                        columnNames = {"provider", "external_token"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProvider provider;

    @Column(name = "external_token", nullable = false) //value object
    private String externalToken;

    @Column(nullable = false)
    private boolean isDefault;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoredPaymentMethodStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public StoredPaymentMethod(
            Long userId,
            PaymentProvider provider,
            String externalToken
    ) {
        if (externalToken == null || externalToken.isBlank()) {
            throw new RuntimeException("Invalid payment provider token");
        }
        this.userId = userId;
        this.provider = provider;
        this.externalToken = externalToken;
        this.status = StoredPaymentMethodStatus.ACTIVE;
        this.isDefault = false;
        this.createdAt = LocalDateTime.now();
    }


    public void setDefault() {

        if (!isActive()) {
            throw new RuntimeException("Inactive method cannot be default");
//            throw new DomainException(
//                    ErrorCode.INACTIVE_METHOD_CANNOT_BE_DEFAULT
//            );
        }

        this.isDefault = true;
    }


    public void unsetDefault() {
        this.isDefault = false;
    }


    public void deactivate() {

        if (isDefault) {
            throw new RuntimeException("Default method cannot be deactivated");
//            throw new DomainException(
//                    ErrorCode.DEFAULT_METHOD_CANNOT_BE_DEACTIVATED
//            );
        }

        this.status = StoredPaymentMethodStatus.INACTIVE;
    }


    public boolean isActive() {
        return status == StoredPaymentMethodStatus.ACTIVE;
    }

}
