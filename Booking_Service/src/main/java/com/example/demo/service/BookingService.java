package com.example.demo.service;

import com.example.demo.client.NotificationClient;
import com.example.demo.client.RenterClient;
import com.example.demo.dto.BookingConfirmedEvent;
import com.example.demo.dto.BookingNotificationRequest;
import com.example.demo.dto.CreateBookingRequest;
import com.example.demo.dto.NotificationMetadata;
import com.example.demo.dto.NotificationRecipient;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.entity.Booking;
import com.example.demo.entity.RoomType;
import com.example.demo.exception.BookingConflictException;
import com.example.demo.exception.InsufficientInventoryException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.pricing.PricingResult;
import com.example.demo.pricing.PricingService;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.HotelRepository;
import com.example.demo.repository.RoomInventoryRepository;
import com.example.demo.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final PaymentService paymentService;
    private final NotificationClient notificationClient;
    private final RenterClient renterClient;
    private final PricingService pricingService;

    @Transactional
    public BookingResult createBooking(CreateBookingRequest req, Long userId, String idemKey) {
        if (idemKey != null && !idemKey.isBlank()) {
            var existing = bookingRepository.findByIdempotencyKey(idemKey);
            if (existing.isPresent()) {
                return new BookingResult(existing.get(), null);
            }
        }

        Integer minAvailable = inventoryRepository.minAvailableInRange(
                req.getRoomTypeId(), req.getCheckIn(), req.getCheckOut());
        if (minAvailable == null || minAvailable < req.getRooms()) {
            throw new InsufficientInventoryException("Not enough room inventory for selected date range");
        }

        int updatedRows = inventoryRepository.decreaseInventory(
                req.getRoomTypeId(), req.getRooms(), req.getCheckIn(), req.getCheckOut());
        if (updatedRows == 0) {
            throw new InsufficientInventoryException("Not enough room inventory for selected date range");
        }

        long basePrice = estimateBasePrice(req);
        PricingResult pricing = pricingService.calculatePrice(basePrice);

        Booking booking = Booking.builder()
                .userId(userId)
                .hotelId(req.getHotelId())
                .roomTypeId(req.getRoomTypeId())
                .checkIn(req.getCheckIn())
                .checkOut(req.getCheckOut())
                .rooms(req.getRooms())
                .guestCount(req.getGuestCount())
                .totalAmount(BigDecimal.valueOf(pricing.finalPrice()))
                .basePrice(BigDecimal.valueOf(pricing.basePrice()))
                .promotionDiscount(BigDecimal.valueOf(pricing.promotionDiscount()))
                .tierDiscount(BigDecimal.valueOf(pricing.tierDiscount()))
                .finalPrice(BigDecimal.valueOf(pricing.finalPrice()))
                .commissionAmount(BigDecimal.valueOf(pricing.commissionAmount()))
                .hotelPayout(BigDecimal.valueOf(pricing.hotelPayout()))
                .status(Booking.BookingStatus.PENDING_PAYMENT)
                .idempotencyKey(idemKey)
                .customerEmail(req.getCustomerEmail())
                .createdAt(Instant.now())
                .build();

        Booking saved = bookingRepository.save(booking);
        PaymentService.PaymentSessionResult session = paymentService.createPaymentSession(
                saved,
                req.getPaymentMethod() == null ? null : req.getPaymentMethod().name()
        );
        bookingRepository.save(saved);
        return new BookingResult(saved, new PaymentSession(session.paymentUrl()));
    }

    @Transactional
    public CancelBookingResult cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to cancel this booking");
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BookingConflictException("Booking is already cancelled");
        }
        if (!Booking.cancellableStatuses().contains(booking.getStatus())) {
            throw new BookingConflictException("Booking cannot be cancelled in current status");
        }

        inventoryRepository.increaseInventory(
                booking.getRoomTypeId(),
                booking.getRooms(),
                booking.getCheckIn(),
                booking.getCheckOut()
        );
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        sendBookingCancelledNotificationSafely(booking);
        return new CancelBookingResult(booking.getId(), booking.getStatus(), true);
    }

    @Transactional
    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        Booking booking = bookingRepository.findByPaymentOrderId(event.paymentOrderId())
                .orElseThrow(() -> new RuntimeException("Booking not found for payment order: " + event.paymentOrderId()));

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            return;
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaidAt(Instant.now());
        bookingRepository.save(booking);

        notifyRenter(booking);
        sendBookingConfirmationNotificationSafely(booking);
        sendPaymentSuccessNotificationSafely(booking);
    }

    public void notifyRenter(Booking booking) {
        String roomType = roomTypeRepository.findById(booking.getRoomTypeId())
                .map(RoomType::getName)
                .orElse("RoomType-" + booking.getRoomTypeId());

        String guestName = (booking.getCustomerEmail() == null || booking.getCustomerEmail().isBlank())
                ? "Guest-" + booking.getUserId()
                : booking.getCustomerEmail();

        BookingNotificationRequest request = BookingNotificationRequest.builder()
                .bookingId(booking.getId())
                .hotelId(booking.getHotelId())
                .guestName(guestName)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .roomType(roomType)
                .totalPrice(booking.getFinalPrice() == null ? booking.getTotalAmount() : booking.getFinalPrice())
                .build();
        renterClient.sendBookingNotification(request);
    }

    private void sendBookingConfirmationNotificationSafely(Booking booking) {
        if (booking.getCustomerEmail() == null || booking.getCustomerEmail().isBlank()) {
            return;
        }

        try {
            String hotelName = hotelRepository.findById(booking.getHotelId())
                    .map(hotel -> hotel.getName() == null || hotel.getName().isBlank() ? "Hotel-" + hotel.getId() : hotel.getName())
                    .orElse("Hotel-" + booking.getHotelId());

            notificationClient.send(NotificationRequest.builder()
                    .type("BOOKING_CONFIRMATION")
                    .recipient(NotificationRecipient.builder()
                            .email(booking.getCustomerEmail())
                            .name(booking.getCustomerEmail())
                            .build())
                    .payload(Map.of(
                            "bookingCode", String.valueOf(booking.getId()),
                            "hotelName", hotelName,
                            "checkInDate", booking.getCheckIn().toString(),
                            "checkOutDate", booking.getCheckOut().toString(),
                            "totalAmount", booking.getFinalPrice() == null ? booking.getTotalAmount() : booking.getFinalPrice()
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("booking-confirmation-booking-" + booking.getId())
                            .build())
                    .build());
        } catch (Exception exception) {
            log.warn("Failed to send booking confirmation notification for booking {}", booking.getId(), exception);
        }
    }

    private void sendPaymentSuccessNotificationSafely(Booking booking) {
        if (booking.getCustomerEmail() == null || booking.getCustomerEmail().isBlank()) {
            return;
        }

        try {
            notificationClient.send(NotificationRequest.builder()
                    .type("PAYMENT_SUCCESS")
                    .recipient(NotificationRecipient.builder()
                            .email(booking.getCustomerEmail())
                            .name(booking.getCustomerEmail())
                            .build())
                    .payload(Map.of(
                            "paymentCode", booking.getPaymentOrderId(),
                            "bookingCode", String.valueOf(booking.getId()),
                            "amount", booking.getFinalPrice() == null ? booking.getTotalAmount() : booking.getFinalPrice()
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("payment-success-booking-" + booking.getId())
                            .build())
                    .build());
        } catch (Exception exception) {
            log.warn("Failed to send payment success notification for booking {}", booking.getId(), exception);
        }
    }

    private void sendBookingCancelledNotificationSafely(Booking booking) {
        if (booking.getCustomerEmail() == null || booking.getCustomerEmail().isBlank()) {
            return;
        }

        try {
            notificationClient.send(NotificationRequest.builder()
                    .type("BOOKING_CANCELLED")
                    .recipient(NotificationRecipient.builder()
                            .email(booking.getCustomerEmail())
                            .name(booking.getCustomerEmail())
                            .build())
                    .payload(Map.of(
                            "bookingCode", String.valueOf(booking.getId()),
                            "reason", "Cancelled by user"
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("booking-cancelled-booking-" + booking.getId())
                            .build())
                    .build());
        } catch (Exception exception) {
            log.warn("Failed to send booking cancelled notification for booking {}", booking.getId(), exception);
        }
    }

    private long estimateBasePrice(CreateBookingRequest req) {
        long nights = ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());
        long safeNights = Math.max(1L, nights);
        int safeRooms = Math.max(1, req.getRooms());

        Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(req.getRoomTypeId());
        BigDecimal basePrice = roomTypeOpt.map(RoomType::getBasePricePerNight)
                .orElse(BigDecimal.valueOf(100_000L));

        BigDecimal total = basePrice
                .multiply(BigDecimal.valueOf(safeNights))
                .multiply(BigDecimal.valueOf(safeRooms));
        return total.longValue();
    }

    public record BookingResult(Booking booking, PaymentSession paymentSession) {}
    public record PaymentSession(String paymentUrl) {}
    public record CancelBookingResult(Long bookingId, Booking.BookingStatus status, boolean inventoryRestored) {}
}
