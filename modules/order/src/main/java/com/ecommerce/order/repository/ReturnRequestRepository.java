package com.ecommerce.order.repository;

import com.ecommerce.order.domain.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository; // Cần kế thừa cái này
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    boolean existsByOrderItemId(Long orderItemId);
    List<ReturnRequest> findAllByCustomerId(Long customerId);

    List<ReturnRequest> findAllBySellerId(Long sellerId);
}