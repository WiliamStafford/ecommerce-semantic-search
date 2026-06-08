package com.ecommerce.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class AddPaymentMethodCommand {
    private UUID userId;

    @NotBlank(message = "PROVIDER_REQUIRED")
    private String provider;

    @NotBlank(message = "EXTERNAL_TOKEN_REQUIRED")
    private String externalToken;

    private boolean makeDefault;
}
