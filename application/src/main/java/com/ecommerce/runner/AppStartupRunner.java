package com.ecommerce.runner;

import com.ecommerce.product.service.ProductSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppStartupRunner implements CommandLineRunner {

    private final ProductSyncService productSyncService;

    @Override
    public void run(String... args) throws IOException {
        log.info("⚡ Đang tự động kích hoạt quá trình đồng bộ hóa khi khởi động...");
        productSyncService.fullReSync();
    }
}