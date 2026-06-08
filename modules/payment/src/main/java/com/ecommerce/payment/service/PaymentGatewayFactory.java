package com.ecommerce.payment.service;

import com.ecommerce.payment.enums.PaymentProvider;
import com.ecommerce.payment.port.PaymentGatewayStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayFactory {

    private final Map<PaymentProvider, PaymentGatewayStrategy> strategies;

    public PaymentGatewayFactory(List<PaymentGatewayStrategy> strategyList) {
        strategies = strategyList.stream()
            .collect(Collectors.toMap(PaymentGatewayStrategy::getProvider, s -> s));
    }

    public PaymentGatewayStrategy get(PaymentProvider provider) {
        return Optional.ofNullable(strategies.get(provider))
            .orElseThrow(() -> new IllegalArgumentException("Cổng thanh toán không hỗ trợ: " + provider));
    }
}
