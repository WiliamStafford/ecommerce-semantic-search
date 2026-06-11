package com.ecommerce.order.domain;

import com.ecommerce.order.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resolve_returns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long returnRequestId;
    private Long sellerId;
    private String sellerName;
    private Long customerId;
    private String customerName;
    private String customerEvidenceUrl;
    private String sellerRefundProofUrl;
    private Long orderId;
    private Long orderItemId;
    private Double refundAmount;
    @Column(columnDefinition = "TEXT")
    private String resolutionNote;

    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    private ReturnStatus finalStatus;
}