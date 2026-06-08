package com.ecommerce.payment.domain;


import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Embeddable
@Getter
public class Money   {

    private BigDecimal amount;

    private String currency;

    protected Money() {
    }

    public Money(BigDecimal amount, String currency) {

        if (amount == null ||
            amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
//            throw new DomainException(
//                ErrorCode.INVALID_PAYMENT_AMOUNT
//            );
        }

        this.amount = amount;
        this.currency = currency;
    }

//    @Override
//    protected List<Object> getEqualityComponents() {
//        return List.of(amount.stripTrailingZeros(), currency);
//    }

    public boolean isSameAmountAs(BigDecimal otherAmount) {
        if (otherAmount == null) return false;
        return this.amount.compareTo(otherAmount) == 0;
    }

    public boolean isNonPositive() { // bỏ vào constructor
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }
}
