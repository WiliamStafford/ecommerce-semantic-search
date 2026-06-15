-- Tạo bảng addresses
CREATE TABLE IF NOT EXISTS addresses (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         user_id BIGINT,
                                         full_name VARCHAR(255),
                                         phone VARCHAR(20),
                                         province VARCHAR(100),
                                         district VARCHAR(100),
                                         ward VARCHAR(100),
                                         street VARCHAR(255),
                                         house_number VARCHAR(100),
                                         is_default BOOLEAN DEFAULT FALSE,
                                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Thêm cột address_id nếu chưa có, sau đó tạo khóa ngoại
SET @dbname = DATABASE();
SET @tablename = 'orders';
SET @columnname = 'address_id';
SET @preparedStatement = (SELECT IF(
                                         (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                                          WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
                                         'SELECT 1',
                                         'ALTER TABLE orders ADD COLUMN address_id BIGINT'
                                 ));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm ràng buộc khóa ngoại (kiểm tra nếu chưa tồn tại constraint để tránh lỗi)
SET @constraint_name = 'fk_orders_address_id_v2';
SET @sql = CONCAT('ALTER TABLE orders ADD CONSTRAINT ', @constraint_name, ' FOREIGN KEY (address_id) REFERENCES addresses(id)');

SET @check_constraint = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                         WHERE CONSTRAINT_NAME = @constraint_name AND TABLE_NAME = 'orders');

SET @sql_to_run = IF(@check_constraint = 0, @sql, 'SELECT 1');
PREPARE stmt_constraint FROM @sql_to_run;
EXECUTE stmt_constraint;
DEALLOCATE PREPARE stmt_constraint;