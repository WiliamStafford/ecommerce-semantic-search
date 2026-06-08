package com.ecommerce.product.service.migration;// Đừng quên import CategoryRepository
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.ecommerce.common.service.PythonTextEmbeddingAdapter;
import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.document.ProductDocument;
import com.ecommerce.product.dto.request.BatchEmbedRequest;
import com.ecommerce.product.dto.request.ProductData;
import com.ecommerce.product.enums.SellerProductStatus;
import com.ecommerce.product.repository.jpa.CategoryRepository;
import com.ecommerce.product.repository.jpa.ProductRepository;
import com.ecommerce.product.repository.jpa.SellerProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException; // 🌟 Xử lý lỗi Unhandled exception
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@Slf4j
@RequiredArgsConstructor
public class DataMigrationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PythonTextEmbeddingAdapter pythonAdapter;
    private final ElasticsearchClient esClient;
    private final SellerProductRepository sellerProductRepository;
    private final RestTemplate restTemplate;

    private float[] convertListToFloatArray(List<Double> list) {
        if (list == null) return new float[0];
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i).floatValue();
        return arr;
    }

    public void syncSingleProduct(Product product) {
        try {
            float[] vector = pythonAdapter.getEmbedding(product.getProductName());

            String json = String.format(
                    "{\"id\": %d, \"productName\": \"%s\", \"vector\": %s}",
                    product.getId(),
                    product.getProductName(),
                    Arrays.toString(vector)
            );

            log.info("DEBUG: JSON thô chuẩn bị gửi: {}", json);
        } catch (Exception e) {
            log.error("Lỗi sync: ", e);
        }
    }

    private void syncToEs(Product product, float[] vector) throws IOException {
        Double minPrice = sellerProductRepository.findMinPriceByProductIdAndStatus(product.getId(), SellerProductStatus.ACTIVE);
        String catName = (product.getCategoryId() != null) ?
                categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse("Khác") : "Khác";

        Map<String, Object> docMap = new HashMap<>();
        docMap.put("id", product.getId());
        docMap.put("productName", product.getProductName());
        docMap.put("price", (minPrice != null) ? minPrice : 0.0);
        docMap.put("categoryId", product.getCategoryId());
        docMap.put("categoryName", catName);
        docMap.put("description", product.getDescription());
        docMap.put("avatar", product.getAvatar());
        docMap.put("vector", (vector != null) ? vector : new float[0]);

        esClient.index(i -> i.index("products_v3").id(product.getId().toString()).document(docMap));
    }

    @Scheduled(fixedDelay = 60000)
    public void updateMissingEmbeddings() {
        List<Product> products = productRepository.findByEmbeddingIsNull();
        if (products.isEmpty()) return;

        log.info("Đang xử lý {} sản phẩm thiếu vector...", products.size());

        List<ProductData> data = products.stream()
                .map(p -> {
                    String catName = (p.getCategoryId() != null) ?
                            categoryRepository.findById(p.getCategoryId()).map(Category::getName).orElse("Khác") : "Khác";
                    String semanticContent = buildSemanticContent(p, catName);
                    return new ProductData(p.getId(), semanticContent);
                })
                .collect(Collectors.toList());
        try {
            Map<String, Object> response = restTemplate.postForObject(
                    "http://127.0.0.1:8000/api/v1/batch-embed",
                    new BatchEmbedRequest(data),
                    Map.class
            );

            if (response == null || !response.containsKey("data")) {
                log.error("Phản hồi từ Python API không hợp lệ.");
                return;
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("data");
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

            for (Map<String, Object> result : results) {
                try {
                    Long productId = Long.valueOf(result.get("id").toString());
                    List<Double> vectorList = (List<Double>) result.get("vector");

                    if (vectorList == null || vectorList.isEmpty()) continue;

                    float[] vector = convertListToFloatArray(vectorList);

                    productRepository.findById(productId).ifPresent(p -> {
                        try {
                            p.setEmbedding(mapper.writeValueAsString(vectorList));
                            productRepository.save(p);

                            syncToEs(p, vector);
                            log.info("Đã đồng bộ thành công ID: {}", productId);
                        } catch (Exception e) {
                            log.error("Lỗi lưu DB/ES cho ID {}: {}", productId, e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    log.error("Lỗi xử lý một phần tử trong batch: {}", e.getMessage());
                }
            }
            log.info("Đã hoàn tất đợt đồng bộ vector.");

        } catch (Exception e) {
            log.error("Không thể kết nối Python API để lấy vector: {}", e.getMessage());
        }
    }

    public void deleteProductFromEs(Long id) {
    }
    public String buildSemanticContent(Product p, String catName) {
        return String.format(
                "Sản phẩm: %s | Danh mục: %s | Lợi ích: %s | Đặc điểm: %s",
                p.getProductName(),
                catName,
                (p.getDescription() != null ? p.getDescription() : "Nông sản sạch chất lượng cao"),
                (p.getOrigin() != null ? "Xuất xứ " + p.getOrigin() : "Tự nhiên")
        );
    }

    public void syncAllProducts() {
        log.info("Đang bắt đầu đồng bộ toàn bộ sản phẩm...");
        updateMissingEmbeddings();
    }
}