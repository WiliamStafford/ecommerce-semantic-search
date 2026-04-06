package com.ecommerce.product.controller;

import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/A_Product")
@RequiredArgsConstructor
public class A_ProductController {

    private final ProductService productService;

    @PatchMapping("/{id}/hide")
    public ResponseEntity<?> hideProduct(@PathVariable Long id) {
        productService.changeSellerProductStatus(id, false);
        return ResponseEntity.ok("Admin đã ẩn sản phẩm ID: " + id);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<?> activeProduct(@PathVariable Long id) {
        productService.changeSellerProductStatus(id, true);
        return ResponseEntity.ok("Admin đã kích hoạt lại sản phẩm ID: " + id);
    }
}