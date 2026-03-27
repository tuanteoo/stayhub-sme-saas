ALTER TABLE properties ADD COLUMN checkin_before VARCHAR(10) DEFAULT '23:30';
ALTER TABLE properties ADD COLUMN checkout_after VARCHAR(10) DEFAULT '01:00';

-- Đảm bảo các cột hiện tại đang giữ đúng giá trị mặc định theo yêu cầu
ALTER TABLE properties ALTER COLUMN checkin_after SET DEFAULT '14:00';
ALTER TABLE properties ALTER COLUMN checkout_before SET DEFAULT '12:00';