CREATE DATABASE IF NOT EXISTS hotel_booking;
USE hotel_booking;

-- =========================================================
-- USERS
-- =========================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NULL,

    provider ENUM('LOCAL', 'GOOGLE') NOT NULL,
    role VARCHAR(50) NOT NULL,

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    password_changed_at TIMESTAMP NULL,
    token_version BIGINT NOT NULL DEFAULT 0,

    failed_login_count INT NOT NULL DEFAULT 0,
    lock_until TIMESTAMP NULL,
    auth_status ENUM('NORMAL', 'OTP_REQUIRED', 'LOCKED') NOT NULL DEFAULT 'NORMAL',
    failed_password_attempts INT NOT NULL DEFAULT 0,
    failed_otp_attempts INT NOT NULL DEFAULT 0,
    otp_cooldown_until TIMESTAMP NULL,

    account_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_bookings INT NOT NULL DEFAULT 0,
    verified_stays INT NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    helpful_votes_received INT NOT NULL DEFAULT 0,
    cancel_rate DOUBLE NOT NULL DEFAULT 0,
    ip_reputation_score DOUBLE NOT NULL DEFAULT 1,

    CONSTRAINT chk_users_cancel_rate CHECK (cancel_rate BETWEEN 0 AND 1),

    INDEX idx_users_role (role),
    INDEX idx_users_auth_status (auth_status),
    INDEX idx_users_lock_until (lock_until),
    INDEX idx_users_created (account_created_at)
);

-- =========================================================
-- HOTELS
-- Merge from:
-- - DB1: hotels(id, hotel_id VARCHAR unique)
-- - DB2: hotels(id, name, location)
-- Giờ giữ internal PK + external_hotel_id để không conflict
-- =========================================================
CREATE TABLE IF NOT EXISTS hotels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_hotel_id VARCHAR(64) NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NULL,

    INDEX idx_hotels_location (location),
    INDEX idx_hotels_name (name)
);

-- =========================================================
-- ROOM TYPES
-- Merge from:
-- - DB2: room_types(hotel_id FK, name, base_price_per_night)
-- - DB1: hotel_room_types(hotel_id varchar, room_type_id varchar)
-- Giờ nhét external_room_type_id vào cùng table room_types
-- =========================================================
CREATE TABLE IF NOT EXISTS room_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    external_room_type_id VARCHAR(64) NULL,
    name VARCHAR(255) NOT NULL,
    base_price_per_night DECIMAL(19, 2) NULL,

    CONSTRAINT fk_room_types_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,

    UNIQUE KEY uk_room_types_hotel_name (hotel_id, name),
    UNIQUE KEY uk_room_types_hotel_external (hotel_id, external_room_type_id),

    INDEX idx_room_types_hotel (hotel_id)
);

-- =========================================================
-- SIMPLE INVENTORY
-- DB2 style
-- =========================================================
CREATE TABLE IF NOT EXISTS room_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_type_id BIGINT NOT NULL,
    `date` DATE NOT NULL,
    available_rooms INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_room_inventory_room_type
        FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE,

    CONSTRAINT chk_room_inventory_available_non_negative
        CHECK (available_rooms >= 0),

    UNIQUE KEY uk_room_inventory_room_date (room_type_id, `date`),
    INDEX idx_room_inventory_room_date (room_type_id, `date`)
);

-- =========================================================
-- ADVANCED DAILY INVENTORY
-- DB1 style, nhưng map sang internal hotel_id / room_type_id
-- rate_plan_id vẫn để VARCHAR vì chưa có bảng rate_plans riêng
-- =========================================================
CREATE TABLE IF NOT EXISTS room_inventory_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    rate_plan_id VARCHAR(64) NOT NULL,
    stay_date DATE NOT NULL,
    total_inventory INT NOT NULL,
    sold_inventory INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    min_stay INT NULL,
    max_stay INT NULL,
    closed_to_arrival BOOLEAN NOT NULL DEFAULT FALSE,
    closed_to_departure BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_daily_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_daily_room_type
        FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE,

    CONSTRAINT chk_inventory_daily_non_negative
        CHECK (total_inventory >= 0 AND sold_inventory >= 0),

    UNIQUE KEY uk_inventory_daily (hotel_id, room_type_id, rate_plan_id, stay_date),
    INDEX idx_inventory_hotel_date (hotel_id, stay_date),
    INDEX idx_inventory_room_date (room_type_id, stay_date)
);

-- =========================================================
-- BOOKINGS
-- Merge from both DBs
-- =========================================================
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,

    check_in DATE NOT NULL,
    check_out DATE NOT NULL,

    rooms INT NOT NULL,
    guest_count INT NOT NULL,

    total_amount DECIMAL(19, 2) NOT NULL,
    base_price DECIMAL(19, 2) NULL,
    promotion_discount DECIMAL(19, 2) NULL,
    tier_discount DECIMAL(19, 2) NULL,
    final_price DECIMAL(19, 2) NOT NULL,
    commission_amount DECIMAL(19, 2) NULL,
    hotel_payout DECIMAL(19, 2) NULL,

    status ENUM(
        'PENDING_PAYMENT',
        'CONFIRMED',
        'PAID',
        'CANCELLED',
        'COMPLETED'
    ) NOT NULL,

    idempotency_key VARCHAR(128) NULL,
    payment_provider VARCHAR(32) NULL,
    payment_order_id VARCHAR(128) NULL,
    paid_at TIMESTAMP NULL,
    customer_email VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_bookings_room_type
        FOREIGN KEY (room_type_id) REFERENCES room_types(id),

    UNIQUE KEY uk_bookings_idem (idempotency_key),
    UNIQUE KEY uk_bookings_payment (payment_order_id),

    INDEX idx_bookings_user_created (user_id, created_at),
    INDEX idx_bookings_hotel_status (hotel_id, status),
    INDEX idx_bookings_room_dates (room_type_id, check_in, check_out),
    INDEX idx_bookings_status_created (status, created_at),
    INDEX idx_booking_status_checkout (status, check_out)
);

-- =========================================================
-- REVIEWS + RATING SUMMARY
-- =========================================================
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    star_rating INT NOT NULL,
    review_text VARCHAR(3000) NOT NULL,

    trust_score DOUBLE NOT NULL,
    weight DOUBLE NOT NULL,

    ip_address VARCHAR(64) NOT NULL,
    verified_stay BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    INDEX idx_reviews_hotel_created (hotel_id, created_at),
    INDEX idx_reviews_user_created (user_id, created_at),
    INDEX idx_reviews_ip_created (ip_address, created_at)
);

CREATE TABLE IF NOT EXISTS hotel_rating_summary (
    hotel_id BIGINT PRIMARY KEY,
    weighted_sum DOUBLE NOT NULL,
    weight_sum DOUBLE NOT NULL,
    rating DOUBLE NOT NULL,
    review_count INT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rating_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
);

-- =========================================================
-- AUTH / SECURITY TABLES
-- =========================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_refresh_user (user_id),
    INDEX idx_refresh_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,

    CONSTRAINT fk_prt_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_prt_user (user_id),
    INDEX idx_prt_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,

    CONSTRAINT fk_evt_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_evt_user (user_id),
    INDEX idx_evt_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS login_otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    purpose ENUM('LOGIN_CHALLENGE') NOT NULL DEFAULT 'LOGIN_CHALLENGE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_login_otps_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_login_otp_user (user_id, purpose, used_at),
    INDEX idx_login_otp_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS security_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,

    event_type ENUM(
        'LOGIN_SUCCESS',
        'LOGIN_FAILED',
        'OTP_REQUIRED',
        'OTP_SUCCESS',
        'OTP_FAILED',
        'OTP_COOLDOWN',
        'PASSWORD_CHANGED',
        'LOGOUT',
        'ACCOUNT_LOCKED',
        'REFRESH_ROTATED'
    ) NOT NULL,

    ip VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    metadata TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_security_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_security_user (user_id),
    INDEX idx_security_created (created_at),
    INDEX idx_security_type_created (event_type, created_at)
);

-- =========================================================
-- REQUEST LOG
-- =========================================================
CREATE TABLE IF NOT EXISTS request_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- RENTER VERIFICATION
-- =========================================================
CREATE TABLE IF NOT EXISTS renter_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cccd_number VARCHAR(12) NOT NULL,
    mst_number VARCHAR(14) NOT NULL,
    bank_account_number VARCHAR(32) NOT NULL,
    bank_code VARCHAR(16) NOT NULL,
    account_holder_name VARCHAR(255) NULL,
    verification_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_renter_verifications_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT chk_renter_verification_status
        CHECK (
            verification_status IN (
                'PENDING',
                'CCCD_VALID',
                'MST_VALID',
                'BANK_VERIFIED',
                'VERIFIED',
                'REJECTED'
            )
        ),

    INDEX idx_renter_verification_user (user_id)
);

-- =========================================================
-- OWNER NOTIFICATIONS
-- =========================================================
CREATE TABLE IF NOT EXISTS owner_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL UNIQUE,
    message VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_owner_notifications_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    CONSTRAINT fk_owner_notifications_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,

    INDEX idx_owner_notification_hotel (hotel_id, created_at)
);

-- =========================================================
-- ADS / CAMPAIGNS
-- =========================================================
CREATE TABLE IF NOT EXISTS campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    bid_price DECIMAL(19, 2) NOT NULL,
    daily_budget DECIMAL(19, 2) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(16) NOT NULL,

    CONSTRAINT fk_campaigns_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,

    CONSTRAINT chk_campaign_status
        CHECK (status IN ('ACTIVE', 'PAUSED', 'ENDED')),

    INDEX idx_campaign_hotel (hotel_id),
    INDEX idx_campaign_status_time (status, start_time, end_time)
);

CREATE TABLE IF NOT EXISTS campaign_wallet (
    campaign_id BIGINT PRIMARY KEY,
    remaining_budget DECIMAL(19, 2) NOT NULL,
    is_out_of_budget BOOLEAN NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_campaign_wallet
        FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ads_click_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    hotel_id BIGINT NOT NULL,
    click_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    price_charged DECIMAL(19, 2) NOT NULL,

    CONSTRAINT fk_ads_click_log_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    CONSTRAINT fk_ads_click_log_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_ads_click_log_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,

    INDEX idx_ads_click_campaign_time (campaign_id, click_time),
    INDEX idx_ads_click_hotel_time (hotel_id, click_time)
);

CREATE TABLE IF NOT EXISTS ads_auction_stats (
    city VARCHAR(128) PRIMARY KEY,
    avg_bid DECIMAL(19, 2) NOT NULL,
    p50_bid DECIMAL(19, 2) NOT NULL,
    p75_bid DECIMAL(19, 2) NOT NULL,
    p90_bid DECIMAL(19, 2) NOT NULL,
    avg_ctr DOUBLE NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================================================
-- PAYOUTS
-- =========================================================
CREATE TABLE IF NOT EXISTS hotel_payouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,

    CONSTRAINT fk_hotel_payouts_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,

    CONSTRAINT chk_hotel_payout_status
        CHECK (status IN ('PENDING', 'PAID')),

    INDEX idx_hotel_payout_hotel (hotel_id),
    INDEX idx_hotel_payout_status (status)
);

CREATE TABLE IF NOT EXISTS payout_bookings (
    booking_id BIGINT PRIMARY KEY,
    payout_id BIGINT NOT NULL,
    booking_amount DECIMAL(19, 2) NOT NULL,
    commission_amount DECIMAL(19, 2) NOT NULL,
    hotel_net_amount DECIMAL(19, 2) NOT NULL,

    CONSTRAINT fk_payout_bookings_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_payout_bookings_payout
        FOREIGN KEY (payout_id) REFERENCES hotel_payouts(id) ON DELETE CASCADE,

    INDEX idx_payout_booking_payout (payout_id)
);