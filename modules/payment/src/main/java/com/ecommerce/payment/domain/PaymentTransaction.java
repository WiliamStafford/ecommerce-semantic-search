package com.ecommerce.payment.domain;

import com.ecommerce.payment.enums.PaymentProvider;
import com.ecommerce.payment.enums.PaymentTransactionStatus;
import com.ecommerce.payment.enums.PaymentTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "payment_transactions",
        indexes = {
                @Index(name = "idx_session_id", columnList = "session_id"),
                @Index(name = "idx_txn_reference", columnList = "gateway_txn_id")
        }
)
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // THAY ĐỔI QUAN TRỌNG: Dùng Object thay vì Long ID để hỗ trợ Builder và JPA Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private PaymentSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentTransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentTransactionStatus status;

    /**
     * Dùng để lưu PayerID (PayPal) hoặc BankCode (VNPay)
     */
    @Column(name = "gateway")
    private String providerReference;

    /**
     * Dùng để lưu token (PayPal) hoặc TransactionNo (VNPay)
     */
    @Column(name = "gateway_txn_id", nullable = false, unique = true, length = 100)
    private String txnReference;

    @Embedded
    private Money amount;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructor tùy chỉnh cho nghiệp vụ (Domain logic)
    public PaymentTransaction(
            PaymentSession session,
            PaymentProvider provider,
            PaymentTransactionType type,
            Money amount,
            String txnReference
    ) {
        this.session = Objects.requireNonNull(session, "Session cannot be null");
        this.provider = Objects.requireNonNull(provider, "Provider cannot be null");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.txnReference = Objects.requireNonNull(txnReference, "Transaction reference cannot be null");

        if (amount.isNonPositive()) {
            throw new RuntimeException("Invalid payment amount");
        }

        this.status = PaymentTransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // --- Business Methods ---

    public void markSuccess(String providerReference) {
        this.providerReference = providerReference;
        transitionTo(PaymentTransactionStatus.SUCCESS);
    }

    public void markFailed(String reason) {
        this.failureReason = reason;
        transitionTo(PaymentTransactionStatus.FAILED);
    }

    private void transitionTo(PaymentTransactionStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new RuntimeException("Invalid status transition from " + status + " to " + next);
        }
        this.status = next;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return status == PaymentTransactionStatus.SUCCESS;
    }

    public boolean isCharge() {
        return type == PaymentTransactionType.CHARGE;
    }
}