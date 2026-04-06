package com.ecommerce.order.domain;

import com.ecommerce.order.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_refund")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReturnRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_request_id", nullable = false, unique = true)
    private Long returnRequestId;

    @Column(name = "refund_amount", nullable = false)
    private Double refundAmount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status")
    private RefundStatus status = RefundStatus.COMPLETED;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now();
    }
}