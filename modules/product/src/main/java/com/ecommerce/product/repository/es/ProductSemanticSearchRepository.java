package com.ecommerce.product.repository.es;

import com.ecommerce.product.domain.Product;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSemanticSearchRepository extends ElasticsearchRepository<Product, Long> {
    @Query("{\"knn\": {\"field\": \"vector\", \"query_vector\": ?0, \"k\": ?1, \"num_candidates\": 100}}")
    SearchHits<Product> findByVector(float[] vector, int k);
}
