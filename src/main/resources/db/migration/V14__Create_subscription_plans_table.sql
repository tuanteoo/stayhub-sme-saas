-- =====================================================================================
-- 1. CẬP NHẬT ENUM: Bổ sung các hạng gói cước mới (Giữ nguyên logic cũ)
-- =====================================================================================
ALTER TYPE subscription_tier_enum ADD VALUE IF NOT EXISTS 'BASIC';
ALTER TYPE subscription_tier_enum ADD VALUE IF NOT EXISTS 'PRO';
ALTER TYPE subscription_tier_enum ADD VALUE IF NOT EXISTS 'ENTERPRISE';

-- =====================================================================================
-- 2. CẬP NHẬT BẢNG SUBSCRIPTION_PLANS HIỆN TẠI
-- =====================================================================================

-- 2.1 Thêm 2 cột mới: Mô tả và Hạn mức dư nợ
ALTER TABLE subscription_plans
ADD COLUMN description TEXT,
ADD COLUMN credit_limit DECIMAL(15,2) NOT NULL DEFAULT 0,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 2.2 Đổi tên cột duration_days thành duration_months
ALTER TABLE subscription_plans
RENAME COLUMN duration_days TO duration_months;

-- 2.3 Chuyển đổi dữ liệu cũ (Data Migration):
-- Nếu trước đó em để 30 ngày -> sẽ tự chuyển thành 1 tháng, 90 ngày -> 3 tháng
UPDATE subscription_plans
SET duration_months = GREATEST(1, ROUND(duration_months / 30.0));

-- 2.4 Cập nhật lại giá trị mặc định cho cột duration_months (từ 30 xuống 1)
ALTER TABLE subscription_plans
ALTER COLUMN duration_months SET DEFAULT 1;