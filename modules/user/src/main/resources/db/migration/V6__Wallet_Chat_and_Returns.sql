CREATE TABLE seller_wallet (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               balance DOUBLE DEFAULT 0.0,
                               CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE conversation (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              customer_id BIGINT NOT NULL,
                              seller_id BIGINT NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_conv_customer FOREIGN KEY (customer_id) REFERENCES users(id),
                              CONSTRAINT fk_conv_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE message (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         conversation_id BIGINT NOT NULL,
                         sender_id BIGINT NOT NULL,
                         content TEXT,
                         is_read BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id) REFERENCES conversation(id),
                         CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE TABLE reviews (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         order_item_id BIGINT NOT NULL,
                         rating INT CHECK (rating BETWEEN 1 AND 5),
                         comment TEXT,
                         request_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id),
                         CONSTRAINT fk_review_item FOREIGN KEY (order_item_id) REFERENCES order_items(id)
);

CREATE TABLE return_requests (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 order_item_id BIGINT NOT NULL,
                                 customer_id BIGINT NOT NULL,
                                 seller_id BIGINT NOT NULL,
                                 return_reason ENUM('DEFECTIVE', 'DAMAGED', 'MISSING_PART', 'NOT_AS_DESCRIBED', 'QUALITY_NOT_GOOD', 'EXPIRED', 'CHANGED_MIND', 'DUPLICATE_ORDER', 'LATE_DELIVERY', 'OTHER'),
                                 description TEXT,
                                 status ENUM('PENDING', 'APPROVED', 'REJECTED', 'WAITING_RETURN', 'RECEIVED', 'COMPLETED') DEFAULT 'PENDING',
                                 evidence_image_urls TEXT, -- Lưu danh sách URL ảnh (comma separated)
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_return_item FOREIGN KEY (order_item_id) REFERENCES order_items(id),
                                 CONSTRAINT fk_return_customer FOREIGN KEY (customer_id) REFERENCES users(id),
                                 CONSTRAINT fk_return_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE return_refunds (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                return_request_id BIGINT NOT NULL,
                                refund_amount DOUBLE NOT NULL,
                                refund_status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
                                request_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                CONSTRAINT fk_refund_request FOREIGN KEY (return_request_id) REFERENCES return_requests(id)
);

CREATE TABLE carts (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_items (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            cart_id BIGINT NOT NULL,
                            seller_product_id BIGINT NOT NULL,
                            quantity INT NOT NULL DEFAULT 1,
                            CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
                            CONSTRAINT fk_cart_item_product FOREIGN KEY (seller_product_id) REFERENCES seller_products(id)
);