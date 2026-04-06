ALTER TABLE users
    ADD COLUMN avatar VARCHAR(512),
    ADD COLUMN age INT,
    ADD COLUMN phone VARCHAR(20) UNIQUE,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       role_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id)
);