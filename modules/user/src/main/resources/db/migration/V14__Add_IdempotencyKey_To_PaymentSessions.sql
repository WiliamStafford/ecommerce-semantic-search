
ALTER TABLE payment_sessions ADD COLUMN idempotency_key VARCHAR(255) NULL;