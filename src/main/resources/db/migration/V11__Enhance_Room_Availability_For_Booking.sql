-- 1. Thêm cột booking_id để tracking ngày này bị khóa bởi đơn nào
ALTER TABLE room_availability ADD COLUMN booking_id BIGINT;
ALTER TABLE room_availability ADD CONSTRAINT fk_avail_booking
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL;

-- 2. Thêm cột version để hỗ trợ Optimistic Locking (Phòng chống đụng độ dữ liệu)
ALTER TABLE room_availability ADD COLUMN version BIGINT DEFAULT 0;

-- =========================================================================
-- V13: Cập nhật lại cơ chế thu phí và hoa hồng trên bảng Bookings
-- =========================================================================

-- 1. Xóa cột service_fee (Vì hệ thống quyết định KHÔNG thu phí dịch vụ của khách thuê nữa)
ALTER TABLE bookings
DROP COLUMN service_fee;

-- 2. Thêm cột platform_commission (Để lưu cứng số tiền hoa hồng nền tảng sẽ thu của Host)
-- Mặc định là 0.00 để không làm lỗi các record cũ (nếu có)
ALTER TABLE bookings
ADD COLUMN platform_commission DECIMAL(15, 2) DEFAULT 0.00;