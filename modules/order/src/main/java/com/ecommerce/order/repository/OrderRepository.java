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

@Query(value = "SELECT COALESCE(sr.shop_name, 'Unknown Shop'), " +
               "COALESCE(sr.user_id, 0), " +
               "SUM(o.total_price), " +
               "COUNT(o.id) " +
               "FROM orders o " +
               "INNER JOIN seller_registrations sr ON o.seller_id = sr.user_id " + // Use INNER JOIN
               "WHERE o.order_status = 'DELIVERED' " +
               "GROUP BY sr.shop_name, sr.user_id", nativeQuery = true)
List<Object[]> calculateRevenueBySeller();

    @Query("SELECT FUNCTION('YEAR', o.createdAt), SUM(o.totalPrice), COUNT(o.id) " +
           "FROM Order o " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "GROUP BY FUNCTION('YEAR', o.createdAt)")
    List<Object[]> getRevenueStatisticsByYear();

    @Query(value = "SELECT oi.product_name, SUM(oi.quantity) as sold_qty, SUM(oi.price * oi.quantity) as revenue " +
                   "FROM order_items oi " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE o.order_status = 'COMPLETED' " +
                   "GROUP BY oi.seller_product_id, oi.product_name " +
                   "ORDER BY sold_qty DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getProductStatistics();
    // 1. Thống kê theo năm (Native Query)
    @Query(value = "SELECT YEAR(o.created_at) as time_period, " +
                   "COALESCE(SUM(o.total_price), 0.0), COALESCE(COUNT(o.id), 0) " +
                   "FROM orders o " +
                   "WHERE o.order_status = 'DELIVERED' AND o.seller_id = :sellerId " +
                   "GROUP BY YEAR(o.created_at)", nativeQuery = true)
    List<Object[]> getRevenueStatisticsByYearForSeller(@Param("sellerId") Long sellerId);

    // 2. Thống kê sản phẩm (Native Query)
    @Query(value = "SELECT oi.product_name, " +
                   "COALESCE(SUM(oi.quantity), 0), " +
                   "COALESCE(SUM(oi.price * oi.quantity), 0.0) " +
                   "FROM order_items oi " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE o.order_status = 'DELIVERED' AND o.seller_id = :sellerId " +
                   "GROUP BY oi.product_name " +
                   "ORDER BY SUM(oi.quantity) DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getProductStatisticsBySeller(@Param("sellerId") Long sellerId);

    @Query(value = "SELECT p.id, " +
                   "oi.product_name, " +
                   "COALESCE(SUM(oi.quantity), 0), " +
                   "COALESCE(SUM(oi.price * oi.quantity), 0.0) " +
                   "FROM order_items oi " +
                   "JOIN products p ON oi.product_name = p.product_name " + // Nối qua tên sản phẩm
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE o.order_status = 'DELIVERED' AND o.seller_id = :sellerId " +
                   "GROUP BY p.id, oi.product_name " +
                   "ORDER BY SUM(oi.quantity) DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getProductStatisticsBySellerForAdmin(@Param("sellerId") Long sellerId);
    // 3. Thống kê linh hoạt theo Period (Native Query)
    @Query(value = "SELECT " +
                   "CASE " +
                   "  WHEN :periodType = 'MONTH' THEN MONTH(o.created_at) " +
                   "  WHEN :periodType = 'WEEK' THEN WEEK(o.created_at) " +
                   "  ELSE YEAR(o.created_at) " +
                   "END as time_period, " +
                   "COALESCE(SUM(o.total_price), 0.0) as revenue, " +
                   "COALESCE(COUNT(o.id), 0) as count " +
                   "FROM orders o " +
                   "WHERE o.order_status = 'DELIVERED' AND o.seller_id = :sellerId " +
                   "GROUP BY time_period", nativeQuery = true)
    List<Object[]> getStatsByPeriod(@Param("sellerId") Long sellerId, @Param("periodType") String periodType);


}