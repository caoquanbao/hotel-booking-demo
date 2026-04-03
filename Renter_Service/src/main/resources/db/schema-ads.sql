CREATE TABLE IF NOT EXISTS campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    bid_price DECIMAL(19,2) NOT NULL,
    daily_budget DECIMAL(19,2) NOT NULL,
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
    remaining_budget DECIMAL(19,2) NOT NULL,
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
    price_charged DECIMAL(19,2) NOT NULL,

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
    avg_bid DECIMAL(19,2) NOT NULL,
    p50_bid DECIMAL(19,2) NOT NULL,
    p75_bid DECIMAL(19,2) NOT NULL,
    p90_bid DECIMAL(19,2) NOT NULL,
    avg_ctr DOUBLE NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);