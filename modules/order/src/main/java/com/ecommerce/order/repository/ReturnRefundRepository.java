package com.ecommerce.order.repository;

import com.ecommerce.order.domain.ReturnRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReturnRefundRepository extends JpaRepository<ReturnRefund, Long> {
    Optional<ReturnRefund> findByReturnRequestId(Long returnRequestId);
    boolean existsByReturnRequestId(Long returnRequestId);
}