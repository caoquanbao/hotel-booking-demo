-- seed-mysql.sql
-- Seed data for Booking_Service (MySQL)
--
-- Goals:
-- 1. Enough data to test search hotel flow
-- 2. Enough data to test create booking and overbooking
-- 3. Enough data to test mock payment callback flow
-- 4. Enough data to test cancel booking and inventory restore
-- 5. Enough data to test review and rating summary
--
-- Notes:
-- - Inventory values below are the CURRENT remaining rooms after seeded bookings are applied.
-- - Booking #1 is PENDING_PAYMENT and is still holding inventory.
-- - Booking #2 is CONFIRMED and is consuming inventory.
-- - Booking #3 is CANCELLED and is NOT consuming inventory anymore.
-- - Booking #4 is COMPLETED and represents a finished stay for review tests.
--
-- Quick accounts:
-- - normal@test.com / 123456
-- - user2@test.com / 123456

USE hotel_booking;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM security_events;
DELETE FROM login_otps;
DELETE FROM email_verification_tokens;
DELETE FROM password_reset_tokens;
DELETE FROM refresh_tokens;
DELETE FROM hotel_rating_summary;
DELETE FROM reviews;
DELETE FROM bookings;
DELETE FROM room_inventory;
DELETE FROM room_types;
DELETE FROM hotels;
DELETE FROM users;

ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE hotels AUTO_INCREMENT = 1;
ALTER TABLE room_types AUTO_INCREMENT = 1;
ALTER TABLE room_inventory AUTO_INCREMENT = 1;
ALTER TABLE bookings AUTO_INCREMENT = 1;
ALTER TABLE reviews AUTO_INCREMENT = 1;
ALTER TABLE refresh_tokens AUTO_INCREMENT = 1;
ALTER TABLE password_reset_tokens AUTO_INCREMENT = 1;
ALTER TABLE email_verification_tokens AUTO_INCREMENT = 1;
ALTER TABLE login_otps AUTO_INCREMENT = 1;
ALTER TABLE security_events AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1) USERS
-- =========================================================
INSERT INTO users (
    id, email, password, provider, role,
    email_verified, password_changed_at, token_version,
    failed_login_count, lock_until, auth_status,
    failed_password_attempts, failed_otp_attempts, otp_cooldown_until,
    account_created_at, total_bookings, verified_stays, review_count,
    helpful_votes_received, cancel_rate, ip_reputation_score
) VALUES
(
    1, 'normal@test.com',
    '$2a$10$5JjnEcN9N4nPZHPKjai0duBOJG9i21l6mtt9a9dytY9cHNrfWWbgu',
    'LOCAL', 'USER',
    TRUE, '2026-03-20 08:00:00', 2,
    0, NULL, 'NORMAL',
    0, 0, NULL,
    '2025-12-01 09:00:00', 3, 2, 1,
    4, 0.20, 0.98
),
(
    2, 'user2@test.com',
    '$2a$10$5JjnEcN9N4nPZHPKjai0duBOJG9i21l6mtt9a9dytY9cHNrfWWbgu',
    'LOCAL', 'USER',
    TRUE, '2026-03-18 10:00:00', 1,
    0, NULL, 'NORMAL',
    0, 0, NULL,
    '2026-01-15 10:30:00', 1, 0, 1,
    1, 0.00, 0.93
),
(
    3, 'spammer@test.com',
    '$2a$10$5JjnEcN9N4nPZHPKjai0duBOJG9i21l6mtt9a9dytY9cHNrfWWbgu',
    'LOCAL', 'USER',
    TRUE, '2026-03-25 11:00:00', 0,
    0, NULL, 'NORMAL',
    0, 0, NULL,
    '2026-03-24 14:00:00', 0, 0, 1,
    0, 0.00, 0.30
),
(
    4, 'unverified@test.com',
    '$2a$10$5JjnEcN9N4nPZHPKjai0duBOJG9i21l6mtt9a9dytY9cHNrfWWbgu',
    'LOCAL', 'USER',
    FALSE, NULL, 0,
    0, NULL, 'NORMAL',
    0, 0, NULL,
    '2026-03-01 12:00:00', 0, 0, 0,
    0, 0.00, 0.85
),
(
    5, 'otp@test.com',
    '$2a$10$5JjnEcN9N4nPZHPKjai0duBOJG9i21l6mtt9a9dytY9cHNrfWWbgu',
    'LOCAL', 'USER',
    TRUE, '2026-03-22 09:15:00', 0,
    4, NULL, 'OTP_REQUIRED',
    4, 0, NULL,
    '2026-02-10 08:45:00', 0, 0, 0,
    0, 0.00, 0.88
),
(
    6, 'locked@test.com',
    '$2a$10$5JjnEcN9N4nPZHPKjai0duBOJG9i21l6mtt9a9dytY9cHNrfWWbgu',
    'LOCAL', 'USER',
    TRUE, '2026-03-10 07:00:00', 0,
    8, '2026-04-01 00:00:00', 'LOCKED',
    5, 2, '2026-03-30 09:00:00',
    '2026-02-05 16:20:00', 0, 0, 0,
    0, 0.00, 0.40
);

-- =========================================================
-- 2) HOTELS
-- =========================================================
INSERT INTO hotels (id, name, location) VALUES
(1, 'Sun Hotel Hanoi', 'Hanoi'),
(2, 'Saigon Central Hotel', 'HCM'),
(3, 'Lotus West Lake Hotel', 'Hanoi'),
(4, 'Danang Riverside Hotel', 'Da Nang');

-- =========================================================
-- 3) ROOM TYPES
-- =========================================================
INSERT INTO room_types (id, hotel_id, name, base_price_per_night) VALUES
(1, 1, 'Standard Double', 800000.00),
(2, 1, 'Deluxe Family', 1200000.00),
(3, 2, 'Standard Twin', 900000.00),
(4, 2, 'Executive Suite', 1800000.00),
(5, 3, 'Superior Queen', 950000.00),
(6, 3, 'Lake View Suite', 1500000.00),
(7, 4, 'Beach Standard', 1100000.00);

-- =========================================================
-- 4) ROOM INVENTORY
-- Remaining rooms by day after seeded bookings are applied.
--
-- Base idea:
-- - room_type 1 starts with 8 and is untouched for 2026-04-10 -> 2026-04-14
-- - room_type 2 started with 7, booking #2 consumes 2 rooms on 2026-04-11 and 2026-04-12
-- - room_type 3 started with 6, booking #1 consumes 1 room on 2026-04-10 and 2026-04-11
-- - room_type 5 cancelled booking has already restored inventory, so stock remains full
-- - room_type 7 is intentionally 0 to let you test empty search / unavailable inventory
-- =========================================================
INSERT INTO room_inventory (room_type_id, `date`, available_rooms) VALUES
-- room_type 1: free stock, good for happy-path booking creation
(1, '2026-04-10', 8),
(1, '2026-04-11', 8),
(1, '2026-04-12', 8),
(1, '2026-04-13', 8),
(1, '2026-04-14', 8),

-- room_type 2: remaining rooms after a confirmed booking of 2 rooms
(2, '2026-04-10', 7),
(2, '2026-04-11', 5),
(2, '2026-04-12', 5),
(2, '2026-04-13', 7),
(2, '2026-04-14', 7),

-- room_type 3: remaining rooms after a pending-payment booking of 1 room
(3, '2026-04-10', 5),
(3, '2026-04-11', 5),
(3, '2026-04-12', 6),
(3, '2026-04-13', 6),
(3, '2026-04-14', 6),

-- room_type 4: small inventory for edge cases
(4, '2026-04-10', 3),
(4, '2026-04-11', 3),
(4, '2026-04-12', 3),
(4, '2026-04-13', 3),
(4, '2026-04-14', 3),

-- room_type 5: full stock because cancelled booking no longer holds rooms
(5, '2026-04-10', 4),
(5, '2026-04-11', 4),
(5, '2026-04-12', 4),
(5, '2026-04-13', 4),
(5, '2026-04-14', 4),

-- room_type 6: extra Hanoi option for search results
(6, '2026-04-10', 5),
(6, '2026-04-11', 5),
(6, '2026-04-12', 5),
(6, '2026-04-13', 5),
(6, '2026-04-14', 5),

-- room_type 7: sold out to make negative test cases easy
(7, '2026-04-10', 0),
(7, '2026-04-11', 0),
(7, '2026-04-12', 0),
(7, '2026-04-13', 0),
(7, '2026-04-14', 0);

-- =========================================================
-- 5) BOOKINGS / TRANSACTIONS
-- These are the seeded booking states you asked for:
-- - #1 pending payment
-- - #2 confirmed / payment done
-- - #3 cancelled
-- - #4 completed
-- =========================================================
INSERT INTO bookings (
    id, user_id, hotel_id, room_type_id,
    check_in, check_out, rooms, guest_count,
    total_amount, base_price, promotion_discount, tier_discount,
    final_price, commission_amount, hotel_payout,
    status, idempotency_key, payment_provider, payment_order_id,
    paid_at, customer_email, created_at
) VALUES
(
    1, 2, 2, 3,
    '2026-04-10', '2026-04-12', 1, 2,
    1800000.00, 1800000.00, 0.00, 0.00,
    1800000.00, 180000.00, 1620000.00,
    'PENDING_PAYMENT',
    'idem-pending-001',
    'MOMO',
    'MOMO-BOOKING-0001',
    NULL,
    'user2@test.com',
    '2026-03-28 09:00:00'
),
(
    2, 1, 1, 2,
    '2026-04-11', '2026-04-13', 2, 4,
    4800000.00, 4800000.00, 200000.00, 100000.00,
    4500000.00, 450000.00, 4050000.00,
    'CONFIRMED',
    'idem-confirmed-001',
    'VNPAY',
    'VNPAY-BOOKING-0002',
    '2026-03-28 10:15:00',
    'normal@test.com',
    '2026-03-28 10:00:00'
),
(
    3, 1, 3, 5,
    '2026-04-13', '2026-04-14', 1, 2,
    950000.00, 950000.00, 0.00, 0.00,
    950000.00, 95000.00, 855000.00,
    'CANCELLED',
    'idem-cancelled-001',
    'MOMO',
    'MOMO-BOOKING-0003',
    NULL,
    'normal@test.com',
    '2026-03-27 18:20:00'
),
(
    4, 2, 1, 1,
    '2026-04-14', '2026-04-15', 1, 2,
    800000.00, 800000.00, 0.00, 0.00,
    800000.00, 80000.00, 720000.00,
    'COMPLETED',
    'idem-completed-001',
    'MOMO',
    'MOMO-BOOKING-0004',
    '2026-03-20 08:00:00',
    'user2@test.com',
    '2026-03-18 08:00:00'
);

-- =========================================================
-- 6) REVIEWS
-- =========================================================
INSERT INTO reviews (
    id, hotel_id, user_id, star_rating, review_text,
    trust_score, weight, ip_address, verified_stay, created_at
) VALUES
(
    1, 1, 1, 5,
    'Great stay, clean room, smooth check-in.',
    0.95, 1.50, '10.0.0.1', TRUE, '2026-03-28 12:00:00'
),
(
    2, 1, 3, 4,
    'Location is convenient, but room insulation could be better.',
    0.60, 0.90, '10.0.0.3', FALSE, '2026-03-28 13:00:00'
),
(
    3, 2, 2, 4,
    'Nice central location, decent value for money.',
    0.75, 1.50, '10.0.0.2', FALSE, '2026-03-28 14:00:00'
);

-- =========================================================
-- 7) HOTEL RATING SUMMARY
-- =========================================================
INSERT INTO hotel_rating_summary (
    hotel_id, weighted_sum, weight_sum, rating, review_count, updated_at
) VALUES
(1, 10.62, 2.40, 4.4250, 2, '2026-03-28 13:05:00'),
(2,  6.00, 1.50, 4.0000, 1, '2026-03-28 14:05:00'),
(3,  0.00, 0.00, 0.0000, 0, '2026-03-28 15:00:00'),
(4,  0.00, 0.00, 0.0000, 0, '2026-03-28 15:00:00');

-- =========================================================
-- 8) AUTH / TOKEN / SECURITY SUPPORT DATA
-- =========================================================
INSERT INTO refresh_tokens (
    id, token_hash, user_id, expires_at, revoked
) VALUES
(1, SHA2('refresh-normal-001', 256), 1, '2026-04-30 00:00:00', FALSE),
(2, SHA2('refresh-user2-001', 256), 2, '2026-04-30 00:00:00', FALSE),
(3, SHA2('refresh-revoked-001', 256), 1, '2026-03-25 00:00:00', TRUE);

INSERT INTO password_reset_tokens (
    id, token_hash, user_id, expires_at, used_at
) VALUES
(1, SHA2('reset-normal-001', 256), 1, '2026-03-31 23:59:59', NULL),
(2, SHA2('reset-used-001', 256), 2, '2026-03-20 23:59:59', '2026-03-20 20:00:00');

INSERT INTO email_verification_tokens (
    id, token_hash, user_id, expires_at, used_at
) VALUES
(1, SHA2('verify-unverified-001', 256), 4, '2026-04-02 23:59:59', NULL),
(2, SHA2('verify-used-001', 256), 1, '2026-03-10 23:59:59', '2026-03-09 08:00:00');

INSERT INTO login_otps (
    id, user_id, otp_hash, expires_at, used_at, purpose, created_at
) VALUES
(1, 5, SHA2('123456', 256), '2026-03-30 00:10:00', NULL, 'LOGIN_CHALLENGE', '2026-03-29 23:55:00'),
(2, 1, SHA2('654321', 256), '2026-03-20 10:00:00', '2026-03-20 09:30:00', 'LOGIN_CHALLENGE', '2026-03-20 09:20:00');

INSERT INTO security_events (
    id, user_id, event_type, ip, user_agent, metadata, created_at
) VALUES
(1, 1, 'LOGIN_SUCCESS', '10.0.0.1', 'PostmanRuntime/7.43.0', '{"email":"normal@test.com"}', '2026-03-28 08:00:00'),
(2, 2, 'LOGIN_SUCCESS', '10.0.0.2', 'PostmanRuntime/7.43.0', '{"email":"user2@test.com"}', '2026-03-28 08:05:00'),
(3, 5, 'OTP_REQUIRED', '10.0.0.5', 'Mozilla/5.0', '{"reason":"too_many_failed_password_attempts"}', '2026-03-29 23:55:00'),
(4, 6, 'ACCOUNT_LOCKED', '10.0.0.6', 'Mozilla/5.0', '{"reason":"too_many_failed_attempts"}', '2026-03-29 20:00:00'),
(5, 1, 'REFRESH_ROTATED', '10.0.0.1', 'PostmanRuntime/7.43.0', '{"tokenVersion":2}', '2026-03-28 08:30:00');

-- =========================================================
-- 9) QUICK CHECKS
-- =========================================================
-- SELECT room_type_id, date, available_rooms FROM room_inventory ORDER BY room_type_id, date;
-- SELECT id, user_id, hotel_id, room_type_id, status, payment_order_id, paid_at FROM bookings ORDER BY id;
-- SELECT * FROM reviews ORDER BY id;
-- SELECT * FROM hotel_rating_summary ORDER BY hotel_id;

-- =========================================================
-- 10) QUICK TEST MAP
-- =========================================================
-- Happy path booking:
-- POST /api/customer/bookings
-- Idempotency-Key: test-create-001
-- {
--   "hotelId": 1,
--   "roomTypeId": 1,
--   "checkIn": "2026-04-10",
--   "checkOut": "2026-04-12",
--   "rooms": 1,
--   "guestCount": 2,
--   "paymentMethod": "MOMO",
--   "customerEmail": "normal@test.com"
-- }
--
-- Overbooking case:
-- POST /api/customer/bookings
-- Idempotency-Key: test-overbook-001
-- {
--   "hotelId": 1,
--   "roomTypeId": 2,
--   "checkIn": "2026-04-11",
--   "checkOut": "2026-04-13",
--   "rooms": 6,
--   "guestCount": 12,
--   "paymentMethod": "VNPAY",
--   "customerEmail": "normal@test.com"
-- }
--
-- Existing seeded bookings:
-- - booking 1 = PENDING_PAYMENT
-- - booking 2 = CONFIRMED
-- - booking 3 = CANCELLED
-- - booking 4 = COMPLETED
--
-- Mock payment flow:
-- 1) POST /api/auth/login with normal@test.com / 123456
-- 2) POST /api/customer/bookings
-- 3) GET /api/mock-payments/momo/{paymentOrderId}/callback?result=success
-- 4) GET /api/customer/bookings and verify booking becomes CONFIRMED
--
-- Cancel flow:
-- - cancel booking 1 as user2@test.com
-- - cancel booking 2 as normal@test.com
-- - call cancel twice to verify inventory is not restored twice
--
-- Ownership flow:
-- - login user2@test.com
-- - try cancel booking 2
-- - expected: forbidden
--
-- Review flow:
-- POST /api/customer/reviews
-- {
--   "hotelId": 1,
--   "starRating": 5,
--   "reviewText": "Test review from confirmed booking."
-- }
