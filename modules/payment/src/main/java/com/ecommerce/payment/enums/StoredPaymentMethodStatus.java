package com.ecommerce.payment.enums;

public enum StoredPaymentMethodStatus {

    ACTIVE,
    INACTIVE,
    REMOVED;

    public boolean canTransitionTo(
        StoredPaymentMethodStatus next
    ) {

        return switch (this) {

            case ACTIVE -> next == INACTIVE ||
                           next == REMOVED;

            case INACTIVE -> next == ACTIVE ||
                             next == REMOVED;

            case REMOVED -> false;
        };
    }

}
