package com.ecommerce.order.repository;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Order o SET o.orderStatus = :status WHERE o.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") OrderStatus status);

    Order findAllByOrderStatus(OrderStatus status);

    List<Order> findAllByUserId(Long userId);

    List<Order> findBySellerId(Long sellerId);

    @Query(value = "SELECT sr.shop_name, SUM(o.total_price), COUNT(o.id) " +
                   "FROM orders o " +
                   "LEFT JOIN seller_registrations sr ON o.seller_id = sr.user_id " +
                   "WHERE o.order_status = 'DELIVERED' " +
                   "GROUP BY sr.shop_name", nativeQuery = true)
    List<Object[]> calculateRevenueBySeller();
}