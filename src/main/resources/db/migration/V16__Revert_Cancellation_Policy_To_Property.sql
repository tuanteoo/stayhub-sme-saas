-- ==============================================================================
-- 1. CHUYỂN CHÍNH SÁCH HỦY TỪ PHÒNG (ROOMS) VỀ LẠI BÀI ĐĂNG (PROPERTIES)
-- ==============================================================================
ALTER TABLE rooms
DROP CONSTRAINT IF EXISTS fk_room_cancel;

ALTER TABLE rooms
DROP COLUMN IF EXISTS cancellation_policy_id;

ALTER TABLE properties
ADD COLUMN cancellation_policy_id INT;

ALTER TABLE properties
ADD CONSTRAINT fk_prop_cancel_policy
FOREIGN KEY (cancellation_policy_id) REFERENCES cancellation_policies(id);


-- ==============================================================================
-- 2. DỌN DẸP BẢNG BOOKING_ROOMS
-- ==============================================================================
ALTER TABLE booking_rooms
DROP COLUMN IF EXISTS num_guests;