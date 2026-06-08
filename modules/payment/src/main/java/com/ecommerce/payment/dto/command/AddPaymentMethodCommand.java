package com.ecommerce.payment.dto.command;

import com.ecommerce.payment.enums.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AddPaymentMethodCommand(
        @NotNull(message = "USER_ID_REQUIRED")
        UUID userId,

        @NotNull(message = "PROVIDER_REQUIRED")
        PaymentProvider provider,

        @NotBlank(message = "EXTERNAL_TOKEN_REQUIRED")
        String externalToken,

        boolean makeDefault
) {

}
