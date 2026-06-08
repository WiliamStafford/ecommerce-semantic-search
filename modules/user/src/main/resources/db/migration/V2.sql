-- V2__Insert_Sample_Data.sql

-- 1. Khởi tạo quyền
INSERT INTO roles (role_name) VALUES ('ROLE_USER'), ('ROLE_SELLER'), ('ROLE_ADMIN');

-- 2. Khởi tạo người dùng
INSERT INTO users (email, password, full_name, enabled, phone) VALUES
                                                                   ('levuhung678@gmail.com', '123456', 'Lê Vũ Hùng', TRUE, '0901234567'),
                                                                   ('22130091@st.hcmuaf.edu.vn', '123456', 'Sinh Viên HCMAUF', TRUE, '0907654321');

-- 3. Gán quyền
INSERT INTO user_roles (user_id, role_id) VALUES
                                              (1, 1), (1, 2), (1, 3),
                                              (2, 1), (2, 2);

-- 4. Danh mục & Sản phẩm
INSERT INTO categories (name) VALUES ('Rau ăn lá'), ('Củ quả tươi'), ('Trái cây nhiệt đới');
INSERT INTO products (product_name, category_id, origin, description) VALUES
                                                                          ('Xoài Cát Hòa Lộc', 3, 'Tiền Giang', 'Xoài ngọt thơm'),
                                                                          ('Cải Xà Lách Thủy Canh', 1, 'Đà Lạt', 'Rau sạch'),
                                                                          ('Cà Chua Beef', 2, 'Lâm Đồng', 'Cà chua mọng nước');

-- 5. Insert vào seller_products (Đã bao gồm cột sku)
INSERT INTO seller_products (seller_id, product_id, product_name, image_url, price, stock, status, sku) VALUES
                                                                                                            (1, 1, 'Xoài Cát Hòa Lộc', '...', 65000, 50, 'ACTIVE', 'XCL-001'),
                                                                                                            (2, 2, 'Cải Xà Lách Thủy Canh', '...', 35000, 100, 'ACTIVE', 'CXLT-002'),
                                                                                                            (1, 3, 'Cà Chua Beef', '...', 45000, 30, 'ACTIVE', 'CTB-003');