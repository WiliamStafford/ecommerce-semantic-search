package com.ecommerce.product.controller;

import com.ecommerce.product.service.ProductSyncService;
import com.ecommerce.product.service.migration.DataMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {
    private final DataMigrationService dataMigrationService;
    private final ProductSyncService productSyncService;

    @PostMapping("/migrate-vectors")
    public ResponseEntity<String> migrateVectors() {
        CompletableFuture.runAsync(() -> {
            log.info("Đang bắt đầu tạo vector cho toàn bộ sản phẩm...");
            dataMigrationService.syncAllProducts();
            log.info("Đã tạo vector thành công!");
        });
        return ResponseEntity.accepted().body("Đang tạo vector trong nền...");
    }

    @PostMapping("/sync")
    public ResponseEntity<String> sync() {
        if (productSyncService.isSyncing()) {
            return ResponseEntity.status(409).body("Tiến trình đang chạy, vui lòng đợi!");
        }

        CompletableFuture.runAsync(() -> {
            try {
                productSyncService.fullReSync();
            } catch (IOException e) {
                log.error("Lỗi đồng bộ ngầm: {}", e.getMessage());
            }
        });

        return ResponseEntity.accepted().body("Tiến trình đồng bộ đã bắt đầu.");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return productSyncService.isSyncing()
                ? ResponseEntity.ok("Đang đồng bộ...")
                : ResponseEntity.ok("Hệ thống đang rảnh.");
    }
}