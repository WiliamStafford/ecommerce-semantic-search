-- V15__Convert_PaymentStatus_To_Enum.sql
ALTER TABLE payment_sessions
    MODIFY COLUMN status ENUM('pending', 'processing', 'completed', 'failed', 'expired', 'cancelled');