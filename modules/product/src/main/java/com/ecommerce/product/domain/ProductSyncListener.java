package com.ecommerce.product.domain;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.ecommerce.common.service.PythonTextEmbeddingAdapter;
import com.ecommerce.common.util.BeanUtil;
import com.ecommerce.product.dto.document.ProductDocument;
import com.ecommerce.product.enums.SellerProductStatus;
import com.ecommerce.product.repository.jpa.ProductRepository;
import com.ecommerce.product.repository.jpa.SellerProductRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ProductSyncListener {

    private void handleSync(Object entity) {
        ProductRepository productRepository = BeanUtil.getBean(ProductRepository.class);
        SellerProductRepository sellerProductRepository = BeanUtil.getBean(SellerProductRepository.class);
        ElasticsearchClient esClient = BeanUtil.getBean(ElasticsearchClient.class);

        if (entity instanceof SellerProduct sp) {
            if (sp.getStatus() == SellerProductStatus.INACTIVE) {
                deleteFromEs(esClient, sp.getId());
            } else {
                syncSellerProduct(sp, productRepository, esClient);
            }
        } else if (entity instanceof Product p) {
            List<SellerProduct> sps = sellerProductRepository.findByProductId(p.getId());
            for (SellerProduct sp : sps) {
                syncSellerProduct(sp, productRepository, esClient);
            }
        }
    }

    @PostPersist
    @PostUpdate
    public void onPostPersistOrUpdate(Object entity) {
        CompletableFuture.runAsync(() -> handleSync(entity));
    }

    @PostRemove
    public void onPostRemove(Object entity) {
        if (entity instanceof SellerProduct sp) {
            ElasticsearchClient esClient = BeanUtil.getBean(ElasticsearchClient.class);
            deleteFromEs(esClient, sp.getId());
        }
    }

    private void deleteFromEs(ElasticsearchClient esClient, Long id) {
        try {
            esClient.delete(d -> d.index("products").id(id.toString()));
            log.info("⚡ Đã xóa/gỡ ID {} khỏi Elasticsearch", id);
        } catch (Exception e) {
            log.error("Lỗi xóa Elastic ID {}: {}", id, e.getMessage());
        }
    }

    public void syncSellerProduct(SellerProduct sellerProduct, ProductRepository productRepository, ElasticsearchClient esClient) {
        try {
            PythonTextEmbeddingAdapter pythonAdapter = BeanUtil.getBean(PythonTextEmbeddingAdapter.class);
            Product product = productRepository.findById(sellerProduct.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product gốc không tồn tại"));

            float[] vector = pythonAdapter.getEmbedding(product.getProductName());

            esClient.index(i -> i
                    .index("products_v3")
                    .id(sellerProduct.getId().toString())
                    .document(new ProductDocument(
                            product,
                            vector,
                            sellerProduct.getPrice()
                    ))
            );
            log.info(" Đồng bộ SellerProduct ID: {} thành công", sellerProduct.getId());
        } catch (Exception e) {
            log.error("Lỗi sync SellerProduct ID {}: {}", sellerProduct.getId(), e.getMessage());
        }
    }
}