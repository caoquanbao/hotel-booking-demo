package com.example.demo.repository;

import com.example.demo.entity.HotelPayout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelPayoutRepository extends JpaRepository<HotelPayout, Long> {
}
