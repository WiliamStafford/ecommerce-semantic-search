package com.ecommerce.payment.enums;

import java.util.List;

public enum PaymentSessionStatus {

    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    EXPIRED,
    CANCELLED;

    public boolean canAcceptTransaction() {
        return this == PENDING || this == PROCESSING;
    }

    public boolean canTransitionTo(PaymentSessionStatus next) {
        if (this == next) {
            return true;
        }

        return switch (this) {
            case PENDING ->
                List.of(
                    PROCESSING,
                    COMPLETED,
                    FAILED,
                    EXPIRED,
                    CANCELLED
                ).contains(next);

            case PROCESSING ->
                List.of(
                    COMPLETED,
                    FAILED,
                    EXPIRED,
                    CANCELLED
                ).contains(next);

            case COMPLETED,
                 FAILED,
                 EXPIRED,
                 CANCELLED -> false;
        };
    }

    public boolean isFinalState() {
        return switch (this) {
            case COMPLETED, FAILED, EXPIRED, CANCELLED -> true;
            default -> false;
        };
    }
}
