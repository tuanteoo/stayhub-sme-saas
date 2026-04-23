ALTER TABLE verification_tokens
ALTER COLUMN type TYPE VARCHAR(50) USING type::text;

ALTER TABLE verification_tokens
ADD COLUMN attempt_count INT DEFAULT 0 NOT NULL;

DROP TYPE IF EXISTS verification_type_enum;