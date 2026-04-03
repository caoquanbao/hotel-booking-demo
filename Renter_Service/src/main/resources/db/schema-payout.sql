CREATE TABLE IF NOT EXISTS hotel_payouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
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
    booking_amount DECIMAL(19,2) NOT NULL,
    commission_amount DECIMAL(19,2) NOT NULL,
    hotel_net_amount DECIMAL(19,2) NOT NULL,

    CONSTRAINT fk_payout_bookings_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,

    CONSTRAINT fk_payout_bookings_payout
        FOREIGN KEY (payout_id) REFERENCES hotel_payouts(id) ON DELETE CASCADE,

    INDEX idx_payout_booking_payout (payout_id)
);