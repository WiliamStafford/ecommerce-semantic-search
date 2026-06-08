package com.ecommerce.product.repository.jpa;

import com.ecommerce.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductNameIgnoreCase(String trim);

    List<Product> findByEmbeddingIsNull();
}