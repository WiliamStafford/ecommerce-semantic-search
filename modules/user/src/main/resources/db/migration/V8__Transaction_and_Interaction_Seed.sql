-- =====================================================================
-- 0. BƯỚC VÁ DỮ LIỆU: ĐẢM BẢO NHÓM CUSTOMER (6-10) TỒN TẠI
-- =====================================================================
INSERT IGNORE INTO users (id, email, full_name, enabled) VALUES
                                                             (6, 'hung_buyer@gmail.com', 'Nguyễn Hùng', TRUE),
                                                             (7, 'lan_customer@gmail.com', 'Trần Thị Lan', TRUE),
                                                             (8, 'tuan_customer@gmail.com', 'Lê Anh Tuấn', TRUE),
                                                             (9, 'hoa_customer@gmail.com', 'Phạm Quỳnh Hoa', TRUE),
                                                             (10, 'dung_customer@gmail.com', 'Hoàng Trung Dũng', TRUE);

-- =====================================================================
-- 1. KHỞI TẠO DỮ LIỆU GỐC (PRODUCT & WALLET)
-- =====================================================================
INSERT IGNORE INTO categories (id, name) VALUES (4, 'Gia dụng'), (5, 'Làm đẹp'), (6, 'Đồ chơi');
INSERT IGNORE INTO products (id, product_name, category_id, origin, stock) VALUES
                                                                               (4, 'Đèn ngủ Mặt Trăng', 4, 'China', 100), (5, 'Serum Vitamin C', 5, 'Korea', 50), (6, 'Bộ Lego Phi Thuyền', 6, 'Denmark', 20);

INSERT IGNORE INTO seller_wallet (user_id, balance) VALUES (31, 2000000), (32, 15000000), (33, 5000000);
INSERT IGNORE INTO seller_products (id, seller_id, product_id, price, stock) VALUES (4, 31, 4, 350000, 50), (5, 32, 5, 450000, 30), (6, 33, 6, 1200000, 15);

-- =====================================================================
-- 2. TẠO CHAT (DÙNG BIẾN TẠM ĐỂ TRÁNH LỖI ID TỰ TĂNG)
-- =====================================================================
-- Chat 1: Hùng (ID 6) hỏi mua Đèn ngủ từ Shop Decor (ID 31)
INSERT INTO conversation (customer_id, seller_id) VALUES (6, 31);
SET @conv_id_1 = LAST_INSERT_ID();
INSERT INTO message (conversation_id, sender_id, content) VALUES (@conv_id_1, 6, 'Shop ơi đèn có remote không?'), (@conv_id_1, 31, 'Dạ có kèm remote ạ!');

-- Chat 2: Lan (ID 7) hỏi mua Serum từ Shop Mỹ phẩm (ID 32)
INSERT INTO conversation (customer_id, seller_id) VALUES (7, 32);
SET @conv_id_2 = LAST_INSERT_ID();
INSERT INTO message (conversation_id, sender_id, content) VALUES (@conv_id_2, 7, 'Serum này da nhạy cảm dùng được không shop?');

-- =====================================================================
-- 3. BƠM ĐƠN HÀNG (ÉP ID CỐ ĐỊNH ĐỂ V9 KHÔNG BỊ NULL)
-- =====================================================================
INSERT INTO orders (id, user_id, seller_id, order_status, total_price) VALUES
                                                                           (100, 7, 32, 'PAID', 450000), (101, 8, 33, 'DELIVERED', 1200000);

INSERT INTO order_items (id, order_id, seller_product_id, quantity, price) VALUES
                                                                               (100, 100, 5, 1, 450000), (101, 101, 6, 1, 1200000);