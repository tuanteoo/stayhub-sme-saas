CREATE TABLE rental_type_categories (
    rental_type_id INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (rental_type_id, category_id),
    CONSTRAINT fk_rtc_rental FOREIGN KEY (rental_type_id) REFERENCES rental_types(id) ON DELETE CASCADE,
    CONSTRAINT fk_rtc_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);