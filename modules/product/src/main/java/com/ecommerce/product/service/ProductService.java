package com.ecommerce.product.service;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.dto.request.ProductRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductRequest request);
    List<Product> getAll();
    Product getById(Long id);

    @Transactional
    Product updateProduct(Long id, ProductRequest request);

    @Transactional
    void deleteProduct(Long id);

    List<Product> searchSemantic(List<Double> queryVector);
    SellerProduct getSellerProductById(Long id);

    void changeSellerProductStatus(Long sellerProductId, boolean active);

    List<SellerProduct> getAllActiveForHomePage();

    List<SellerProduct> getAllActiveBySeller(Long sellerId);

    List<SellerProduct> getAllForAdmin();
}