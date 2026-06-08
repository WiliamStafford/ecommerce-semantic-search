package com.ecommerce.product.dto.request;

public record SellerProductRequest(
        Long productId,       // ID của sản phẩm cốt lõi hệ thống (Rau, Củ, Quả...)
        String name,
        Long categoryId,// Tên hiển thị riêng của gian hàng
        String imageUrl,      // Link ảnh nông sản
        Double price,         // Giá bán
        Integer stock,        // Số lượng nhập kho
        String description,   // Ghi chú / Mô tả sản phẩm
        String sku,           // Mã SKU quản lý kho (Ví dụ: SKU-TAO-001)
        String unit
) {}