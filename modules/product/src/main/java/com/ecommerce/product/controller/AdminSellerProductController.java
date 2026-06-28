package com.ecommerce.product.controller;

import com.ecommerce.product.dto.request.SellerProductRequest;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/seller-products")
@RequiredArgsConstructor
public class AdminSellerProductController {

    private final ProductService productService;

    // 1. Xem danh sách sản phẩm của seller (dùng lại hàm hiện có của Service)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{sellerId}/dashboard")
    public ResponseEntity<?> getSellerDashboard(@PathVariable Long sellerId) {
        // Tận dụng hàm service đang dùng cho Seller
        return ResponseEntity.ok(productService.getAllActiveBySeller(sellerId));
    }

    // 2. Thêm sản phẩm (Truyền sellerId trực tiếp thay vì lấy từ token)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{sellerId}/add")
    public ResponseEntity<?> addSellerProduct(@PathVariable Long sellerId, @RequestBody SellerProductRequest request) {
        return ResponseEntity.ok(productService.addSellerProduct(sellerId, request));
    }

    // 3. Cập nhật sản phẩm
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{sellerId}/update")
    public ResponseEntity<?> updateSellerProduct(@PathVariable Long sellerId, @RequestBody SellerProductRequest request) {
        return ResponseEntity.ok(productService.updateSellerProduct(sellerId, request));
    }

    // 4. Xóa sản phẩm
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{sellerId}/delete/{productId}")
    public ResponseEntity<?> deleteSellerProduct(@PathVariable Long sellerId, @PathVariable Long productId) {
        return ResponseEntity.ok(productService.deleteSellerProduct(sellerId, productId));
    }
}