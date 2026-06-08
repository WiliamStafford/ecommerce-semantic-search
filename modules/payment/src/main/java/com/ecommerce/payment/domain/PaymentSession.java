package com.ecommerce.payment.domain;


import com.ecommerce.payment.enums.PaymentSessionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payment_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Database tự sinh ID
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Embedded
    private Money totalAmount;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "session_id")
    private List<PaymentTransaction> transactions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentSessionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    public PaymentSession(
            Long orderId,
            Money totalAmount,
            String idempotencyKey
    ) {
        if (orderId == null || totalAmount == null || idempotencyKey == null) {
            throw new RuntimeException("Invalid payment session data");
        }

        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentSessionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void addTransaction(
        PaymentTransaction txn
    ) {

        if (!status.canAcceptTransaction()) {

//            throw new DomainException(
//                ErrorCode.INVALID_SESSION_STATE
//            );
            throw new RuntimeException("Invalid session state");
        }


        if (txn.isCharge()
            && hasChargeTransaction()) {

//            throw new DomainException(
//                ErrorCode.DUPLICATE_CHARGE_TRANSACTION
//            );
            throw new RuntimeException("Duplicate charge transaction");
        }


        if (txn.isCharge()
            && totalAmount.getAmount()
                   .compareTo(
                       txn.getAmount().getAmount()
                   ) != 0
        ) {

//            throw new DomainException(
//                ErrorCode.PAYMENT_AMOUNT_MISMATCH
//            );
            throw new RuntimeException("Payment amount mismatch");
        }


        transactions.add(txn);


        if (txn.isSuccess()) {

            markCompleted();
        }
    }


    public void startProcessing() {

        transitionTo(
            PaymentSessionStatus.PENDING
        );
    }


    public void markCompleted() {

        boolean hasSuccessTxn =
            transactions.stream()
                .anyMatch(
                    PaymentTransaction::isSuccess
                );


        if (!hasSuccessTxn) {

//            throw new DomainException(
//                ErrorCode.NO_SUCCESSFUL_TRANSACTION
//            );
            throw new RuntimeException("No successful transaction");
        }


        transitionTo(
            PaymentSessionStatus.COMPLETED
        );
    }


    public void fail() {

        transitionTo(
            PaymentSessionStatus.FAILED
        );
    }


    public void expire() {

        transitionTo(
            PaymentSessionStatus.EXPIRED
        );
    }




    public void cancel() {
        if (status.isFinalState()) {
//            throw new DomainException(ErrorCode.INVALID_STATUS_TRANSITION);
        throw new RuntimeException("Invalid status transition");
        }
        transitionTo(PaymentSessionStatus.CANCELLED);
    }


    private void transitionTo(
        PaymentSessionStatus next
    ) {

        if (!status.canTransitionTo(next)) {
            throw new RuntimeException("Invalid status transition");
//            throw new DomainException(
//                ErrorCode.INVALID_STATUS_TRANSITION
//            );
        }


        this.status = next;
    }


    private boolean hasChargeTransaction() {

        return transactions.stream()
            .anyMatch(
                PaymentTransaction::isCharge
            );
    }

    public Long getAmount() {
        return totalAmount.getAmount().longValue();
    }
}
