USE hotel_booking;
-- =========================================================
-- USERS
-- =========================================================
INSERT INTO users (
    id, email, password, provider, role, email_verified, password_changed_at, token_version,
    failed_login_count, lock_until, auth_status, failed_password_attempts, failed_otp_attempts,
    otp_cooldown_until, account_created_at, total_bookings, verified_stays, review_count,
    helpful_votes_received, cancel_rate, ip_reputation_score
) VALUES
    (1, 'owner1@hotelbooking.local', '$2a$10$owner.demo.hash', 'LOCAL', 'OWNER', TRUE, NOW() - INTERVAL 30 DAY, 0,
     0, NULL, 'NORMAL', 0, 0, NULL, NOW() - INTERVAL 120 DAY, 8, 0, 0, 0, 0.00, 1.00),
    (2, 'customer1@hotelbooking.local', '$2a$10$customer.demo.hash', 'LOCAL', 'CUSTOMER', TRUE, NOW() - INTERVAL 15 DAY, 0,
     0, NULL, 'NORMAL', 0, 0, NULL, NOW() - INTERVAL 90 DAY, 5, 3, 2, 4, 0.05, 0.98),
    (3, 'renter.verified@hotelbooking.local', '$2a$10$renter.verified.hash', 'LOCAL', 'OWNER', TRUE, NOW() - INTERVAL 10 DAY, 0,
     0, NULL, 'NORMAL', 0, 0, NULL, NOW() - INTERVAL 60 DAY, 0, 0, 0, 0, 0.00, 0.99),
    (4, 'renter.rejected@hotelbooking.local', '$2a$10$renter.rejected.hash', 'LOCAL', 'OWNER', TRUE, NOW() - INTERVAL 10 DAY, 0,
     0, NULL, 'NORMAL', 0, 0, NULL, NOW() - INTERVAL 45 DAY, 0, 0, 0, 0, 0.00, 0.95),
    (5, 'renter.pending@hotelbooking.local', '$2a$10$renter.pending.hash', 'LOCAL', 'OWNER', FALSE, NULL, 0,
     0, NULL, 'OTP_REQUIRED', 1, 0, NOW() + INTERVAL 5 MINUTE, NOW() - INTERVAL 7 DAY, 0, 0, 0, 0, 0.00, 0.92);

-- =========================================================
-- HOTELS
-- =========================================================
INSERT INTO hotels (id, external_hotel_id, name, location) VALUES
    (1, 'HOTEL-001', 'Sunrise Hanoi Hotel', 'Ha Noi'),
    (2, 'HOTEL-002', 'Saigon Riverside Suites', 'Ho Chi Minh City');

-- =========================================================
-- ROOM TYPES
-- =========================================================
INSERT INTO room_types (id, hotel_id, external_room_type_id, name, base_price_per_night) VALUES
    (1, 1, 'DLX-HN-01', 'Deluxe King', 1200000.00),
    (2, 1, 'STE-HN-02', 'Family Suite', 1800000.00),
    (3, 2, 'SUP-HCM-01', 'Superior Queen', 950000.00),
    (4, 2, 'DLX-HCM-02', 'Deluxe River View', 1500000.00);

-- =========================================================
-- INVENTORY
-- =========================================================
INSERT INTO room_inventory (id, room_type_id, `date`, available_rooms) VALUES
    (1, 1, CURDATE() + INTERVAL 1 DAY, 8),
    (2, 1, CURDATE() + INTERVAL 2 DAY, 8),
    (3, 1, CURDATE() + INTERVAL 3 DAY, 7),
    (4, 2, CURDATE() + INTERVAL 1 DAY, 4),
    (5, 2, CURDATE() + INTERVAL 2 DAY, 4),
    (6, 2, CURDATE() + INTERVAL 3 DAY, 3),
    (7, 3, CURDATE() + INTERVAL 1 DAY, 10),
    (8, 3, CURDATE() + INTERVAL 2 DAY, 10),
    (9, 3, CURDATE() + INTERVAL 3 DAY, 9),
    (10, 4, CURDATE() + INTERVAL 1 DAY, 6),
    (11, 4, CURDATE() + INTERVAL 2 DAY, 6),
    (12, 4, CURDATE() + INTERVAL 3 DAY, 5);

INSERT INTO room_inventory_daily (
    id, hotel_id, room_type_id, rate_plan_id, stay_date, total_inventory, sold_inventory,
    status, min_stay, max_stay, closed_to_arrival, closed_to_departure, created_at, updated_at
) VALUES
    (1, 1, 1, 'BAR', CURDATE() + INTERVAL 1 DAY, 8, 2, 'OPEN', 1, 7, FALSE, FALSE, NOW(), NOW()),
    (2, 1, 1, 'BAR', CURDATE() + INTERVAL 2 DAY, 8, 3, 'OPEN', 1, 7, FALSE, FALSE, NOW(), NOW()),
    (3, 1, 1, 'BAR', CURDATE() + INTERVAL 3 DAY, 7, 1, 'OPEN', 1, 7, FALSE, FALSE, NOW(), NOW()),
    (4, 1, 2, 'BAR', CURDATE() + INTERVAL 1 DAY, 4, 1, 'OPEN', 2, 7, FALSE, FALSE, NOW(), NOW()),
    (5, 1, 2, 'BAR', CURDATE() + INTERVAL 2 DAY, 4, 1, 'OPEN', 2, 7, FALSE, FALSE, NOW(), NOW()),
    (6, 1, 2, 'BAR', CURDATE() + INTERVAL 3 DAY, 3, 0, 'OPEN', 2, 7, FALSE, FALSE, NOW(), NOW()),
    (7, 2, 3, 'BAR', CURDATE() + INTERVAL 1 DAY, 10, 4, 'OPEN', 1, 5, FALSE, FALSE, NOW(), NOW()),
    (8, 2, 3, 'BAR', CURDATE() + INTERVAL 2 DAY, 10, 5, 'OPEN', 1, 5, FALSE, FALSE, NOW(), NOW()),
    (9, 2, 3, 'BAR', CURDATE() + INTERVAL 3 DAY, 9, 3, 'OPEN', 1, 5, FALSE, FALSE, NOW(), NOW()),
    (10, 2, 4, 'BAR', CURDATE() + INTERVAL 1 DAY, 6, 1, 'OPEN', 1, 5, FALSE, FALSE, NOW(), NOW()),
    (11, 2, 4, 'BAR', CURDATE() + INTERVAL 2 DAY, 6, 2, 'OPEN', 1, 5, FALSE, FALSE, NOW(), NOW()),
    (12, 2, 4, 'BAR', CURDATE() + INTERVAL 3 DAY, 5, 1, 'OPEN', 1, 5, FALSE, FALSE, NOW(), NOW());

-- =========================================================
-- BOOKINGS
-- =========================================================
INSERT INTO bookings (
    id, user_id, hotel_id, room_type_id, check_in, check_out, rooms, guest_count,
    total_amount, base_price, promotion_discount, tier_discount, final_price,
    commission_amount, hotel_payout, status, idempotency_key, payment_provider,
    payment_order_id, paid_at, customer_email, created_at
) VALUES
    (1, 2, 1, 1, CURDATE() + INTERVAL 2 DAY, CURDATE() + INTERVAL 4 DAY, 1, 2,
     2400000.00, 2400000.00, 150000.00, 50000.00, 2200000.00,
     660000.00, 1540000.00, 'CONFIRMED', 'idem-booking-001', 'VNPAY',
     'pay-order-001', NOW() - INTERVAL 2 HOUR, 'customer1@hotelbooking.local', NOW() - INTERVAL 1 DAY),
    (2, 2, 2, 3, CURDATE() + INTERVAL 3 DAY, CURDATE() + INTERVAL 5 DAY, 1, 2,
     1900000.00, 1900000.00, 0.00, 0.00, 1900000.00,
     570000.00, 1330000.00, 'PENDING_PAYMENT', 'idem-booking-002', 'MOMO',
     'pay-order-002', NULL, 'customer1@hotelbooking.local', NOW() - INTERVAL 12 HOUR),
    (3, 2, 1, 2, CURDATE() + INTERVAL 5 DAY, CURDATE() + INTERVAL 7 DAY, 1, 4,
     3600000.00, 3600000.00, 200000.00, 100000.00, 3300000.00,
     990000.00, 2310000.00, 'CANCELLED', 'idem-booking-003', 'VNPAY',
     'pay-order-003', NULL, 'customer1@hotelbooking.local', NOW() - INTERVAL 6 HOUR),
    (4, 2, 1, 1, CURDATE() - INTERVAL 10 DAY, CURDATE() - INTERVAL 8 DAY, 1, 2,
     1500000.00, 1500000.00, 0.00, 0.00, 1500000.00,
     450000.00, 1050000.00, 'COMPLETED', 'idem-booking-004', 'VNPAY',
     'pay-order-004', NOW() - INTERVAL 11 DAY, 'customer1@hotelbooking.local', NOW() - INTERVAL 20 DAY),
    (5, 2, 2, 4, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 18 DAY, 1, 2,
     1300000.00, 1300000.00, 0.00, 0.00, 1300000.00,
     390000.00, 910000.00, 'COMPLETED', 'idem-booking-005', 'MOMO',
     'pay-order-005', NOW() - INTERVAL 21 DAY, 'customer1@hotelbooking.local', NOW() - INTERVAL 30 DAY);

-- =========================================================
-- RENTER VERIFICATION
-- =========================================================
INSERT INTO renter_verifications (
    id, user_id, cccd_number, mst_number, bank_account_number, bank_code,
    account_holder_name, verification_status, created_at, updated_at
) VALUES
    (1, 3, '001095000001', '0312345678', '1234567890', 'VCB', 'NGUYEN VAN A', 'VERIFIED', NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 13 DAY),
    (2, 4, '079099000002', '0309876543', '00001234', 'TCB', NULL, 'REJECTED', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
    (3, 5, '048203000003', '0101234567', '9876543210', 'ACB', NULL, 'PENDING', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

-- =========================================================
-- OWNER NOTIFICATIONS
-- =========================================================
INSERT INTO owner_notifications (id, hotel_id, booking_id, message, created_at, is_read) VALUES
    (1, 1, 1, 'You have a new booking. Payment completed.', NOW() - INTERVAL 90 MINUTE, FALSE);

-- =========================================================
-- ADS
-- =========================================================
INSERT INTO campaigns (id, hotel_id, bid_price, daily_budget, start_time, end_time, status) VALUES
    (101, 1, 6500.00, 50000.00, NOW() - INTERVAL 2 DAY, NOW() + INTERVAL 10 DAY, 'ACTIVE'),
    (102, 2, 4800.00, 8000.00, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 7 DAY, 'ACTIVE'),
    (103, 1, 7200.00, 30000.00, NOW() - INTERVAL 3 DAY, NOW() + INTERVAL 5 DAY, 'PAUSED'),
    (104, 2, 7000.00, 3000.00, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 3 DAY, 'ACTIVE');

INSERT INTO campaign_wallet (campaign_id, remaining_budget, is_out_of_budget, updated_at) VALUES
    (101, 42000.00, FALSE, NOW() - INTERVAL 10 MINUTE),
    (102, 1200.00, FALSE, NOW() - INTERVAL 5 MINUTE),
    (103, 25000.00, FALSE, NOW() - INTERVAL 15 MINUTE),
    (104, 0.00, TRUE, NOW() - INTERVAL 2 MINUTE);

INSERT INTO ads_click_log (id, campaign_id, user_id, hotel_id, click_time, price_charged) VALUES
    (1, 101, 2, 1, NOW() - INTERVAL 9 HOUR, 1200.00),
    (2, 101, 2, 1, NOW() - INTERVAL 8 HOUR, 1300.00),
    (3, 101, NULL, 1, NOW() - INTERVAL 7 HOUR, 1100.00),
    (4, 102, 2, 2, NOW() - INTERVAL 6 HOUR, 900.00),
    (5, 102, NULL, 2, NOW() - INTERVAL 5 HOUR, 950.00),
    (6, 102, 2, 2, NOW() - INTERVAL 4 HOUR, 1000.00),
    (7, 103, 2, 1, NOW() - INTERVAL 3 HOUR, 1400.00),
    (8, 101, 2, 1, NOW() - INTERVAL 2 HOUR, 1250.00),
    (9, 102, 2, 2, NOW() - INTERVAL 70 MINUTE, 980.00),
    (10, 101, NULL, 1, NOW() - INTERVAL 20 MINUTE, 1350.00);

INSERT INTO ads_auction_stats (city, avg_bid, p50_bid, p75_bid, p90_bid, avg_ctr, updated_at) VALUES
    ('Ha Noi', 5200.00, 5000.00, 6200.00, 7100.00, 0.073, NOW()),
    ('Ho Chi Minh City', 4800.00, 4600.00, 5900.00, 6800.00, 0.069, NOW());

-- =========================================================
-- PAYOUT
-- =========================================================
INSERT INTO hotel_payouts (id, hotel_id, total_amount, status, created_at, paid_at) VALUES
    (201, 2, 910000.00, 'PENDING', NOW() - INTERVAL 2 DAY, NULL);

INSERT INTO payout_bookings (booking_id, payout_id, booking_amount, commission_amount, hotel_net_amount) VALUES
    (5, 201, 1300000.00, 390000.00, 910000.00);

-- =========================================================
-- OPTIONAL RESET OF AUTO_INCREMENT FOR READABLE NEXT INSERTS
-- =========================================================
ALTER TABLE users AUTO_INCREMENT = 6;
ALTER TABLE hotels AUTO_INCREMENT = 3;
ALTER TABLE room_types AUTO_INCREMENT = 5;
ALTER TABLE room_inventory AUTO_INCREMENT = 13;
ALTER TABLE room_inventory_daily AUTO_INCREMENT = 13;
ALTER TABLE bookings AUTO_INCREMENT = 6;
ALTER TABLE renter_verifications AUTO_INCREMENT = 4;
ALTER TABLE owner_notifications AUTO_INCREMENT = 2;
ALTER TABLE campaigns AUTO_INCREMENT = 105;
ALTER TABLE ads_click_log AUTO_INCREMENT = 11;
ALTER TABLE hotel_payouts AUTO_INCREMENT = 202;
