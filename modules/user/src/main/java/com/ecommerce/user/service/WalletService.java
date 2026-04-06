package com.ecommerce.user.service;

import com.ecommerce.user.domain.Wallet;

public interface WalletService {
    Wallet getWalletByUserId(Long userId);
    void changeBalance(Long userId, Double amount);

    boolean isBalanceEnough(Long userId, Double amount);
}