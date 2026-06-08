package com.ecommerce.product.dto.response;

public record SellerProductItemResponse(
        Long id,              // ID thực tế của bản ghi trong bảng seller_products
        String productName,   // Tên nông sản của người bán (sp.getName())
        double price,         // Giá bán cá nhân hóa (sp.getPrice())
        int stockQuantity,    // Số lượng tồn kho thực tế (sp.getStock())
        String avatar         // Đường dẫn ảnh sản phẩm (sp.getImageUrl())
) {}