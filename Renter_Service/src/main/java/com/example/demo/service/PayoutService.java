package com.example.demo.service;

import com.example.demo.entity.BookingRecord;
import com.example.demo.entity.HotelPayout;
import com.example.demo.entity.PayoutBooking;
import com.example.demo.entity.PayoutStatus;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.HotelPayoutRepository;
import com.example.demo.repository.PayoutBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.30");

    private final BookingRepository bookingRepository;
    private final HotelPayoutRepository hotelPayoutRepository;
    private final PayoutBookingRepository payoutBookingRepository;

    @Transactional
    public void processMonthlyPayoutBatch() {
        LocalDate cutoffDate = LocalDate.now().minusDays(7);

        List<BookingRecord> eligibleBookings = bookingRepository
                .findCompletedBookingsEligibleForPayout(cutoffDate);

        if (eligibleBookings.isEmpty()) {
            log.info("No eligible bookings for payout batch at cutoff={}", cutoffDate);
            return;
        }

        Map<Long, List<BookingRecord>> bookingsByHotel = eligibleBookings.stream()
                .collect(Collectors.groupingBy(BookingRecord::getHotelId));

        for (Map.Entry<Long, List<BookingRecord>> entry : bookingsByHotel.entrySet()) {
            Long hotelId = entry.getKey();
            List<BookingRecord> hotelBookings = entry.getValue();

            List<PayoutBooking> payoutLines = new ArrayList<>();
            BigDecimal totalHotelNet = BigDecimal.ZERO;

            for (BookingRecord booking : hotelBookings) {
                if (payoutBookingRepository.existsByBookingId(booking.getId())) {
                    // extra safety for idempotency if concurrent batch/rerun.
                    continue;
                }

                BigDecimal bookingAmount = nvl(booking.getTotalAmount());
                BigDecimal commissionAmount = bookingAmount
                        .multiply(COMMISSION_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal hotelNetAmount = bookingAmount
                        .subtract(commissionAmount)
                        .setScale(2, RoundingMode.HALF_UP);

                PayoutBooking line = PayoutBooking.builder()
                        .bookingId(booking.getId())
                        .bookingAmount(bookingAmount)
                        .commissionAmount(commissionAmount)
                        .hotelNetAmount(hotelNetAmount)
                        .build();
                payoutLines.add(line);

                totalHotelNet = totalHotelNet.add(hotelNetAmount);
            }

            if (payoutLines.isEmpty()) {
                continue;
            }

            HotelPayout payout = HotelPayout.builder()
                    .hotelId(hotelId)
                    .totalAmount(totalHotelNet)
                    .status(PayoutStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();
            payout = hotelPayoutRepository.save(payout);

            final Long payoutId = payout.getId();
            payoutLines.forEach(line -> line.setPayoutId(payoutId));
            payoutBookingRepository.saveAll(payoutLines);

            // Simulate bank transfer.
            payout.setStatus(PayoutStatus.PAID);
            payout.setPaidAt(Instant.now());
            hotelPayoutRepository.save(payout);

            log.info("Payout completed. payoutId={}, hotelId={}, bookingCount={}, totalNet={}",
                    payout.getId(), hotelId, payoutLines.size(), totalHotelNet);
        }
    }

    private BigDecimal nvl(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
