package com.ecommerce.payment.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentSessionStatus, String> {

    @Override
    public String convertToDatabaseColumn(PaymentSessionStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public PaymentSessionStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return PaymentSessionStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}