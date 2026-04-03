package com.example.demo.repository;

import com.example.demo.entity.HotelRoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRoomTypeRepository extends JpaRepository<HotelRoomType, Long> {
    boolean existsByHotelIdAndRoomTypeId(String hotelId, String roomTypeId);
}
