ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_booking
FOREIGN KEY (booking_id)
REFERENCES bookings(id)
ON DELETE RESTRICT;