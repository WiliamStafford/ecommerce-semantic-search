package com.ecommerce.product.repository.jpa;

import com.ecommerce.product.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 🔒 [TƯỜNG LỬA 1]: Cờ kiểm tra trùng nội bộ của JPA (Giữ nguyên vì không dùng Native Query)
    boolean existsByOrderItemId(Long orderItemId);

    // 🌟 ĐÃ SỬA: Đổi sang kiểu INT và chỉ dùng COUNT(*) thuần túy
    // Biện pháp này bẻ gãy hoàn toàn lỗi ClassCastException Integer sang Boolean từ MySQL native query
    @Query(value = "SELECT COUNT(*) FROM return_request WHERE order_item_id = :orderItemId", nativeQuery = true)
    int countReturnRequestByOrderItemId(@Param("orderItemId") Long orderItemId);

    @Query(value = """
    SELECT COUNT(o.id) FROM orders o 
    JOIN order_item oi ON o.id = oi.order_id 
    WHERE oi.id = :orderItemId 
    AND o.order_status = 'DELIVERED'
""", nativeQuery = true)
    int countDeliveredOrderItems(@Param("orderItemId") Long orderItemId);

    // Các hàm bốc danh sách đánh giá phục vụ giao diện hiển thị
    List<Review> findAllByProductId(Long productId);

    List<Review> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}