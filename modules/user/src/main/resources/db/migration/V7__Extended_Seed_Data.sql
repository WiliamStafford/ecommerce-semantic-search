-- Khởi tạo Roles cơ bản
INSERT IGNORE INTO roles (id, role_name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_SELLER'), (3, 'ROLE_CUSTOMER');

-- Tạo nhóm Seller (31-35) và Customer mồi (6-10)
INSERT IGNORE INTO users (id, email, full_name, enabled, phone) VALUES
                                                                    (31, 'home_decor@gmail.com', 'Tiệm Decor Nhà Xinh', TRUE, '0980000031'),
                                                                    (32, 'beauty_cosmetic@gmail.com', 'Mỹ Phẩm Auth 24h', TRUE, '0980000032'),
                                                                    (33, 'toy_world@gmail.com', 'Thế Giới Đồ Chơi', TRUE, '0980000033'),
                                                                    (6, 'hung_buyer@gmail.com', 'Nguyễn Hùng', TRUE, '0970000006'),
                                                                    (7, 'lan_customer@gmail.com', 'Trần Thị Lan', TRUE, '0970000007'),
                                                                    (8, 'tuan_customer@gmail.com', 'Lê Anh Tuấn', TRUE, '0970000008');

-- Gán quyền
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (31, 2), (32, 2), (33, 2), (6, 3), (7, 3), (8, 3);