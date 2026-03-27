-- Đổi giá trị mặc định cho các cột boolean thành true
ALTER TABLE properties ALTER COLUMN is_instant_book SET DEFAULT true;
ALTER TABLE properties ALTER COLUMN is_smoking_allowed SET DEFAULT true;
ALTER TABLE properties ALTER COLUMN is_pets_allowed SET DEFAULT true;
ALTER TABLE properties ALTER COLUMN is_party_allowed SET DEFAULT true;

-- Đổi giá trị mặc định của phần trăm đặt cọc thành 0
ALTER TABLE properties ALTER COLUMN is_pay_at_checkin_allowed SET DEFAULT true;
ALTER TABLE properties ALTER COLUMN deposit_percentage SET DEFAULT 0;

-- Đặt giá trị mặc định cho cột cancellation_policy_id trong bảng rooms là 1
ALTER TABLE rooms ALTER COLUMN cancellation_policy_id SET DEFAULT 1;

