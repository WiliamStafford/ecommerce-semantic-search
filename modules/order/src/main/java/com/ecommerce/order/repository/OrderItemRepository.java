package com.ecommerce.order.repository;

import com.ecommerce.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query(value = "SELECT COUNT(*) FROM review WHERE order_item_id = :orderItemId", nativeQuery = true)
    int countReviewByOrderItemId(@Param("orderItemId") Long orderItemId);

    @Query(value = "SELECT COUNT(*) FROM return_request WHERE order_item_id = :orderItemId", nativeQuery = true)
    int countReturnRequestByOrderItemId(@Param("orderItemId") Long orderItemId);

    List<OrderItem> findByOrderId(Long id);

}