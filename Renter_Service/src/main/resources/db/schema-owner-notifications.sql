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