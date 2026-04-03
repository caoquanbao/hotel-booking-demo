package com.example.demo.repository;

import com.example.demo.entity.RoomInventory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    // trả về MIN(availableRooms) trong range -> nếu min >= roomsRequested => available
    @Query("""
        select coalesce(min(ri.availableRooms), 0)
        from RoomInventory ri
        where ri.roomTypeId = :roomTypeId
          and ri.date >= :checkIn
          and ri.date < :checkOut
    """)
    Integer minAvailableInRange(@Param("roomTypeId") Long roomTypeId,
                                @Param("checkIn") LocalDate checkIn,
                                @Param("checkOut") LocalDate checkOut);

    @Modifying
    @Transactional
    @Query("""
        update RoomInventory ri
        set ri.availableRooms = ri.availableRooms - :rooms
        where ri.roomTypeId = :roomTypeId
          and ri.date >= :checkIn
          and ri.date < :checkOut
          and ri.availableRooms >= :rooms
    """)
    int decreaseInventory(@Param("roomTypeId") Long roomTypeId,
                          @Param("rooms") int rooms,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);

    @Modifying
    @Transactional
    @Query("""
        update RoomInventory ri
        set ri.availableRooms = ri.availableRooms + :rooms
        where ri.roomTypeId = :roomTypeId
          and ri.date >= :checkIn
          and ri.date < :checkOut
    """)
    int increaseInventory(@Param("roomTypeId") Long roomTypeId,
                          @Param("rooms") int rooms,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);
}
