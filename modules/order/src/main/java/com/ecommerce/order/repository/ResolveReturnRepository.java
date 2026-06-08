package com.ecommerce.order.repository;

import com.ecommerce.order.domain.ResolveReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ResolveReturnRepository extends JpaRepository<ResolveReturn, Long> {

    Optional<ResolveReturn> findByReturnRequestId(Long returnRequestId);

}