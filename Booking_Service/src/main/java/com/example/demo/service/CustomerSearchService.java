package com.example.demo.service;

import com.example.demo.dto.CustomerHotelSearchRequest;
import com.example.demo.dto.CustomerHotelSearchResponse;
import com.example.demo.dto.HotelCandidate;
import com.example.demo.dto.RankedHotel;
import com.example.demo.dto.SearchContext;
import com.example.demo.entity.Hotel;
import com.example.demo.entity.HotelRatingSummary;
import com.example.demo.entity.RoomType;
import com.example.demo.entity.Booking;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.HotelRatingSummaryRepository;
import com.example.demo.repository.HotelRepository;
import com.example.demo.repository.RoomInventoryRepository;
import com.example.demo.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerSearchService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final HotelRatingSummaryRepository hotelRatingSummaryRepository;
    private final BookingRepository bookingRepository;
    private final RankingService rankingService;

    public List<CustomerHotelSearchResponse> search(CustomerHotelSearchRequest req) {
        if (req.getCheckIn() == null || req.getCheckOut() == null) {
            throw new IllegalArgumentException("checkIn/checkOut required");
        }
        if (!req.getCheckOut().isAfter(req.getCheckIn())) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        int roomsRequested = (req.getRooms() == null || req.getRooms() <= 0) ? 1 : req.getRooms();

        List<Hotel> hotels = (req.getLocation() == null || req.getLocation().isBlank())
                ? hotelRepository.findAll()
                : hotelRepository.findByLocationContainingIgnoreCase(req.getLocation());

        long nights = ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());

        List<CustomerHotelSearchResponse> out = new ArrayList<>();
        Map<String, Double> scoreByCandidateKey = new HashMap<>();
        for (Hotel h : hotels) {
            List<RoomType> roomTypes = roomTypeRepository.findByHotelId(h.getId());
            for (RoomType rt : roomTypes) {
                Integer minAvail = roomInventoryRepository.minAvailableInRange(rt.getId(), req.getCheckIn(), req.getCheckOut());
                boolean available = minAvail != null && minAvail >= roomsRequested;

                BigDecimal estimated = null;
                if (rt.getBasePricePerNight() != null) {
                    estimated = rt.getBasePricePerNight()
                            .multiply(BigDecimal.valueOf(nights))
                            .multiply(BigDecimal.valueOf(roomsRequested));
                }

                double trustScore = hotelRatingSummaryRepository.findById(h.getId())
                        .map(HotelRatingSummary::getRating)
                        .orElse(0d);
                int bookingCount = Math.toIntExact(
                        bookingRepository.countByHotelIdAndStatusIn(h.getId(), Booking.successfulStatuses())
                );
                double distanceKm = 0d; // no geolocation in current hotel model, keep neutral.
                double priceValue = estimated == null ? 0d : estimated.doubleValue();

                HotelCandidate candidate = HotelCandidate.builder()
                        .hotelId(h.getId())
                        .trustScore(trustScore)
                        .distanceKm(distanceKm)
                        .price(priceValue)
                        .bookingCount(bookingCount)
                        .build();

                SearchContext searchContext = SearchContext.builder()
                        .userBudget(req.getUserBudget() == null ? 0d : req.getUserBudget())
                        .userSearchLat(req.getUserSearchLat() == null ? 0d : req.getUserSearchLat())
                        .userSearchLng(req.getUserSearchLng() == null ? 0d : req.getUserSearchLng())
                        .build();
                List<RankedHotel> ranked = rankingService.rankHotels(List.of(candidate), searchContext);
                double organicScore = ranked.isEmpty() ? 0d : ranked.get(0).getScore();
                scoreByCandidateKey.put(candidateKey(h.getId(), rt.getId()), organicScore);

                out.add(CustomerHotelSearchResponse.builder()
                        .hotelId(h.getId())
                        .hotelName(h.getName())
                        .location(h.getLocation())
                        .roomTypeId(rt.getId())
                        .roomTypeName(rt.getName())
                        .roomsRequested(roomsRequested)
                        .available(available)
                        .estimatedTotal(estimated)
                        .checkIn(req.getCheckIn())
                        .checkOut(req.getCheckOut())
                        .build());
            }
        }
        out.sort(Comparator.comparingDouble((CustomerHotelSearchResponse o) ->
                scoreByCandidateKey.getOrDefault(candidateKey(o.getHotelId(), o.getRoomTypeId()), 0d)
        ).reversed());
        return out;
    }

    private String candidateKey(Long hotelId, Long roomTypeId) {
        return hotelId + "-" + roomTypeId;
    }
}
