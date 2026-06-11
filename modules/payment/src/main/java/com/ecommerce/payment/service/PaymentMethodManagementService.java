package com.ecommerce.payment.service;


import com.ecommerce.payment.domain.StoredPaymentMethod;
import com.ecommerce.payment.dto.command.AddPaymentMethodCommand;
import com.ecommerce.payment.repository.PaymentStoredMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;@RequiredArgsConstructor
@Service
public class PaymentMethodManagementService {
    private final PaymentStoredMethodRepository paymentStoredMethodRepository;

    public void addStoredPaymentMethod(Long userId, AddPaymentMethodCommand command) {
        paymentStoredMethodRepository.findByProviderAndExternalToken(command.provider(), command.externalToken())
                .ifPresent(existing -> {
                    throw new RuntimeException("Payment method already exists");
                });

        StoredPaymentMethod newMethod = new StoredPaymentMethod(
                userId,
                command.provider(),
                command.externalToken()
        );

        boolean hasAnyMethod = paymentStoredMethodRepository.existsByUserId(userId);
        if (command.makeDefault() || !hasAnyMethod) {
            handleSetDefault(userId, newMethod);
        }

        paymentStoredMethodRepository.save(newMethod);
    }

    public void deactivatePaymentMethod(Long userId, Long methodId) {
        StoredPaymentMethod method = paymentStoredMethodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        if (!method.getUserId().equals(userId)) {
            throw new RuntimeException("NOT_ENOUGH_PERMISSION");
        }

        method.deactivate();
        paymentStoredMethodRepository.save(method);
    }

    public void setDefaultPaymentMethod(Long userId, Long methodId) {
        StoredPaymentMethod newDefault = paymentStoredMethodRepository.findById(methodId)
                .filter(m -> m.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        handleSetDefault(userId, newDefault);
        paymentStoredMethodRepository.save(newDefault);
    }

    public void deleteStoredPaymentMethod(Long userId, Long paymentMethodId) {
        var paymentMethod = paymentStoredMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        if (!paymentMethod.getUserId().equals(userId)) {
            throw new RuntimeException("NOT_ENOUGH_PERMISSION");
        }

        paymentStoredMethodRepository.delete(paymentMethod);
    }

    private void handleSetDefault(Long userId, StoredPaymentMethod newDefault) {
        paymentStoredMethodRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(StoredPaymentMethod::unsetDefault);

        newDefault.setDefault();
    }
}