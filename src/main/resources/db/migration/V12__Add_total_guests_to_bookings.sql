-- =========================================================================
-- V14: Thêm cột total_guests vào bảng bookings
-- =========================================================================

ALTER TABLE bookings
ADD COLUMN total_guests INT NOT NULL DEFAULT 1;