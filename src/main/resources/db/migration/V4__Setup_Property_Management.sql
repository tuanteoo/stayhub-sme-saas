-- =========================================================================
-- PHẦN 1: TẠO BẢNG CHÍNH SÁCH HỦY PHÒNG (CANCELLATION POLICIES)
-- =========================================================================
-- Đổi tên cột và ép kiểu thành Integer (Phần trăm)
ALTER TABLE properties RENAME COLUMN weekend_price TO weekend_surcharge_percentage;
ALTER TABLE properties ALTER COLUMN weekend_surcharge_percentage TYPE INT USING weekend_surcharge_percentage::integer;
ALTER TABLE properties ALTER COLUMN weekend_surcharge_percentage SET DEFAULT 0;

-- 1. Tạo bảng Master Data
CREATE TABLE cancellation_policies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    refund_percentage INT NOT NULL,
    days_before_checkin INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- 2. Đổ dữ liệu hạt giống (Seed Data)
INSERT INTO cancellation_policies (name, description, refund_percentage, days_before_checkin) VALUES
('Linh hoạt', 'Hoàn tiền 100% nếu hủy trước 1 ngày check-in. Sau đó không hoàn tiền.', 100, 1),
('Trung bình', 'Hoàn tiền 100% nếu hủy trước 5 ngày check-in. Sau đó không hoàn tiền.', 100, 5),
('Nghiêm ngặt', 'Hoàn tiền 50% nếu hủy trước 14 ngày check-in. Sau đó không hoàn tiền.', 50, 14);

-- =========================================================================
-- PHẦN 2: CẬP NHẬT BẢNG CĂN NHÀ (PROPERTIES)
-- =========================================================================

-- 3. Xóa cột Enum cũ (cancellation_policy) vì chúng ta không dùng chuỗi String nữa
ALTER TABLE properties DROP COLUMN IF EXISTS cancellation_policy;
ALTER TABLE bookings DROP COLUMN IF EXISTS cancellation_policy;

-- 2. Thêm cột mới nối với bảng Chính sách (Để biết lúc khách đặt phòng áp dụng chính sách nào)
ALTER TABLE bookings ADD COLUMN cancellation_policy_id INT;
ALTER TABLE bookings ADD CONSTRAINT fk_booking_cancel_policy FOREIGN KEY (cancellation_policy_id) REFERENCES cancellation_policies(id);

-- 4. Thêm Khóa ngoại nối với bảng Chính sách hủy phòng
ALTER TABLE properties ADD COLUMN cancellation_policy_id INT;
ALTER TABLE properties ADD CONSTRAINT fk_prop_cancel_policy FOREIGN KEY (cancellation_policy_id) REFERENCES cancellation_policies(id);

-- 5. Thêm cấu hình Thanh toán & Đặt cọc
ALTER TABLE properties ADD COLUMN is_pay_at_checkin_allowed BOOLEAN DEFAULT FALSE;
ALTER TABLE properties ADD COLUMN deposit_percentage INT DEFAULT 100 CHECK (deposit_percentage >= 0 AND deposit_percentage <= 100);

DROP TYPE IF EXISTS cancellation_policy_enum;