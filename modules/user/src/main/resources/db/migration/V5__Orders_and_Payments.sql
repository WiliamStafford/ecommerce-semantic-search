CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        seller_id BIGINT NOT NULL,
                        order_status ENUM('PENDING', 'PAID', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'RETURNED'),
                        shipping_address VARCHAR(255),
                        total_price DOUBLE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id),
                        CONSTRAINT fk_order_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE order_items (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             seller_product_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             price DOUBLE NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
                             CONSTRAINT fk_item_seller_product FOREIGN KEY (seller_product_id) REFERENCES seller_products(id)
);

CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          order_id BIGINT NOT NULL,
                          payment_method ENUM('COD', 'VNPAY', 'MOMO', 'BANK_TRANSFER'),
                          payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED'),
                          transaction_id VARCHAR(255),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
);