package com.ecommerce.product.repository.jpa;

import com.ecommerce.product.domain.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.userId = :userId AND w.sellerProductId = :sellerProductId")
    void deleteByUserIdAndSellerProductId(@Param("userId") Long userId, @Param("sellerProductId") Long sellerProductId);

    List<Wishlist> findAllByUserId(Long userId);

    boolean existsByUserIdAndSellerProductId(Long userId, Long sellerProductId);

    void deleteBySellerProductId(Long id);
}