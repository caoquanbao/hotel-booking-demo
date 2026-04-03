package com.example.demo.controller;

import com.example.demo.dto.ApiSuccessResponse;
import com.example.demo.dto.CancelBookingResponse;
import com.example.demo.dto.CreateBookingRequest;
import com.example.demo.dto.CreateBookingResponse;
import com.example.demo.dto.CustomerBookingSummary;
import com.example.demo.service.BookingService;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.CustomerBookingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/bookings")
public class CustomerBookingController {

    private final BookingService bookingService;
    private final CustomerBookingQueryService bookingQueryService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<CreateBookingResponse> create(@RequestBody CreateBookingRequest req,
                                                        @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        var result = bookingService.createBooking(req, currentUserService.getCurrentUserId(), idemKey);

        String paymentUrl = result.paymentSession() == null ? null : result.paymentSession().paymentUrl();

        return ResponseEntity.ok(
                CreateBookingResponse.builder()
                        .bookingId(result.booking().getId())
                        .status(result.booking().getStatus().name())
                        .totalAmount(result.booking().getTotalAmount())
                        .basePrice(toLong(result.booking().getBasePrice()))
                        .promotionDiscount(toLong(result.booking().getPromotionDiscount()))
                        .tierDiscount(toLong(result.booking().getTierDiscount()))
                        .finalPrice(toLong(result.booking().getFinalPrice()))
                        .commission(toLong(result.booking().getCommissionAmount()))
                        .hotelPayout(toLong(result.booking().getHotelPayout()))
                        .paymentUrl(paymentUrl)
                        .build()
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiSuccessResponse<CancelBookingResponse>> cancel(@PathVariable("id") Long bookingId) {
        var result = bookingService.cancelBooking(bookingId, currentUserService.getCurrentUserId());
        return ResponseEntity.ok(new ApiSuccessResponse<>(
                true,
                "Booking cancelled successfully",
                new CancelBookingResponse(result.bookingId(), result.status().name(), result.inventoryRestored())
        ));
    }

    @GetMapping
    public ResponseEntity<List<CustomerBookingSummary>> listMine() {
        return ResponseEntity.ok(bookingQueryService.listMyBookings(currentUserService.getCurrentUserId()));
    }

    private Long toLong(java.math.BigDecimal value) {
        return value == null ? null : value.longValue();
    }
}
