package com.ecommerce.user.event;

/**
 * Event này được module 'product' phát ra
 * khi cần truy vấn thông tin người dùng từ 'user' module.
 */
public record UserRequestEvent(Long sellerId) {
}