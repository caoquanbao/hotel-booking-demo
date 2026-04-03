package com.example.demo.service;

import com.example.demo.dto.HotelCandidate;
import com.example.demo.dto.RankedHotel;
import com.example.demo.dto.SearchContext;

import java.util.List;

public interface RankingService {
    List<RankedHotel> rankHotels(List<HotelCandidate> candidates, SearchContext context);
}
