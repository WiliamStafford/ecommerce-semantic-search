package com.ecommerce.product.listener;

import com.ecommerce.common.util.BeanUtil;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.service.migration.DataMigrationService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PostRemove;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductEntityListener {

    @PostPersist
    @PostUpdate
    public void onPostPersistOrUpdate(Product product) {
        log.info("EntityListener: Phát hiện thay đổi trên sản phẩm ID: {}", product.getId());
        DataMigrationService service = BeanUtil.getBean(DataMigrationService.class);
        service.syncSingleProduct(product);
    }

    @PostRemove
    public void onPostRemove(Product product) {
        log.info("EntityListener: Sản phẩm ID: {} đã bị xóa", product.getId());
        // service.deleteProductFromEs(product.getId());
    }
}