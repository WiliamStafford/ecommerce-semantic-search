package com.ecommerce.payment.repository;

import com.ecommerce.payment.domain.PaymentSession;
import com.ecommerce.payment.dto.response.PaymentHistoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentSessionRepository extends JpaRepository<PaymentSession, Long> {

    Optional<PaymentSession> findByOrderId(Long orderId);

    Optional<PaymentSession> findByIdempotencyKey(String idempotencyKey);
    @Query(value = "SELECT s.id AS sessionId, " +
                   "       s.order_id AS orderId, " +
                   "       s.amount AS amount, " +
                   "       t.provider AS provider, " +
                   "       s.status AS status, " +
                   "       s.created_at AS createdAt, " +
                   "       t.gateway_txn_id AS transactionId, " +
                   "       t.created_at AS paymentDate " +
                   "FROM payment_sessions s " +
                   "JOIN orders o ON s.order_id = o.id " +
                   "LEFT JOIN payment_transactions t ON t.session_id = s.id " +
                   "WHERE o.user_id = :userId " +
                   "ORDER BY s.created_at DESC", nativeQuery = true)
    List<PaymentHistoryProjection> findPaymentHistoryByUserId(@Param("userId") Long userId);
}