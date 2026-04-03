package com.example.demo.service;

import com.example.demo.config.RankingWeights;
import com.example.demo.dto.HotelCandidate;
import com.example.demo.dto.RankedHotel;
import com.example.demo.dto.SearchContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingServiceImplTest {

    @Test
    void rankHotels_shouldSortByScoreDesc() {
        RankingWeights weights = new RankingWeights();
        RankingServiceImpl service = new RankingServiceImpl(weights);

        SearchContext context = SearchContext.builder()
                .userBudget(100)
                .userSearchLat(21.0)
                .userSearchLng(105.0)
                .build();

        List<HotelCandidate> candidates = List.of(
                HotelCandidate.builder().hotelId(1L).trustScore(4.5).bookingCount(100).distanceKm(2).price(100).build(),
                HotelCandidate.builder().hotelId(2L).trustScore(2.5).bookingCount(10).distanceKm(8).price(150).build(),
                HotelCandidate.builder().hotelId(3L).trustScore(0).bookingCount(5).distanceKm(1).price(90).build()
        );

        List<RankedHotel> ranked = service.rankHotels(candidates, context);

        assertEquals(3, ranked.size());
        assertTrue(ranked.get(0).getScore() >= ranked.get(1).getScore());
        assertTrue(ranked.get(1).getScore() >= ranked.get(2).getScore());
    }

    @Test
    void normalizeTrustScore_shouldHandleColdStart() {
        RankingWeights weights = new RankingWeights();
        RankingServiceImpl service = new RankingServiceImpl(weights);

        double coldStart = service.normalizeTrustScore(0);
        double highTrust = service.normalizeTrustScore(5);

        assertEquals(0.5, coldStart, 0.0001);
        assertEquals(1.0, highTrust, 0.0001);
    }
}
