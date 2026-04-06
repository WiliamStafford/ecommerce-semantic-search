package com.ecommerce.product.service.impl;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.enums.SellerProductStatus;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.SellerProductRepository;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final SellerProductRepository sellerProductRepository;


    @Override
    @Transactional
    public Product createProduct(ProductRequest request) {
        try {
            String embeddingJson = request.vector() != null ?
                    objectMapper.writeValueAsString(request.vector()) : null;

            Product product = Product.builder()
                    .productName(request.productName())
                    .categoryId(request.categoryId())
                    .origin(request.origin())
                    .stock(request.stock() != null ? request.stock() : 0)
                    .size(request.size())
                    .description(request.description())
                    .embedding(embeddingJson)
                    .build();
            return productRepository.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi nạp sản phẩm: " + e.getMessage());
        }
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
    }

    @Transactional
    @Override
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getById(id);

        if (request.productName() != null) product.setProductName(request.productName());
        if (request.categoryId() != null) product.setCategoryId(request.categoryId());
        if (request.origin() != null) product.setOrigin(request.origin());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.size() != null) product.setSize(request.size());
        if (request.description() != null) product.setDescription(request.description());

        if (request.vector() != null) {
            try {
                product.setEmbedding(objectMapper.writeValueAsString(request.vector()));
            } catch (Exception ignored) {}
        }

        return productRepository.save(product);
    }

    @Transactional
    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("sản phẩm không tồn tại");
        }
        productRepository.deleteById(id);
    }



    @Override
    public SellerProduct getSellerProductById(Long id) {
        return sellerProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm của người bán (SellerProduct) với ID: " + id));
    }

    @Override
    public void changeSellerProductStatus(Long sellerProductId, boolean active) {
        SellerProduct sp = sellerProductRepository.findById(sellerProductId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gian hàng này!"));

        if (active) {
            sp.setStatus(SellerProductStatus.ACTIVE);
        } else {
            sp.setStatus(SellerProductStatus.INACTIVE);
        }
        sellerProductRepository.save(sp);
    }

    @Override
    public List<Product> searchSemantic(List<Double> queryVector) {
        return null;
    }


    @Override
    public List<SellerProduct> getAllActiveForHomePage() {
        return sellerProductRepository.findAllByStatus(SellerProductStatus.ACTIVE);
    }

    @Override
    public List<SellerProduct> getAllActiveBySeller(Long sellerId) {
        return sellerProductRepository.findAllBySellerIdAndStatus(sellerId, SellerProductStatus.ACTIVE);
    }
    @Override
    public List<SellerProduct> getAllForAdmin() {
        return sellerProductRepository.findAll();
    }
}