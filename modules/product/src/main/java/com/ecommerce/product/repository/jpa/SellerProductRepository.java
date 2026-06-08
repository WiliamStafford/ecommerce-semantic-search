package com.ecommerce.product.repository.jpa;

import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.enums.SellerProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerProductRepository extends JpaRepository<SellerProduct, Long> {
     List<SellerProduct> findAllByStatus(SellerProductStatus status);
     List<SellerProduct> findAllBySellerIdAndStatus(Long sellerId, SellerProductStatus status);

     @Query("SELECT MIN(sp.price) FROM SellerProduct sp WHERE sp.productId = :productId AND sp.status = :status")
     Double findMinPriceByProductIdAndStatus(@Param("productId") Long productId, @Param("status") SellerProductStatus status);

    List<SellerProduct> findByProductId(Long id);
}