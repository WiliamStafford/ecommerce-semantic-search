-- =====================================================================
-- V1__Init_Database_Schema.sql - Cấu trúc hoàn chỉnh cho hệ thống Ecommerce
-- Tích hợp Snapshot dữ liệu tại bảng order_items
-- =====================================================================

-- 1. BẢNG HỆ THỐNG CƠ BẢN
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255),
                       full_name VARCHAR(255),
                       enabled BOOLEAN DEFAULT TRUE,
                       avatar VARCHAR(512),
                       phone VARCHAR(20) UNIQUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       role_name VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB;

-- 2. DANH MỤC & SẢN PHẨM
CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE products (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          product_name VARCHAR(255) NOT NULL,
                          category_id BIGINT,
                          origin VARCHAR(255),
                          description TEXT,
                          embedding JSON,
                          CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

CREATE TABLE seller_products (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 seller_id BIGINT NOT NULL,
                                 product_id BIGINT NOT NULL,
                                 product_name VARCHAR(255),
                                 image_url VARCHAR(512),
                                 price DOUBLE NOT NULL,
                                 stock INT DEFAULT 0,
                                 sku VARCHAR(50) DEFAULT 'N/A',
                                 status ENUM('ACTIVE', 'HIDDEN', 'BANNED') DEFAULT 'ACTIVE',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_sp_user FOREIGN KEY (seller_id) REFERENCES users(id),
                                 CONSTRAINT fk_sp_item FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

-- 3. GIỎ HÀNG & ĐƠN HÀNG (Với logic Snapshot)
CREATE TABLE carts (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE cart_items (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            cart_id BIGINT NOT NULL,
                            seller_product_id BIGINT NOT NULL,
                            quantity INT NOT NULL DEFAULT 1,
                            CONSTRAINT fk_ci_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
                            CONSTRAINT fk_ci_sp FOREIGN KEY (seller_product_id) REFERENCES seller_products(id)
) ENGINE=InnoDB;

CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        seller_id BIGINT NOT NULL,
                        order_status ENUM('PENDING', 'PAID', 'DELIVERED', 'CANCELLED', 'RETURNED') DEFAULT 'PENDING',
                        shipping_address VARCHAR(255),
                        total_price DOUBLE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE order_items (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             seller_product_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             price DOUBLE NOT NULL,
    -- SNAPSHOT: Lưu lại tên và ảnh tại thời điểm mua
                             product_name VARCHAR(255) NOT NULL,
                             image_url VARCHAR(512) NOT NULL,
                             CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
                             CONSTRAINT fk_item_sp FOREIGN KEY (seller_product_id) REFERENCES seller_products(id)
) ENGINE=InnoDB;

-- 4. THANH TOÁN & ĐỔI TRẢ
CREATE TABLE payment_sessions (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  order_id BIGINT NOT NULL UNIQUE,
                                  amount DOUBLE,
                                  status VARCHAR(20),
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE return_requests (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 order_item_id BIGINT NOT NULL UNIQUE,
                                 status VARCHAR(50) DEFAULT 'PENDING',
                                 reason TEXT,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_ret_item FOREIGN KEY (order_item_id) REFERENCES order_items(id)
) ENGINE=InnoDB;

-- 5. CÁC BẢNG HỖ TRỢ (Wishlist, Review, Chat)
CREATE TABLE wishlist (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          seller_product_id BIGINT NOT NULL,
                          UNIQUE KEY (user_id, seller_product_id),
                          CONSTRAINT fk_wl_sp FOREIGN KEY (seller_product_id) REFERENCES seller_products(id)
) ENGINE=InnoDB;

CREATE TABLE reviews (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         order_item_id BIGINT NOT NULL,
                         rating INT,
                         comment TEXT,
                         CONSTRAINT fk_rev_item FOREIGN KEY (order_item_id) REFERENCES order_items(id)
) ENGINE=InnoDB;

CREATE TABLE conversation (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              customer_id BIGINT NOT NULL,
                              seller_id BIGINT NOT NULL
) ENGINE=InnoDB;