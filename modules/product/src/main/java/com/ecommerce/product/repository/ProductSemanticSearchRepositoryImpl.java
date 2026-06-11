package com.ecommerce.product.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ecommerce.product.dto.document.ProductDocument;
import com.ecommerce.product.dto.request.SearchProductsBySemanticQuery;
import com.ecommerce.product.dto.response.ProductSummaryProjection;
import com.ecommerce.product.dto.response.SearchProductsProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductSemanticSearchRepositoryImpl {

    private final ElasticsearchClient esClient;

    private static final String INDEX_NAME = "products_v3";
    private static final String EMBEDDING_FIELD = "vector";

    public SearchProductsProjection semanticSearch(SearchProductsBySemanticQuery query, float[] queryVector) {
        if (queryVector == null || queryVector.length == 0) {
            log.warn("Vector rỗng, fallback keyword search");
            return keywordOnlySearch(query);
        }

        String cleanQuery = query.q().toLowerCase().replaceAll("giá rẻ|giá mắc|giá cao", "").trim();
        int searchSize = query.size() > 0 ? query.size() : 20;

        List<Float> vectorList = IntStream.range(0, queryVector.length)
                .mapToObj(i -> queryVector[i]).collect(Collectors.toList());

        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .from(query.page() * searchSize)
                    .size(searchSize)
                    .knn(k -> k.field(EMBEDDING_FIELD)
                            .queryVector(vectorList)
                            .k(20)
                            .numCandidates(100)
                            .boost(2.0f))
                            .query(q -> q.bool(b -> b

                                    .should(s1 -> s1.match(m -> m
                                            .field("productName")
                                            .query(cleanQuery)
                                            .boost(5f)))

                                    .should(s1 -> s1.match(m -> m
                                            .field("description")
                                            .query(cleanQuery)
                                            .boost(2f)))
                                    .minimumShouldMatch("1")
                            ))
            );
            return executeSearch(searchRequest, query);
        } catch (Exception e) {
            log.error("Semantic Search Error: {}", e.getMessage());
            return new SearchProductsProjection(new ArrayList<>(), 0, query.page(), searchSize);
        }
    }

    public SearchProductsProjection keywordOnlySearch(SearchProductsBySemanticQuery query) {
        String cleanQuery = query.q().toLowerCase().replaceAll("giá rẻ|giá mắc|giá cao", "").trim();
        int searchSize = query.size() > 0 ? query.size() : 20;

        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .from(query.page() * searchSize)
                    .size(searchSize)
                    .query(q -> q.bool(b -> {
                        if (query.categoryId() != null && query.categoryId() > 0) {
                            b.filter(f -> f.term(t -> t.field("categoryId").value(query.categoryId())));
                        }
                        b.must(m -> m.match(mt -> mt.field("productName").query(cleanQuery).boost(2.0f)));
                        return b;
                    }))
            );
            return executeSearch(request, query);
        } catch (Exception e) {
            log.error("Keyword Search Error", e);
            return new SearchProductsProjection(new ArrayList<>(), 0, query.page(), searchSize);
        }
    }

//    private SearchProductsProjection executeSearch(SearchRequest request, SearchProductsBySemanticQuery query) throws Exception {
//        SearchResponse<ProductDocument> response = esClient.search(request, ProductDocument.class);
//        List<ProductSummaryProjection> products = response.hits().hits().stream()
//                .map(this::mapToProjection).collect(Collectors.toList());
//        int total = response.hits().total() != null ? (int) response.hits().total().value() : products.size();
//
//
//
//        return new SearchProductsProjection(products, total, query.page(), query.size() > 0 ? query.size() : 20);
//    }
private SearchProductsProjection executeSearch(
        SearchRequest request,
        SearchProductsBySemanticQuery query) throws Exception {

    SearchResponse<ProductDocument> response =
            esClient.search(request, ProductDocument.class);

    List<Hit<ProductDocument>> hits = response.hits().hits();

    log.info("========== SEARCH DEBUG ==========");
    log.info("Query: {}", query.q());

    for (int i = 0; i < hits.size(); i++) {

        Hit<ProductDocument> hit = hits.get(i);
        ProductDocument doc = hit.source();

        float score = hit.score() != null
                ? hit.score().floatValue()
                : 0f;

        String productName = doc != null
                ? doc.getProductName()
                : "NULL";

        Long productId = doc != null
                ? doc.getId()
                : -1L;

        log.info(
                "Rank={} | Score={} | Id={} | Product={}",
                i + 1,
                score,
                productId,
                productName
        );

        // Gap với document kế tiếp
        if (i < hits.size() - 1
            && hit.score() != null
            && hits.get(i + 1).score() != null) {

            float nextScore =
                    hits.get(i + 1).score().floatValue();

            float gap = score - nextScore;

            log.info(
                    "      GAP_TO_NEXT={}",
                    gap
            );
        }
    }

    log.info("==================================");

    List<ProductSummaryProjection> products = hits.stream()
            .map(this::mapToProjection)
            .collect(Collectors.toList());

    int total = response.hits().total() != null
            ? (int) response.hits().total().value()
            : products.size();

    return new SearchProductsProjection(
            products,
            total,
            query.page(),
            query.size() > 0 ? query.size() : 20
    );
}

    private ProductSummaryProjection mapToProjection(Hit<ProductDocument> hit) {
        ProductDocument doc = hit.source();
        if (doc == null) return new ProductSummaryProjection(0L, "Unknown Product", 0L, "", 0.0, 0.0f);
        return new ProductSummaryProjection(
                doc.getId(), doc.getProductName(), doc.getCategoryId(), doc.getAvatar(),
                doc.getPrice() != null ? doc.getPrice() : 0.0,
                hit.score() != null ? hit.score().floatValue() : 0.0f
        );
    }
}