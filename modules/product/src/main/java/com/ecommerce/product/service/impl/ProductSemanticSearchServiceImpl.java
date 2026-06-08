package com.ecommerce.product.service.impl;

import com.ecommerce.common.service.PythonTextEmbeddingAdapter;
import com.ecommerce.product.dto.request.SearchProductsBySemanticQuery;
import com.ecommerce.product.dto.response.ProductSummaryProjection;
import com.ecommerce.product.dto.response.SearchProductsProjection;
import com.ecommerce.product.repository.ProductSemanticSearchRepositoryImpl;
import com.ecommerce.product.service.ProductSemanticSearchUseCase;
import com.ecommerce.product.service.RerankAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSemanticSearchServiceImpl implements ProductSemanticSearchUseCase {

    private final ProductSemanticSearchRepositoryImpl semanticSearchRepository;
    private final PythonTextEmbeddingAdapter embeddingAdapter;
    private final RerankAdapter rerankAdapter;

    @Override
    public SearchProductsProjection execute(SearchProductsBySemanticQuery query) {
        try {
            List<Integer> inferredCategories = inferCategories(query.q());
            Integer primaryCategoryId = (!inferredCategories.isEmpty()) ? inferredCategories.get(0) : query.categoryId();

            SearchProductsBySemanticQuery refinedQuery = new SearchProductsBySemanticQuery(
                    query.q(), primaryCategoryId, query.page(), query.size()
            );

            float[] vector = embeddingAdapter.getEmbedding(refinedQuery.q());
            SearchProductsProjection rawResult = semanticSearchRepository.semanticSearch(refinedQuery, vector);

            if (rawResult.getProducts() == null || rawResult.getProducts().isEmpty()) {
                log.info("Kết quả trống với category {}, thử tìm toàn cục...", primaryCategoryId);
                SearchProductsBySemanticQuery globalQuery = new SearchProductsBySemanticQuery(query.q(), null, query.page(), query.size());
                rawResult = semanticSearchRepository.semanticSearch(globalQuery, vector);
            }

            List<ProductSummaryProjection> rawList = rawResult.getProducts();
            if (rawList == null || rawList.isEmpty()) return rawResult;

            int limit = Math.min(rawList.size(), 15);
            List<ProductSummaryProjection> candidates = rawList.subList(0, limit);

            try {
                List<ProductSummaryProjection> rerankedList = rerankAdapter.rerank(
                        refinedQuery.q(),
                        candidates,
                        refinedQuery.categoryId()
                );
                if (rerankedList != null && !rerankedList.isEmpty()) {
                    return new SearchProductsProjection(rerankedList, rawResult.getTotal(), rawResult.getPage(), rawResult.getSize());
                }
            } catch (Exception rerankEx) {
                log.warn("Reranker lỗi (fallback về kết quả thô): {}", rerankEx.getMessage());
            }

            return rawResult;

        } catch (Exception e) {
            log.error("Hệ thống AI lỗi nghiêm trọng, chuyển sang Keyword Search: ", e);
            return semanticSearchRepository.keywordOnlySearch(query);
        }
    }

    private List<Integer> inferCategories(String query) {
        String q = query.toLowerCase();
        List<Integer> categories = new ArrayList<>();

        if (q.contains("rau") || q.contains("salad")) categories.add(100);
        if (q.contains("nước") || q.contains("dừa") || q.contains("cam") || q.contains("trái cây")) categories.add(200);
        if (q.contains("lá") || q.contains("tiêu") || q.contains("hạt") || q.contains("gia vị")) categories.add(300);
        if (q.contains("não bộ") || q.contains("trí nhớ") || q.contains("bổ não") || q.contains("thông minh")) categories.add(600);

        return categories;
    }
}