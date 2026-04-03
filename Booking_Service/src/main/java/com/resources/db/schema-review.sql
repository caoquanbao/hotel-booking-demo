-- Review & Rating with Trust Score Weighting

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    account_created_at DATETIME NOT NULL,
    total_bookings INT NOT NULL DEFAULT 0,
    verified_stays INT NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    helpful_votes_received INT NOT NULL DEFAULT 0,
    cancel_rate DOUBLE NOT NULL DEFAULT 0,
    ip_reputation_score DOUBLE NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    star_rating INT NOT NULL,
    review_text VARCHAR(3000) NOT NULL,
    trust_score DOUBLE NOT NULL,
    weight DOUBLE NOT NULL,
    ip_address VARCHAR(64) NOT NULL,
    verified_stay BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_review_hotel_created (hotel_id, created_at),
    INDEX idx_review_user_created (user_id, created_at),
    INDEX idx_review_ip_created (ip_address, created_at),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS hotel_rating_summary (
    hotel_id BIGINT PRIMARY KEY,
    weighted_sum DOUBLE NOT NULL,
    weight_sum DOUBLE NOT NULL,
    rating DOUBLE NOT NULL,
    review_count INT NOT NULL,
    updated_at DATETIME NOT NULL
);
