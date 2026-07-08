-- 1. Xóa khóa ngoại và cột ở bảng properties
ALTER TABLE properties DROP CONSTRAINT IF EXISTS fk_prop_cancel;
ALTER TABLE properties DROP COLUMN IF EXISTS cancellation_policy_id;

-- 2. Thêm cột và khóa ngoại vào bảng rooms
ALTER TABLE rooms ADD COLUMN cancellation_policy_id INT;
ALTER TABLE rooms ADD CONSTRAINT fk_room_cancel
    FOREIGN KEY (cancellation_policy_id) REFERENCES cancellation_policies(id);