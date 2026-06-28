CREATE TABLE IF NOT EXISTS messages (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        conversation_id BIGINT NOT NULL,
                                        sender_id BIGINT NOT NULL,
                                        content TEXT,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id) REFERENCES conversation(id)
) ENGINE=InnoDB;