package com.example.demo.repository;

import com.example.demo.entity.RoomInventoryDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RoomInventoryRepository extends JpaRepository<RoomInventoryDaily, Long> {
    Optional<RoomInventoryDaily> findByHotelIdAndRoomTypeIdAndRatePlanIdAndStayDate(
            String hotelId,
            String roomTypeId,
            String ratePlanId,
            LocalDate stayDate
    );
}
