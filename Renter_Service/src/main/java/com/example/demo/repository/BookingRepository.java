package com.example.demo.repository;

import com.example.demo.entity.BookingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingRecord, Long> {

    @Query("""
            select b from BookingRecord b
            where b.status = 'COMPLETED'
              and b.checkOut <= :cutoffDate
              and not exists (
                select 1 from PayoutBooking pb where pb.bookingId = b.id
              )
            """)
    List<BookingRecord> findCompletedBookingsEligibleForPayout(@Param("cutoffDate") LocalDate cutoffDate);
}
