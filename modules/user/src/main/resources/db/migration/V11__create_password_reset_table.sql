CREATE TABLE password_reset_codes (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      email VARCHAR(255) NOT NULL,
                                      code VARCHAR(6) NOT NULL,
                                      expiration_time DATETIME NOT NULL,
                                      used BOOLEAN DEFAULT FALSE,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);