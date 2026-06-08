package com.ecommerce.product.service;

public interface UserLookupService {
    String getSellerName(Long sellerId);
    String getSellerEmail(Long sellerId);
}