package com.ecommerce.product.controller;

import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.response.ProductHomeResponse;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final com.ecommerce.common.security.CurrentUserProvider currentUserProvider;

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @PostMapping("/semantic-search")
    public ResponseEntity<?> search(@RequestBody List<Double> queryVector) {
        return ResponseEntity.ok(productService.searchSemantic(queryVector));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductHomeResponse>> getHomeProducts() {
        log.info(">>>> [CONTROLLER] Khách hàng gọi API tải danh sách Trang chủ - Tự động check Wishlist");
        List<ProductHomeResponse> responses = productService.getAllActiveForHomePage();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/seller/{sellerId}/active")
    public ResponseEntity<?> getProductsBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(productService.getAllActiveBySeller(sellerId));
    }


    @CrossOrigin("*")
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
        log.info("Đang lấy chi tiết cho SellerProduct ID: {}", id);
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @PostMapping("/seller/add")
    public ResponseEntity<?> addSellerProduct(@RequestBody com.ecommerce.product.dto.request.SellerProductRequest request) {
        log.info(">>>> [CONTROLLER] Người bán đang thực hiện đăng sản phẩm mới");

        Long currentSellerId = currentUserProvider.getCurrentUserId();
        if (currentSellerId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(productService.addSellerProduct(currentSellerId, request));
    }
    // PUT: Cập nhật thông tin sản phẩm người bán
    @PutMapping("/seller/update")
    public ResponseEntity<?> updateSellerProduct(@RequestBody com.ecommerce.product.dto.request.SellerProductRequest request) {
        log.info(">>>> [CONTROLLER] Người bán đang cập nhật sản phẩm");

        Long currentSellerId = currentUserProvider.getCurrentUserId();
        if (currentSellerId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(productService.updateSellerProduct(currentSellerId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSellerProduct(@PathVariable Long id) {
        log.info(">>>> [CONTROLLER] Người bán đang xóa sản phẩm ID: {}", id);

        Long currentSellerId = currentUserProvider.getCurrentUserId();
        if (currentSellerId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(productService.deleteSellerProduct(currentSellerId, id));
    }





}