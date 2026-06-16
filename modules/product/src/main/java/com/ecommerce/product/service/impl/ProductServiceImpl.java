package com.ecommerce.product.service.impl;

import com.ecommerce.common.security.JwtCurrentUserProvider;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.request.SellerProductRequest;
import com.ecommerce.product.dto.response.ProductDetailResponse;
import com.ecommerce.product.dto.response.ProductHomeResponse;
import com.ecommerce.product.dto.response.ProductResponseDTO;
import com.ecommerce.product.enums.SellerProductStatus;
import com.ecommerce.product.repository.jpa.ProductRepository;
import com.ecommerce.product.repository.jpa.SellerProductRepository;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.product.service.WishlistService;
import com.ecommerce.product.service.migration.DataMigrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final SellerProductRepository sellerProductRepository;
    private final WishlistService wishlistService;
    private final JwtCurrentUserProvider currentUserProvider;
    private final com.ecommerce.common.service.CloudinaryService cloudinaryService;
    private final DataMigrationService dataMigrationService;
    private final com.ecommerce.product.service.UserLookupService userLookupService;

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

            Product savedProduct = productRepository.save(product);
            dataMigrationService.syncSingleProduct(savedProduct);
            return savedProduct;
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
            } catch (Exception ignored) {
            }
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
    public List<ProductHomeResponse> getAllActiveForHomePage() {
        List<SellerProduct> sellerProducts = sellerProductRepository.findAllByStatus(SellerProductStatus.ACTIVE);

        if (sellerProducts.isEmpty()) return List.of();
        Set<Long> productIds = sellerProducts.stream()
                .map(SellerProduct::getProductId)
                .collect(Collectors.toSet());
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Long userId = currentUserProvider.getCurrentUserId();
        Set<Long> favoriteSellerProductIds = (userId != null)
                ? wishlistService.getFavoriteSellerProductIds(userId)
                : Set.of();

        return sellerProducts.stream().map(sp -> {
            Product p = productMap.get(sp.getProductId());

            return new ProductHomeResponse(
                    sp.getId(),
                    (sp.getName() != null && !sp.getName().isBlank()) ? sp.getName() : (p != null ? p.getProductName() : "Không xác định"),
                    sp.getPrice(),
                    p != null ? p.getAvatar() : null,
                    p != null ? p.getOrigin() : "N/A",
                    4.8,
                    favoriteSellerProductIds.contains(sp.getId())
            );
        }).collect(toList());
    }

    @Override
    public List<Product> searchSemantic(List<Double> queryVector) {
        return null;
    }

    @Override
    public List<SellerProduct> getAllActiveBySeller(Long sellerId) {
        return sellerProductRepository.findAllBySellerIdAndStatus(sellerId, SellerProductStatus.ACTIVE);
    }

    @Override
    public List<SellerProduct> getAllForAdmin() {
        return sellerProductRepository.findAll();
    }

    @Override
    public ProductDetailResponse getProductDetail(Long id) {
        SellerProduct sp = sellerProductRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));

        Product p = productRepository.findById(sp.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin sản phẩm gốc"));

        String sellerName = userLookupService.getSellerName(sp.getSellerId());
        String sellerEmail = userLookupService.getSellerEmail(sp.getSellerId());

        return ProductDetailResponse.builder()
                .id(sp.getId())
                .productName(sp.getName() != null ? sp.getName() : p.getProductName())
                .sku(sp.getSku())
                .price(sp.getPrice())
                .avatar(sp.getImageUrl() != null ? sp.getImageUrl() : p.getAvatar())
                .description(p.getDescription())
                .stock(sp.getStock())
                .sellerId(sp.getSellerId())
                .sellerName(sellerName)
                .sellerEmail(sellerEmail)
                .build();
    }

    @Override
    @Transactional
    public void updateProductAverageRating(Long sellerProductId, Double newRating) {
        SellerProduct sellerProduct = sellerProductRepository.findById(sellerProductId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm của người bán"));

        sellerProductRepository.save(sellerProduct);
    }

    @Override
    public List<SellerProduct> getAllSellerProducts() {
        return sellerProductRepository.findAll();
    }

    @Override
    @Transactional
    public SellerProduct addSellerProduct(Long sellerId, SellerProductRequest request) {
        log.info(">>>> [SERVICE] Bắt đầu chuỗi logic rà soát danh mục lõi cho sản phẩm: {}", request.name());

        Product coreProduct = productRepository.findByProductNameIgnoreCase(request.name().trim())
                .orElseGet(() -> {
                    log.info(">>>> [SERVICE] Nông sản '{}' chưa tồn tại trong danh mục hệ thống. Khởi tạo Product gốc ngay!", request.name());

                    Product newCore = Product.builder()
                            .productName(request.name().trim())
                            .avatar(request.imageUrl())
                            .categoryId(1L)
                            .description(request.description() != null && !request.description().isBlank()
                                    ? request.description().trim()
                                    : "Sản phẩm nông sản sạch mới đăng bán trên hệ thống FruitFresh")
                            .stock(request.stock())
                            .embedding("[]")
                            .build();

                    return productRepository.save(newCore);
                });

        Long finalProductId = coreProduct.getId();
        log.info(">>>> [SERVICE] Xác định thành công core productId: {} kết nối với gian hàng", finalProductId);

        SellerProduct newSellerProduct = SellerProduct.builder()
                .sellerId(sellerId)
                .productId(finalProductId)
                .name(request.name().trim())
                .imageUrl(request.imageUrl())
                .price(request.price())
                .stock(request.stock())
                .unit(request.unit() != null && !request.unit().isBlank() ? request.unit().trim() : "kg")
                .sku("SKU-" + System.currentTimeMillis())
                .status(SellerProductStatus.ACTIVE)
                .build();

        log.info(">>>> [SERVICE] Lưu thành công bản ghi vào bảng 'seller_products' với mã SKU: {}", newSellerProduct.getSku());
        return sellerProductRepository.save(newSellerProduct);
    }

    @Override
    @Transactional
    public SellerProduct updateSellerProduct(Long currentSellerId, SellerProductRequest request) {
        SellerProduct sp = sellerProductRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm!")); //


        sp.setName(request.name().trim());
        sp.setPrice(request.price());
        sp.setStock(request.stock());
        if (request.imageUrl() != null) sp.setImageUrl(request.imageUrl());
        if (request.unit() != null && !request.unit().isBlank()) sp.setUnit(request.unit().trim());

        productRepository.findById(sp.getProductId()).ifPresent(coreProduct -> {
            boolean isCoreUpdated = false;

            if (request.imageUrl() != null && !request.imageUrl().equals(coreProduct.getAvatar())) {
                coreProduct.setAvatar(request.imageUrl());
                isCoreUpdated = true;
            }
            if (request.description() != null && !request.description().trim().equals(coreProduct.getDescription())) {
                coreProduct.setDescription(request.description().trim());
                isCoreUpdated = true;
            }

            if (isCoreUpdated) {
                productRepository.save(coreProduct);
                try {
                    dataMigrationService.syncSingleProduct(coreProduct);
                } catch (Exception e) {
                    log.error("Lỗi đồng bộ sản phẩm {}: {}", coreProduct.getId(), e.getMessage());
                }
            }
        });

        return sellerProductRepository.save(sp);
    }


    @Override
    @Transactional
    public SellerProduct deleteSellerProduct(Long currentSellerId, Long id) {
        log.info(">>>> [SERVICE] Seller {} đang yêu cầu xóa cứng sản phẩm ID: {}", currentSellerId, id);

        SellerProduct sp = sellerProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (!sp.getSellerId().equals(currentSellerId)) {
            throw new RuntimeException("Bạn không có quyền xóa sản phẩm này!");
        }

        try {
            wishlistService.deleteBySellerProductId(id);
            dataMigrationService.deleteProductFromEs(id);
            sellerProductRepository.delete(sp);

            log.info("🗑 Đã xóa cứng thành công SellerProduct ID: {}", id);
        } catch (Exception e) {
            log.error(" Lỗi xóa cứng sản phẩm ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Không thể xóa cứng sản phẩm: " + e.getMessage());
        }

        return sp;
    }

    @Override
    public ProductResponseDTO getProductById(Long productSellerId) {
        SellerProduct sp = sellerProductRepository.findById(productSellerId)
                .orElse(null);

        if (sp == null) return null;
        Product p = productRepository.findById(sp.getProductId()).orElse(null);
        return ProductResponseDTO.builder()
                .sellerProductId(sp.getId())
                .productName(sp.getName() != null ? sp.getName() : (p != null ? p.getProductName() : "Sản phẩm"))
                .price(sp.getPrice())
                .stock(sp.getStock())
                .avatar(sp.getImageUrl() != null ? sp.getImageUrl() : (p != null ? p.getAvatar() : null))
                .isFavorite(false)
                .build();
    }


}