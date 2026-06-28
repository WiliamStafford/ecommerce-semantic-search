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

        if (q.contains("rau") || q.contains("salad") || q.contains("cải") || q.contains("xà lách")) {
            categories.add(3);
        }

        if (q.contains("dừa") || q.contains("cam") || q.contains("trái cây") || q.contains("quả") || q.contains("bưởi") || q.contains("mít") || q.contains("ổi")) {
            categories.add(5);
        }

        if (q.contains("sầu riêng") || q.contains("mít") || q.contains("khóm")) {
            categories.add(7);
        }

        if (q.contains("lá") || q.contains("tiêu") || q.contains("thảo mộc") || q.contains("gia vị") || q.contains("tía tô") || q.contains("bạc hà")) {
            categories.add(6);
        }

        if (q.contains("não bộ") || q.contains("trí nhớ") || q.contains("bổ não") || q.contains("hạt sen")) {
            categories.add(12);
        }

        if (q.contains("hạt") || q.contains("hạnh nhân") || q.contains("hạt điều") || q.contains("hạt chia") || q.contains("óc chó") || q.contains("tim mạch")) {
            categories.add(13);
        }

        return categories;
    }
}