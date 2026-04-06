CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL
);

CREATE TABLE products (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          product_name VARCHAR(255) NOT NULL,
                          category_id BIGINT,
                          origin VARCHAR(255),
                          stock INT DEFAULT 0,
                          size VARCHAR(50),
                          avatar VARCHAR(512),
                          note VARCHAR(255),
                          description TEXT,
                          embedding JSON,
                          CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE seller_products (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 seller_id BIGINT NOT NULL,
                                 product_id BIGINT NOT NULL,
                                 price DOUBLE NOT NULL,
                                 stock INT DEFAULT 0,
                                 sku VARCHAR(255),
                                 status ENUM('ACTIVE', 'HIDDEN_BY_SELLER', 'BANNED_BY_ADMIN', 'DELETED') DEFAULT 'ACTIVE',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_seller_product_user FOREIGN KEY (seller_id) REFERENCES users(id),
                                 CONSTRAINT fk_seller_product_item FOREIGN KEY (product_id) REFERENCES products(id)
);