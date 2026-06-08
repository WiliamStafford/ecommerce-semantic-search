package com.ecommerce.product.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.ecommerce.common.service.PythonTextEmbeddingAdapter;
import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.dto.document.ProductDocument;
import com.ecommerce.product.repository.jpa.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSyncService {
    private final ProductRepository productRepository;
    private final SellerProductRepository sellerProductRepository;
    private final CategoryRepository categoryRepository;
    private final PythonTextEmbeddingAdapter pythonAdapter;
    private final ElasticsearchClient esClient;

    @Getter
    private volatile boolean isSyncing = false;


    @Transactional(readOnly = true)
    public int fullReSync() throws IOException {
        if (isSyncing) {
            log.warn("Tiến trình đồng bộ đang chạy, bỏ qua yêu cầu này.");
            return -1;
        }

        isSyncing = true;
        List<SellerProduct> allSellerProducts = sellerProductRepository.findAll();
        int totalProducts = allSellerProducts.size();
        AtomicInteger successCount = new AtomicInteger(0);
        int batchSize = 500;

        log.info("Bắt đầu đồng bộ {} sản phẩm vào Elasticsearch (Batch size: {})...", totalProducts, batchSize);

        try {
            BulkRequest.Builder br = new BulkRequest.Builder();
            int counter = 0;

            for (SellerProduct sp : allSellerProducts) {
                try {
                    Product product = productRepository.findById(sp.getProductId()).orElse(null);
                    if (product == null) continue;

                    float[] vector = pythonAdapter.getEmbedding(product.getProductName());
                    float[] finalVector = (vector != null && vector.length == 384) ? vector : new float[384];
                    Map<String, Object> docMap = new HashMap<>();
                    docMap.put("id", sp.getId());
                    docMap.put("productName", sp.getName() != null ? sp.getName() : product.getProductName());
                    docMap.put("price", sp.getPrice());
                    docMap.put("categoryId", product.getCategoryId());
                    docMap.put("description", product.getDescription());
                    docMap.put("avatar", sp.getImageUrl() != null ? sp.getImageUrl() : product.getAvatar());
                    docMap.put("vector", finalVector);

                    categoryRepository.findById(product.getCategoryId() != null ? product.getCategoryId() : -1L)
                            .ifPresentOrElse(cat -> docMap.put("categoryName", cat.getName()),
                                    () -> docMap.put("categoryName", "Chưa phân loại"));

                    // 3. Xây dựng Bulk Request
                    br.operations(op -> op.index(idx -> idx
                            .index("products_v3")
                            .id(sp.getId().toString())
                            .document(docMap)));

                    counter++;
                    if (counter % batchSize == 0) {
                        if (executeBulk(br)) {
                            successCount.addAndGet(batchSize);
                            br = new BulkRequest.Builder();
                            log.info("Đã đồng bộ {}/{} sản phẩm...", counter, totalProducts);
                        }
                    }
                } catch (Exception e) {
                    log.error("Lỗi xử lý sản phẩm ID {}: {}", sp.getId(), e.getMessage());
                }
            }

            if (counter % batchSize != 0) {
                if (executeBulk(br)) {
                    successCount.addAndGet(counter % batchSize);
                }
            }

        } finally {
            isSyncing = false;
        }

        log.info("Hoàn tất! Đồng bộ thành công {} sản phẩm.", successCount.get());
        return successCount.get();
    }

    private boolean executeBulk(BulkRequest.Builder br) throws IOException {
        BulkResponse result = esClient.bulk(br.build());
        if (result.errors()) {
            result.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("Lỗi tài liệu ID {}: {}", item.id(), item.error().reason());
                }
            });
            return false;
        }
        return true;
    }
}