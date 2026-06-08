package com.ecommerce.payment.repository;

import com.ecommerce.payment.domain.StoredPaymentMethod;
import com.ecommerce.payment.enums.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
// Đổi UUID thành Long ở kiểu Generic thứ hai
public interface PaymentStoredMethodRepository extends JpaRepository<StoredPaymentMethod, Long> {

    boolean existsByUserId(Long userId);

    Optional<StoredPaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<StoredPaymentMethod> findByProviderAndExternalToken(PaymentProvider provider, String token);
}
