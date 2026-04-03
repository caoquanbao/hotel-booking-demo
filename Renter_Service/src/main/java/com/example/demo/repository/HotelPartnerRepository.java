package com.example.demo.repository;

import com.example.demo.entity.HotelPartner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelPartnerRepository extends JpaRepository<HotelPartner, Long> {
    boolean existsByHotelId(String hotelId);
}
