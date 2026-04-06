package com.ecommerce.user.service.impl;

import com.ecommerce.user.domain.Wallet;
import com.ecommerce.user.repository.WalletRepository;
import com.ecommerce.user.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ví người dùng không tồn tại!"));
    }

    @Override
    @Transactional
    public void changeBalance(Long userId, Double amount) {
        Wallet wallet = getWalletByUserId(userId);

        double newBalance = wallet.getBalance() + amount;

        if (newBalance < 0) {
            throw new RuntimeException("Số dư ví không đủ để thực hiện giao dịch!");
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
    }

    @Override
    public boolean isBalanceEnough(Long userId, Double amount) {
        Wallet wallet = getWalletByUserId(userId);
        return wallet.getBalance() >= Math.abs(amount);
    }
}