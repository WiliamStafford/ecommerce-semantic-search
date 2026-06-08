package com.ecommerce.product.service;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.request.SellerProductRequest;
import com.ecommerce.product.dto.response.ProductDetailResponse;
import com.ecommerce.product.dto.response.ProductHomeResponse;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.product.dto.response.ProductResponseDTO;

import java.util.List;

public interface ProductService {


    Product createProduct(ProductRequest request);
    List<Product> getAll();
    Product getById(Long id);

    @Transactional
    Product updateProduct(Long id, ProductRequest request);

    @Transactional
    void deleteProduct(Long id);


    List<ProductHomeResponse> getAllActiveForHomePage();

    List<Product> searchSemantic(List<Double> queryVector);
    SellerProduct getSellerProductById(Long id);

    void changeSellerProductStatus(Long sellerProductId, boolean active);




    List<SellerProduct> getAllActiveBySeller(Long sellerId);

    List<SellerProduct> getAllForAdmin();

    ProductDetailResponse getProductDetail(Long id);

    void updateProductAverageRating(Long productId, Double newAverageRating);

    List<SellerProduct> getAllSellerProducts();

    SellerProduct addSellerProduct(Long currentSellerId, com.ecommerce.product.dto.request.SellerProductRequest request);

    SellerProduct updateSellerProduct(Long currentSellerId, SellerProductRequest request);

    SellerProduct deleteSellerProduct(Long currentSellerId, Long id);

    ProductResponseDTO getProductById(Long productSellerId);

}