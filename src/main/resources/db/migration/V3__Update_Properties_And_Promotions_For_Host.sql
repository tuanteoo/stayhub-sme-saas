ALTER TABLE properties ADD COLUMN weekend_price DECIMAL(15,2);

ALTER TABLE promotions ADD COLUMN host_id BIGINT;
ALTER TABLE promotions ADD CONSTRAINT fk_promo_host FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE;