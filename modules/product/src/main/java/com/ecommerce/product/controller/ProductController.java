package com.ecommerce.product.controller;

import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

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
    public ResponseEntity<?> getHomeProducts() {
        return ResponseEntity.ok(productService.getAllActiveForHomePage());
    }

    @GetMapping("/seller/{sellerId}/active")
    public ResponseEntity<?> getProductsBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(productService.getAllActiveBySeller(sellerId));
    }
}