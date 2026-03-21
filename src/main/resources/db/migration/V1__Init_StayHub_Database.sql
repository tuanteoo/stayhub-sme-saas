-- ==========================================================================
-- FLYWAY MIGRATION: V1__Init_Identity_And_RBAC.sql
-- DESCRIPTION: Khởi tạo module Identity, Multi-tenant RBAC, Host Onboarding
-- ==========================================================================

-- ==========================================================================
-- 1. ENUMS (Định nghĩa kiểu dữ liệu chuẩn)
-- ==========================================================================
CREATE TYPE user_status_enum AS ENUM ('UNVERIFIED', 'ACTIVE', 'BANNED', 'LOCKED');
CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE', 'OTHER');
CREATE TYPE verification_type_enum AS ENUM ('REGISTER', 'FORGOT_PASSWORD', 'UPGRADE_HOST');
CREATE TYPE social_provider_enum AS ENUM ('GOOGLE', 'FACEBOOK');
CREATE TYPE subscription_tier_enum AS ENUM ('FREE', 'PREMIUM', 'BUSINESS');
CREATE TYPE host_onboarding_status_enum AS ENUM ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'REQUEST_CHANGES');

-- ==========================================================================
-- 2. CORE RBAC & SUBSCRIPTION PLANS (Các bảng không có Foreign Key)
-- ==========================================================================

-- 2.1. Danh sách Tài nguyên (Fine-grained Resources)
CREATE TABLE resources (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

-- 2.2. Danh sách Quyền hạn (Actions)
CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

-- 2.3. Các gói dịch vụ cho Host
CREATE TABLE subscription_plans (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    tier subscription_tier_enum NOT NULL UNIQUE,
    price DECIMAL(15,2) NOT NULL DEFAULT 0,
    duration_days INT NOT NULL DEFAULT 30,
    max_listings INT,
    commission_rate DOUBLE PRECISION,
    is_active BOOLEAN DEFAULT TRUE
);

-- ==========================================================================
-- 3. USERS & IDENTITY (Có tính chất Multi-tenant cho nhân viên)
-- ==========================================================================

-- 3.1. Bảng User Chính
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    status user_status_enum NOT NULL DEFAULT 'UNVERIFIED',

    -- Multi-tenant: ID của Host chủ quản (NULL nếu là tài khoản độc lập)
    employer_id BIGINT,

    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_employer FOREIGN KEY (employer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3.2. Bảng Roles (Hỗ trợ Host gói BUSINESS tự tạo Role)
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,

    -- Multi-tenant: Chủ sở hữu của Role (NULL nếu là Role mặc định của hệ thống)
    owner_id BIGINT,

    name VARCHAR(50) NOT NULL, -- KHÔNG UNIQUE GLOBAL NỮA
    description VARCHAR(255),
    is_system_role BOOLEAN DEFAULT FALSE,

    -- Đảm bảo 1 Host không tạo 2 Role trùng tên, nhưng các Host khác nhau thì được
    UNIQUE (owner_id, name),
    CONSTRAINT fk_role_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3.3. Hồ sơ cá nhân (Profile)
CREATE TABLE profiles (
    user_id BIGINT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15),
    avatar_url TEXT,
    dob DATE,
    gender gender_enum,
    bio TEXT,
    address_detail VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3.4. Hồ sơ kinh doanh Host (Progressive Onboarding)
CREATE TABLE host_details (
    user_id BIGINT PRIMARY KEY,
    brand_name VARCHAR(255),
    about_host TEXT,
    business_phone VARCHAR(15),
    support_email VARCHAR(100),
    identity_card_number VARCHAR(20),
    identity_card_front_url TEXT,
    identity_card_back_url TEXT,
    business_license_number VARCHAR(50),
    business_license_url TEXT,
    onboarding_status host_onboarding_status_enum NOT NULL DEFAULT 'DRAFT',
    review_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_hd_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================================================
-- 4. RELATIONSHIPS & SECURITY
-- ==========================================================================

-- 4.1. Liên kết User - Role
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4.2. Ma trận Phân quyền (Role - Resource - Permission)
CREATE TABLE role_resource_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id INT NOT NULL,
    resource_id INT NOT NULL,
    permission_id INT NOT NULL,

    UNIQUE (role_id, resource_id, permission_id),

    CONSTRAINT fk_rrp_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_rrp_res FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_rrp_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- 4.3. Quản lý Gói cước của User
CREATE TABLE user_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id INT NOT NULL,
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    auto_renew BOOLEAN DEFAULT TRUE,
    current_commission_rate DOUBLE PRECISION,

    CONSTRAINT fk_sub_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sub_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
);

-- Các bảng Verification & Logging (Giữ nguyên cấu trúc chuẩn)
CREATE TABLE social_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider social_provider_enum NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    name VARCHAR(100),
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, provider),
    UNIQUE (provider, provider_id),
    CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(100) NOT NULL,
    type verification_type_enum NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    device_info VARCHAR(255),
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE login_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    email_attempt VARCHAR(100),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    status VARCHAR(50),
    failure_reason TEXT
);

-- ==========================================================================
-- 5. INDEXING
-- ==========================================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_profiles_phone ON profiles(phone_number);
CREATE INDEX idx_rrp_role ON role_resource_permissions(role_id);
CREATE INDEX idx_ver_token ON verification_tokens(token);
CREATE INDEX idx_logs_user ON login_logs(user_id);

-- ==========================================================================
-- 6. DATA SEEDING (Gieo hạt dữ liệu gốc)
-- ==========================================================================

-- 6.1. Seed Resources (Fine-grained)
INSERT INTO resources (code, name) VALUES
('USER_ACCOUNT', 'Quản lý Tài khoản người dùng'),
('ROLE_PERMISSION', 'Quản lý Vai trò và Phân quyền'),
('HOST_ONBOARDING', 'Xét duyệt hồ sơ Host'),
('PROPERTY_LISTING', 'Quản lý Thông tin nhà/phòng'),
('PROPERTY_CATEGORY', 'Quản lý Loại hình nhà'),
('PROPERTY_AMENITY', 'Quản lý Tiện ích'),
('BOOKING_ORDER', 'Quản lý Đơn đặt phòng'),
('PROMOTION_CODE', 'Quản lý Mã giảm giá'),
('WALLET_BALANCE', 'Quản lý Số dư ví'),
('TRANSACTION_LOG', 'Quản lý Lịch sử giao dịch'),
('PAYOUT_REQUEST', 'Xét duyệt Yêu cầu rút tiền'),
('GUEST_REVIEW', 'Quản lý Đánh giá của khách'),
('SYSTEM_DASHBOARD', 'Xem biểu đồ toàn hệ thống'),
('HOST_REPORT', 'Xem báo cáo doanh thu Host');

-- 6.2. Seed Permissions
INSERT INTO permissions (code, name) VALUES
('FULL_ACCESS', 'Toàn quyền'),
('VIEW', 'Xem dữ liệu'),
('CREATE', 'Tạo mới'),
('UPDATE', 'Cập nhật'),
('DELETE', 'Xóa bỏ'),
('APPROVE', 'Phê duyệt');

-- 6.3. Seed System Roles (owner_id = NULL)
INSERT INTO roles (owner_id, name, description, is_system_role) VALUES
(NULL, 'ROLE_ADMIN', 'Quản trị viên hệ thống', TRUE),
(NULL, 'ROLE_HOST', 'Chủ nhà / Đối tác', TRUE),
(NULL, 'ROLE_USER', 'Khách thuê phòng', TRUE);

-- 6.4. Seed Subscription Plans
INSERT INTO subscription_plans (name, tier, price, max_listings, commission_rate) VALUES
('Gói Miễn Phí', 'FREE', 0, 1, 0.10),
('Gói Chuyên Nghiệp', 'PREMIUM', 500000, 5, 0.05),
('Gói Doanh Nghiệp', 'BUSINESS', 2000000, 100, 0.02);

-- ==========================================================================
-- FLYWAY MIGRATION: V2__Init_Property_Management.sql
-- DESCRIPTION: Khởi tạo module Property (Danh mục, Tiện ích, Nhà, Lịch trống)
-- ==========================================================================

-- ==========================================================================
-- 1. ENUMS (Trạng thái và Phân loại)
-- ==========================================================================
CREATE TYPE property_status_enum AS ENUM ('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'HIDDEN', 'BANNED');
CREATE TYPE rental_type_enum AS ENUM ('ENTIRE_PLACE', 'PRIVATE_ROOM', 'SHARED_ROOM');
CREATE TYPE cancellation_policy_enum AS ENUM ('FLEXIBLE', 'MODERATE', 'STRICT');

-- ==========================================================================
-- 2. BẢNG DANH MỤC & TIỆN ÍCH (Master Data)
-- ==========================================================================

-- 2.1. Bảng CATEGORIES (Loại hình nhà)
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE, -- Dùng cho SEO URL (vd: homestay, villa-bien)
    description TEXT,
    image_url TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

-- 2.2. Bảng AMENITIES (Tiện ích)
CREATE TABLE amenities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    icon_url VARCHAR(255),
    type VARCHAR(50) DEFAULT 'BASIC' -- Các nhóm: BASIC, OUTDOOR, SAFETY, FAMILY...
);

-- ==========================================================================
-- 3. BẢNG TRUNG TÂM & QUẢN LÝ TÀI SẢN (Core Tables)
-- ==========================================================================

-- 3.1. Bảng PROPERTIES (Thông tin chi tiết của Nhà/Phòng)
CREATE TABLE properties (
    id BIGSERIAL PRIMARY KEY,

    -- Định danh & Phân loại
    host_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    rental_type rental_type_enum NOT NULL DEFAULT 'ENTIRE_PLACE',

    -- Thông tin cơ bản
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,

    -- Địa chỉ & Tọa độ
    address_detail VARCHAR(255),
    ward VARCHAR(100),
    district VARCHAR(100),
    province VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,

    -- Cấu trúc nhà
    max_guests INT NOT NULL DEFAULT 1,
    num_bedrooms INT DEFAULT 1,
    num_beds INT DEFAULT 1,
    num_bathrooms INT DEFAULT 1,

    -- Chính sách giá mặc định
    price_per_night DECIMAL(15,2) NOT NULL,
    cleaning_fee DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'VND',

    -- Quy định & Chính sách
    checkin_after VARCHAR(10) DEFAULT '14:00',
    checkout_before VARCHAR(10) DEFAULT '12:00',
    cancellation_policy cancellation_policy_enum NOT NULL DEFAULT 'FLEXIBLE',
    is_instant_book BOOLEAN DEFAULT FALSE,

    -- Nội quy (House Rules)
    is_smoking_allowed BOOLEAN DEFAULT FALSE,
    is_pets_allowed BOOLEAN DEFAULT FALSE,
    is_party_allowed BOOLEAN DEFAULT FALSE,

    -- Trạng thái (Phục vụ luồng Onboarding) & Thống kê
    status property_status_enum NOT NULL DEFAULT 'DRAFT',
    rating_avg DOUBLE PRECISION DEFAULT 0.0,
    review_count INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prop_host FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_prop_cat FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 3.2. Bảng PROPERTY_IMAGES (Quản lý Ảnh)
CREATE TABLE property_images (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL,
    url TEXT NOT NULL,
    caption VARCHAR(255),
    is_thumbnail BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    CONSTRAINT fk_img_prop FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- 3.3. Bảng PROPERTY_AMENITIES (Bảng trung gian N-N)
CREATE TABLE property_amenities (
    property_id BIGINT NOT NULL,
    amenity_id INT NOT NULL,
    PRIMARY KEY (property_id, amenity_id),
    CONSTRAINT fk_pa_prop FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_am FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
);

-- 3.4. Bảng PROPERTY_AVAILABILITY (Quản lý Lịch trống & Giá linh hoạt)
CREATE TABLE property_availability (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL,
    date DATE NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    price_modifier DECIMAL(15,2),
    CONSTRAINT fk_avail_prop FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    UNIQUE (property_id, date)
);

-- ==========================================================================
-- 4. INDEXING (Tối ưu hóa truy vấn tìm kiếm cho Khách)
-- ==========================================================================
-- Tối ưu cho bộ lọc tìm kiếm chính ngoài trang chủ
CREATE INDEX idx_prop_search ON properties(province, district, max_guests, price_per_night);

-- Tối ưu cho trang Dashboard của Host (Lấy danh sách nhà của tôi)
CREATE INDEX idx_prop_host ON properties(host_id);

-- Tối ưu truy vấn lịch trống theo khoảng thời gian
CREATE INDEX idx_avail_dates ON property_availability(property_id, date);

-- ==========================================================================
-- 5. DATA SEEDING (Dữ liệu mầm)
-- ==========================================================================

-- ==========================================================================
-- FLYWAY MIGRATION: V3__Init_Booking_And_Payment.sql
-- DESCRIPTION: Khởi tạo module Booking, Khuyến mãi & Thanh toán (Hỗ trợ Cọc)
-- ==========================================================================

-- ==========================================================================
-- 1. ENUMS
-- ==========================================================================
CREATE TYPE booking_status_enum AS ENUM (
    'PENDING',
    'AWAITING_PAYMENT',
    'PARTIALLY_PAID',       -- MỚI: Khách đã trả tiền cọc (Nếu chọn Pay at check-in)
    'CONFIRMED',            -- Đã thanh toán 100%
    'CHECKED_IN',           -- Host xác nhận khách đã đến
    'CHECKED_OUT',
    'DISPUTED',
    'COMPLETED',            -- Host xác nhận khách đã đi
    'CANCELLED',
    'REJECTED',
    'EXPIRED'
);

CREATE TYPE payment_method_enum AS ENUM ('VNPAY', 'MOMO', 'PAYPAL', 'STRIPE', 'CASH');
CREATE TYPE payment_status_enum AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');
CREATE TYPE payment_purpose_enum AS ENUM ('DEPOSIT', 'REMAINING_BALANCE', 'FULL_PAYMENT');

-- MỚI: Tùy chọn thanh toán mà Khách hàng chọn lúc đặt phòng
CREATE TYPE booking_payment_option_enum AS ENUM ('PAY_IN_FULL', 'PAY_AT_CHECKIN');

-- ==========================================================================
-- 2. BẢNG PROMOTIONS (Mã giảm giá / Khuyến mãi)
-- ==========================================================================
CREATE TABLE promotions (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    discount_type VARCHAR(20) DEFAULT 'PERCENT',
    discount_value DECIMAL(15,2) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    max_usage INT DEFAULT 100,
    usage_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE
);

-- ==========================================================================
-- 3. BẢNG BOOKINGS (Bảng Lõi Nghiệp vụ)
-- ==========================================================================
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_code VARCHAR(20) NOT NULL UNIQUE,

    user_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,

    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_nights INT NOT NULL,

    num_adults INT DEFAULT 1,
    num_children INT DEFAULT 0,
    num_infants INT DEFAULT 0,

    -- THÔNG TIN TÀI CHÍNH CƠ BẢN
    price_per_night DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    cleaning_fee DECIMAL(15,2) DEFAULT 0,
    service_fee DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    promotion_id INT,

    -- ==========================================
    -- LOGIC ĐẶT CỌC & THANH TOÁN (MỚI)
    -- ==========================================
    payment_option booking_payment_option_enum NOT NULL DEFAULT 'PAY_IN_FULL',

    deposit_percentage INT NOT NULL DEFAULT 100, -- Lưu tỷ lệ cọc của nhà lúc đặt
    deposit_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    remaining_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    is_fully_paid BOOLEAN DEFAULT FALSE, -- Cờ check đã nhận đủ 100% tiền chưa

    -- TRẠNG THÁI & GHI CHÚ
    status booking_status_enum NOT NULL DEFAULT 'PENDING',
    note TEXT,

    cancellation_policy cancellation_policy_enum NOT NULL DEFAULT 'FLEXIBLE',
    cancellation_reason TEXT,
    cancelled_at TIMESTAMP,
    cancelled_by BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bkg_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bkg_prop FOREIGN KEY (property_id) REFERENCES properties(id),
    CONSTRAINT fk_bkg_promo FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

-- ==========================================================================
-- 4. BẢNG PAYMENTS (Lịch sử giao dịch thanh toán của khách)
-- ==========================================================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    amount DECIMAL(15,2) NOT NULL,

    payment_method payment_method_enum NOT NULL,
    payment_status payment_status_enum NOT NULL DEFAULT 'PENDING',
    purpose payment_purpose_enum NOT NULL DEFAULT 'FULL_PAYMENT', -- Phân biệt tiền cọc hay tiền trả full

    transaction_id VARCHAR(255),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pay_bkg FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_pay_user FOREIGN KEY (user_id) REFERENCES users(id)
);
-- ==========================================================================
-- 5. INDEXING (Tối ưu hóa truy vấn)
-- ==========================================================================
CREATE INDEX idx_bkg_user ON bookings(user_id);
CREATE INDEX idx_bkg_prop ON bookings(property_id);
CREATE INDEX idx_bkg_dates ON bookings(property_id, check_in_date, check_out_date);
CREATE INDEX idx_bkg_code ON bookings(booking_code);

-- ==========================================================================
-- 6. DATA SEEDING (Tùy chọn: Mã giảm giá chào mừng)
-- ==========================================================================
INSERT INTO promotions (code, description, discount_type, discount_value, max_usage) VALUES
('WELCOME2026', 'Giảm 10% cho khách hàng mới', 'PERCENT', 10.00, 1000),
('GIAM50K', 'Giảm trực tiếp 50.000 VND', 'FIXED_AMOUNT', 50000.00, 500);

-- ==========================================================================
-- FLYWAY MIGRATION: V4__Init_Finance_And_Wallet.sql
-- DESCRIPTION: Khởi tạo module Tài chính, Ví điện tử, Giao dịch & Rút tiền
-- ==========================================================================

-- ==========================================================================
-- 1. ENUMS
-- ==========================================================================
CREATE TYPE wallet_status_enum AS ENUM ('ACTIVE', 'LOCKED', 'FROZEN', 'DEBT_OVERDUE');

-- Các loại giao dịch làm thay đổi số dư ví
CREATE TYPE transaction_type_enum AS ENUM (
    'BOOKING_PAYMENT',  -- Khách trả tiền (Tiền vào Pending)
    'BOOKING_INCOME',   -- Tiền từ Pending chuyển sang Available (Sau check-out)
    'BOOKING_REFUND',   -- Hoàn tiền cho khách
    'WITHDRAWAL',       -- Host rút tiền về ngân hàng
    'SYSTEM_FEE',       -- Phí sàn (Trừ tiền Host)
    'DEBT_PAYMENT'      -- Host nạp tiền qua VNPAY để trả nợ cho sàn
);

CREATE TYPE transaction_status_enum AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED');
CREATE TYPE payout_status_enum AS ENUM ('REQUESTED', 'PROCESSING', 'COMPLETED', 'REJECTED');

-- ==========================================================================
-- 2. BẢNG WALLETS (Ví điện tử của Host)
-- ==========================================================================
-- Trong mô hình này, chỉ Host mới thực sự cần dùng Ví để nhận tiền và rút tiền.
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    -- 3 CỘT SỐ DƯ QUAN TRỌNG
    available_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00, -- Số dư có thể rút
    pending_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,   -- Số dư đang chờ (Khách chưa check-out)
    debt_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,      -- Dư nợ (Host nợ sàn)

    currency VARCHAR(3) DEFAULT 'VND',
    status wallet_status_enum DEFAULT 'ACTIVE',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================================================
-- 3. BẢNG BANK_ACCOUNTS (Tài khoản ngân hàng của Host)
-- ==========================================================================
CREATE TABLE bank_accounts (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,

    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    branch VARCHAR(100),

    is_default BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bank_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================================================
-- 4. BẢNG TRANSACTIONS (Sổ cái - Ledger)
-- ==========================================================================
-- Nguyên tắc: KHÔNG BAO GIỜ UPDATE/DELETE dòng có status SUCCESS ở bảng này.
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,

    amount DECIMAL(15,2) NOT NULL,

    -- Đánh dấu giao dịch này tác động vào cột số dư nào (AVAILABLE, PENDING hay DEBT)
    balance_affected VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

    type transaction_type_enum NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    description VARCHAR(255),

    booking_id BIGINT,
    payout_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trans_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id)
    -- LƯU Ý: Khóa ngoại payout_id/booking_id add bằng lệnh ALTER để tránh lỗi vòng lặp
);

-- ==========================================================================
-- 5. BẢNG PAYOUTS (Yêu cầu Rút tiền)
-- ==========================================================================
CREATE TABLE payouts (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    amount DECIMAL(15,2) NOT NULL,

    -- Snapshot Bank Info: Lưu cứng thông tin ngân hàng lúc tạo lệnh rút
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    account_holder_name VARCHAR(100),

    status payout_status_enum DEFAULT 'REQUESTED',

    proof_image_url TEXT, -- Ảnh UNC (Ủy nhiệm chi) Admin tải lên làm bằng chứng
    admin_note TEXT,

    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payout_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    CONSTRAINT fk_payout_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add khóa ngoại cho transactions trỏ về payouts
ALTER TABLE transactions
ADD CONSTRAINT fk_trans_payout FOREIGN KEY (payout_id) REFERENCES payouts(id);

-- ==========================================================================
-- 6. INDEXING
-- ==========================================================================
CREATE INDEX idx_trans_wallet ON transactions(wallet_id, created_at DESC);
CREATE INDEX idx_payout_status ON payouts(status);

-- ==========================================================================
-- FLYWAY MIGRATION: V5__Init_Reviews_And_Disputes.sql
-- DESCRIPTION: Khởi tạo module Đánh giá & Tranh chấp/Khiếu nại
-- ==========================================================================

-- ==========================================================================
-- 1. BẢNG DISPUTES (Xử lý khiếu nại trong 24h vàng)
-- ==========================================================================
CREATE TYPE dispute_status_enum AS ENUM ('OPEN', 'UNDER_REVIEW', 'RESOLVED_REFUND_GUEST', 'RESOLVED_PAY_HOST', 'REJECTED');

CREATE TABLE disputes (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE, -- 1 Booking có 1 luồng tranh chấp

    creator_id BIGINT NOT NULL, -- Ai là người tạo khiếu nại (Host hoặc Guest)

    reason VARCHAR(255) NOT NULL, -- Phân loại (VD: "Phòng bẩn", "Khách làm hỏng TV")
    description TEXT,
    evidence_image_urls TEXT, -- Lưu mảng ảnh bằng chứng (JSON hoặc chuỗi cách nhau dấu phẩy)

    status dispute_status_enum NOT NULL DEFAULT 'OPEN',

    admin_note TEXT, -- Quyết định của Ban quản trị StayHub
    resolved_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dispute_bkg FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_dispute_creator FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- ==========================================================================
-- 2. BẢNG REVIEWS (Đánh giá của Khách hàng)
-- ==========================================================================
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,

    booking_id BIGINT NOT NULL UNIQUE, -- Đảm bảo 1 Booking chỉ review 1 lần
    user_id BIGINT NOT NULL,           -- Khách hàng (Guest)
    property_id BIGINT NOT NULL,       -- Nhà được đánh giá

    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,

    -- Host có thể phản hồi lại đánh giá của khách
    host_reply TEXT,
    reply_time TIMESTAMP,

    -- Admin có thể ẩn những review chửi bậy / vi phạm tiêu chuẩn
    is_visible BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rev_bkg FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_rev_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_rev_prop FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- ==========================================================================
-- 3. BẢNG REVIEW_IMAGES (Ảnh đính kèm Đánh giá)
-- ==========================================================================
CREATE TABLE review_images (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,

    image_url TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rimg_rev FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- ==========================================================================
-- 4. INDEXING
-- ==========================================================================
CREATE INDEX idx_rev_prop ON reviews(property_id, created_at DESC);
CREATE INDEX idx_rev_rating ON reviews(property_id, rating);
CREATE INDEX idx_dispute_status ON disputes(status);

-- ==========================================================================
-- FLYWAY MIGRATION: V6__Init_Analytics_And_Reporting.sql
-- DESCRIPTION: Khởi tạo module Analytics, Thống kê hệ thống và báo cáo Doanh thu
-- ==========================================================================

-- ==========================================================================
-- 1. BẢNG DAILY_SYSTEM_STATS (Thống kê toàn sàn - Dành cho Super Admin)
-- ==========================================================================
CREATE TABLE analytics_daily_system (
    id BIGSERIAL PRIMARY KEY,

    date DATE NOT NULL UNIQUE, -- Ngày chốt sổ (VD: 2026-02-23)

    -- Chỉ số Tài chính (Dòng tiền)
    total_gmv DECIMAL(15,2) DEFAULT 0,       -- Gross Merchandise Value: Tổng tiền khách đã thanh toán
    total_system_fee DECIMAL(15,2) DEFAULT 0,-- Doanh thu của sàn StayHub (Từ Service Fee)

    -- Chỉ số Tăng trưởng (Growth)
    new_users INT DEFAULT 0,    -- Số người đăng ký mới trong ngày
    new_hosts INT DEFAULT 0,    -- Số Host được duyệt mới
    new_bookings INT DEFAULT 0, -- Số đơn đặt phòng mới tạo

    -- Chỉ số Hoạt động (Operation)
    active_listings INT DEFAULT 0, -- Tổng số nhà đang ở trạng thái PUBLISHED

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================================
-- 2. BẢNG DAILY_HOST_STATS (Thống kê cho từng Host - Hiển thị trên Host Dashboard)
-- ==========================================================================
CREATE TABLE analytics_daily_host (
    id BIGSERIAL PRIMARY KEY,

    host_id BIGINT NOT NULL,
    date DATE NOT NULL,

    -- Tiền nong của Host
    total_earnings DECIMAL(15,2) DEFAULT 0, -- Tiền Host thực nhận (Sau khi trừ phí sàn, từ các đơn COMPLETED)

    -- Hiệu suất kinh doanh
    total_bookings INT DEFAULT 0,   -- Số đơn nhận được trong ngày
    cancelled_bookings INT DEFAULT 0, -- Số đơn bị hủy (Khách hủy + Host hủy)

    -- Chỉ số phụ
    average_rating_daily DOUBLE PRECISION DEFAULT 0.0, -- Điểm đánh giá trung bình nhận được trong ngày

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (host_id, date), -- Đảm bảo 1 Host chỉ có 1 dòng thống kê cho 1 ngày
    CONSTRAINT fk_stat_host FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================================================
-- 3. BẢNG PROPERTY_STATS (Thống kê chi tiết từng căn nhà)
-- ==========================================================================
CREATE TABLE analytics_property (
    id BIGSERIAL PRIMARY KEY,

    property_id BIGINT NOT NULL,
    date DATE NOT NULL,

    -- Hiệu suất thu hút
    page_views INT DEFAULT 0, -- Số lượt khách click vào xem chi tiết nhà

    -- Hiệu suất lấp đầy (Occupancy)
    is_booked BOOLEAN DEFAULT FALSE, -- Ngày hôm đó nhà có khách lưu trú không?
    revenue_daily DECIMAL(15,2) DEFAULT 0, -- Doanh thu mang lại từ căn này

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (property_id, date),
    CONSTRAINT fk_stat_prop FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- ==========================================================================
-- 4. INDEXING (Cực kỳ quan trọng để vẽ Biểu đồ - Charts nhanh)
-- ==========================================================================
-- Tối ưu cho query lấy dữ liệu 7 ngày, 30 ngày, 1 năm gần nhất
CREATE INDEX idx_sys_date ON analytics_daily_system(date DESC);
CREATE INDEX idx_host_chart ON analytics_daily_host(host_id, date DESC);
CREATE INDEX idx_prop_chart ON analytics_property(property_id, date DESC);