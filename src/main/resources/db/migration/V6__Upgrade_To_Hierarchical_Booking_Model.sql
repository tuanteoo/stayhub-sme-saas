-- ==========================================================================
-- FLYWAY MIGRATION: V6__Upgrade_To_Hierarchical_Booking_Model.sql
-- DESCRIPTION: Đại phẫu thuật Database sang mô hình Phân cấp (Property -> Rooms)
-- ==========================================================================

-- ==========================================================================
-- 1. TẠO BẢNG ROOMS VÀ CÁC BẢNG VỆ TINH
-- ==========================================================================
-- Thêm cột host_code vào bảng host_details
ALTER TABLE host_details ADD COLUMN host_code VARCHAR(50) UNIQUE;

-- Đánh index để tăng tốc độ truy vấn kiểm tra trùng lặp
CREATE INDEX idx_host_code ON host_details(host_code);

-- 1.1. Bảng Rooms (Chịu trách nhiệm về Giá và Sức chứa)
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL,
    
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    price_per_night DECIMAL(15,2) NOT NULL,
    max_guests INT NOT NULL DEFAULT 1,
    num_beds INT DEFAULT 1,
    num_bathrooms INT DEFAULT 1,
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_room_prop FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- 1.2. Bảng Room Images (Ảnh riêng cho từng phòng)
CREATE TABLE room_images (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    url TEXT NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    CONSTRAINT fk_rimg_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- 1.3. Bảng Room Amenities (Tiện ích riêng của từng phòng)
CREATE TABLE room_amenities (
    room_id BIGINT NOT NULL,
    amenity_id INT NOT NULL,
    PRIMARY KEY (room_id, amenity_id),
    CONSTRAINT fk_ra_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_ra_am FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
);

-- ==========================================================================
-- 2. TÁI CẤU TRÚC LỊCH TRỐNG (AVAILABILITY)
-- ==========================================================================

-- Xóa bảng Lịch trống cũ (Khóa theo nhà)
DROP TABLE IF EXISTS property_availability;

-- Khai sinh bảng Lịch trống mới (Khóa theo phòng)
CREATE TABLE room_availability (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    date DATE NOT NULL,
    
    is_available BOOLEAN DEFAULT TRUE,
    price_modifier DECIMAL(15,2) DEFAULT 0, -- Tùy biến giá cho riêng phòng này vào ngày Lễ
    
    CONSTRAINT fk_avail_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE (room_id, date) -- Khóa chống Double Booking ngay từ Database
);

-- ==========================================================================
-- 3. TÁI CẤU TRÚC ĐƠN HÀNG (BOOKINGS)
-- ==========================================================================

-- 3.1. Gọt bỏ các cột không còn phù hợp ở bảng bookings
-- Giữ lại property_id để dễ truy vấn thống kê cấp độ Căn nhà, nhưng bỏ các thông số phòng
ALTER TABLE bookings 
    DROP COLUMN IF EXISTS price_per_night,
    DROP COLUMN IF EXISTS num_adults,
    DROP COLUMN IF EXISTS num_children,
    DROP COLUMN IF EXISTS num_infants;

-- 3.2. Sinh ra bảng Booking Rooms (Giỏ hàng chứa nhiều phòng)
CREATE TABLE booking_rooms (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    
    -- Số lượng khách thực tế ở trong phòng này
    num_guests INT DEFAULT 1, 
    
    -- Lưu lại cấu hình giá tại đúng thời điểm khách bấm Đặt phòng (Snapshot)
    price_at_booking DECIMAL(15,2) NOT NULL, 
    
    CONSTRAINT fk_br_bkg FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_br_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- ==========================================================================
-- 4. GỌT BỎ CÁC CỘT DƯ THỪA Ở BẢNG PROPERTIES
-- ==========================================================================

-- Vì Giá, Giường, Sức chứa đã chuyển xuống Rooms, ta xóa chúng khỏi Properties để tránh rác dữ liệu
ALTER TABLE properties 
    DROP COLUMN IF EXISTS max_guests,
    DROP COLUMN IF EXISTS num_bedrooms,
    DROP COLUMN IF EXISTS num_beds,
    DROP COLUMN IF EXISTS num_bathrooms,
    DROP COLUMN IF EXISTS price_per_night;

-- ==========================================================================
-- 5. ĐÁNH INDEX TỐI ƯU HÓA TÌM KIẾM (TRÁI TIM CỦA SEARCH ENGINE)
-- ==========================================================================

-- Index 1: Tìm phòng theo sức chứa và giá tiền (Dùng nhiều nhất)
CREATE INDEX idx_rooms_search ON rooms(property_id, max_guests, price_per_night);

-- Index 2: Tìm phòng trống theo khoảng thời gian (Cực kỳ quan trọng)
CREATE INDEX idx_room_avail_search ON room_availability(room_id, date, is_available);

-- Index 3: Phục vụ load trang chi tiết đơn hàng
CREATE INDEX idx_booking_rooms ON booking_rooms(booking_id);