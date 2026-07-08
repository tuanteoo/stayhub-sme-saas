-- rename column icon_url to icon_name in table amenities
ALTER TABLE amenities RENAME COLUMN icon_url TO icon_name;
ALTER TABLE categories RENAME COLUMN image_url TO icon_name;

CREATE TABLE rental_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

INSERT INTO rental_types (name, slug, description, icon_name) VALUES
('Toàn bộ nhà', 'toan-bo-nha', 'Khách được sử dụng riêng toàn bộ chỗ ở này.', 'Home'),
('Một căn phòng', 'mot-can-phong', 'Khách sẽ có phòng riêng trong một ngôi nhà và được sử dụng những khu vực chung.', 'DoorClosed'),
('Phòng chung', 'phong-chung', 'Khách ngủ trong phòng chung tại một chỗ ở được quản lý chuyên nghiệp.', 'Users');

ALTER TABLE properties DROP COLUMN IF EXISTS rental_type;
DROP TYPE IF EXISTS rental_type_enum;

ALTER TABLE properties ADD COLUMN rental_type_id INTEGER;
ALTER TABLE properties ADD CONSTRAINT fk_properties_rental_type FOREIGN KEY (rental_type_id) REFERENCES rental_types(id);
