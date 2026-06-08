package com.ecommerce.payment.repository;

import com.ecommerce.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {


    Optional<PaymentTransaction> findByProviderReference(String reference);


}
