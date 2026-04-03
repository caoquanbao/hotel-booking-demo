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

CREATE TABLE IF NOT EXISTS hotels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),

    INDEX idx_hotels_location (location),
    INDEX idx_hotels_name (name)
);

CREATE TABLE IF NOT EXISTS room_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    base_price_per_night DECIMAL(19, 2),

    CONSTRAINT fk_room_types_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,

    UNIQUE KEY uk_room_types_hotel_name (hotel_id, name),
    INDEX idx_room_types_hotel (hotel_id)
);

CREATE TABLE IF NOT EXISTS room_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_type_id BIGINT NOT NULL,
    `date` DATE NOT NULL,
    available_rooms INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_room_inventory_room_type
        FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE,

    UNIQUE KEY uk_room_inventory_room_date (room_type_id, `date`),
    INDEX idx_room_inventory_room_date (room_type_id, `date`)
);

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
    base_price DECIMAL(19, 2),
    promotion_discount DECIMAL(19, 2),
    tier_discount DECIMAL(19, 2),
    final_price DECIMAL(19, 2) NOT NULL,
    commission_amount DECIMAL(19, 2),
    hotel_payout DECIMAL(19, 2),

    status ENUM(
        'PENDING_PAYMENT',
        'CONFIRMED',
        'PAID',
        'CANCELLED',
        'COMPLETED'
    ) NOT NULL,

    idempotency_key VARCHAR(128),
    payment_provider VARCHAR(32),
    payment_order_id VARCHAR(128),
    paid_at TIMESTAMP NULL,
    customer_email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_bookings_room_type FOREIGN KEY (room_type_id) REFERENCES room_types(id),

    UNIQUE KEY uk_bookings_idem (idempotency_key),
    UNIQUE KEY uk_bookings_payment (payment_order_id),

    INDEX idx_bookings_user_created (user_id, created_at),
    INDEX idx_bookings_hotel_status (hotel_id, status),
    INDEX idx_bookings_room_dates (room_type_id, check_in, check_out),
    INDEX idx_bookings_status_created (status, created_at)
);

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

    CONSTRAINT fk_reviews_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),

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

    CONSTRAINT fk_rating_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id),

    INDEX idx_refresh_user (user_id),
    INDEX idx_refresh_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,

    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id),

    INDEX idx_prt_user (user_id),
    INDEX idx_prt_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,

    CONSTRAINT fk_evt_user FOREIGN KEY (user_id) REFERENCES users(id),

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

    CONSTRAINT fk_login_otps_user FOREIGN KEY (user_id) REFERENCES users(id),

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

    ip VARCHAR(64),
    user_agent VARCHAR(512),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_security_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_security_user (user_id),
    INDEX idx_security_created (created_at),
    INDEX idx_security_type_created (event_type, created_at)
);
