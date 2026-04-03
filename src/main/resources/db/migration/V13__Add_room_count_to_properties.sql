-- Thêm cột số lượng phòng vào bảng properties
ALTER TABLE properties ADD COLUMN room_count INT DEFAULT 1;