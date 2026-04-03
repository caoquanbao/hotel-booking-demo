package com.example.demo.repository;

import com.example.demo.entity.HotelRatingSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRatingSummaryRepository extends JpaRepository<HotelRatingSummary, Long> {
}
