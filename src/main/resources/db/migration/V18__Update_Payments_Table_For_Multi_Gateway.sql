ALTER TABLE payments
DROP COLUMN IF EXISTS payment_date,
DROP COLUMN IF EXISTS transaction_id;

ALTER TABLE payments
    ALTER COLUMN payment_method TYPE VARCHAR(50) USING payment_method::VARCHAR,
    ALTER COLUMN payment_status TYPE VARCHAR(50) USING payment_status::VARCHAR,
    ALTER COLUMN purpose TYPE VARCHAR(50) USING purpose::VARCHAR;

ALTER TABLE payments
    ALTER COLUMN payment_status SET DEFAULT 'PENDING',
    ALTER COLUMN purpose SET DEFAULT 'FULL_PAYMENT';

DROP TYPE IF EXISTS payment_method_enum CASCADE;
DROP TYPE IF EXISTS payment_status_enum CASCADE;
DROP TYPE IF EXISTS payment_purpose_enum CASCADE;

ALTER TABLE payments
ADD COLUMN transaction_ref VARCHAR(100),
ADD COLUMN gateway_transaction_no VARCHAR(255),
ADD COLUMN bank_code VARCHAR(50),
ADD COLUMN pay_date VARCHAR(50),
ADD COLUMN gateway_response_code VARCHAR(50),
ADD COLUMN gateway_payload TEXT,
ADD COLUMN refund_amount DECIMAL(15,2) DEFAULT 0.00,
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_payments_transaction_ref ON payments(transaction_ref);