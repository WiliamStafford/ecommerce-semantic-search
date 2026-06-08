package com.ecommerce.payment.enums;

import java.util.List;

public enum PaymentTransactionStatus {

    PENDING,
    AUTHORIZED,
    CAPTURED,
    SUCCESS,
    FAILED,
    EXPIRED,
    CANCELLED;

    public boolean canTransitionTo(
        PaymentTransactionStatus next
    ) {

        return switch (this) {

            case PENDING -> List.of(
                AUTHORIZED,
                SUCCESS,
                FAILED,
                EXPIRED,
                CANCELLED
            ).contains(next);

            case AUTHORIZED -> List.of(
                CAPTURED,
                CANCELLED,
                FAILED
            ).contains(next);

            case CAPTURED -> next == SUCCESS;

            case SUCCESS,
                 FAILED,
                 EXPIRED,
                 CANCELLED -> false;
        };
    }
}
