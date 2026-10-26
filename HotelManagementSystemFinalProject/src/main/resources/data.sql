-- Rooms
INSERT INTO room (room_number, type, price) VALUES (101, 'Standard', 89.99);
INSERT INTO room (room_number, type, price) VALUES (102, 'Standard', 89.99);
INSERT INTO room (room_number, type, price) VALUES (201, 'Deluxe', 149.99);
INSERT INTO room (room_number, type, price) VALUES (202, 'Deluxe', 149.99);
INSERT INTO room (room_number, type, price) VALUES (301, 'Suite', 249.99);
INSERT INTO room (room_number, type, price) VALUES (302, 'Presidential Suite', 499.99);

-- Customers
INSERT INTO customer (name, email, phone_number) VALUES ('Alice Johnson', 'alice@email.com', '555-0101');
INSERT INTO customer (name, email, phone_number) VALUES ('Bob Smith', 'bob@email.com', '555-0102');
INSERT INTO customer (name, email, phone_number) VALUES ('Carol Williams', 'carol@email.com', '555-0103');
INSERT INTO customer (name, email, phone_number) VALUES ('David Brown', 'david@email.com', '555-0104');

-- Services catalog
INSERT INTO provided_service (name, description, price) VALUES ('Breakfast Buffet', 'Full breakfast buffet for one guest', 18.99);
INSERT INTO provided_service (name, description, price) VALUES ('Spa Package', '60-minute spa treatment', 79.99);
INSERT INTO provided_service (name, description, price) VALUES ('Airport Transfer', 'Private car to/from airport', 45.00);

-- Demo bookings relative to today
INSERT INTO bookings (customer_id, room_id, start_date, end_date, status, nightly_rate) VALUES (1, 3, DATEADD('DAY', -1, CURRENT_DATE), DATEADD('DAY', 2, CURRENT_DATE), 'CONFIRMED', 149.99);
INSERT INTO bookings (customer_id, room_id, start_date, end_date, status, nightly_rate) VALUES (2, 5, CURRENT_DATE, DATEADD('DAY', 4, CURRENT_DATE), 'CONFIRMED', 249.99);
INSERT INTO bookings (customer_id, room_id, start_date, end_date, status, nightly_rate) VALUES (3, 1, DATEADD('DAY', -3, CURRENT_DATE), CURRENT_DATE, 'CANCELLED', 89.99);

-- Booking services (many-to-many)
INSERT INTO booking_services (booking_id, service_id) VALUES (1, 1);
INSERT INTO booking_services (booking_id, service_id) VALUES (1, 2);
INSERT INTO booking_services (booking_id, service_id) VALUES (2, 3);

INSERT INTO booking_service_prices (booking_id, service_id, price) VALUES (1, 1, 18.99);
INSERT INTO booking_service_prices (booking_id, service_id, price) VALUES (1, 2, 79.99);
INSERT INTO booking_service_prices (booking_id, service_id, price) VALUES (2, 3, 45.00);
