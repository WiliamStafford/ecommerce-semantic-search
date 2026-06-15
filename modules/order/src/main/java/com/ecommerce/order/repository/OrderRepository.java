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

    @Query("SELECT FUNCTION('YEAR', o.createdAt), SUM(o.totalPrice), COUNT(o.id) " +
           "FROM Order o " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "GROUP BY FUNCTION('YEAR', o.createdAt)")
    List<Object[]> getRevenueStatisticsByYear();

    @Query(value = "SELECT oi.product_name, SUM(oi.quantity) as sold_qty, SUM(oi.price * oi.quantity) as revenue " +
                   "FROM order_item oi " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE o.order_status = 'COMPLETED' " +
                   "GROUP BY oi.seller_product_id, oi.product_name " +
                   "ORDER BY sold_qty DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getProductStatistics();

    @Query("SELECT FUNCTION('YEAR', o.createdAt), SUM(o.totalPrice), COUNT(o.id) " +
           "FROM Order o " +
           "WHERE o.orderStatus = 'DELIVERED' AND o.sellerId = :sellerId " + // Đã sửa
           "GROUP BY FUNCTION('YEAR', o.createdAt)")
    List<Object[]> getRevenueStatisticsByYearForSeller(@Param("sellerId") Long sellerId);

    @Query(value = "SELECT oi.product_name, SUM(oi.quantity) as sold_qty, SUM(oi.price * oi.quantity) as revenue " +
                   "FROM order_item oi " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE o.order_status = 'DELIVERED' AND o.seller_id = :sellerId " + // Đã sửa
                   "GROUP BY oi.seller_product_id, oi.product_name " +
                   "ORDER BY sold_qty DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getProductStatisticsBySeller(@Param("sellerId") Long sellerId);

}