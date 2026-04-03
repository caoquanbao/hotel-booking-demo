package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    long countByUserIdAndCreatedAtAfter(Long userId, Instant createdAt);

    long countByIpAddressAndCreatedAtAfter(String ipAddress, Instant createdAt);

    @Query("""
            select r.reviewText from Review r
            where r.userId = :userId and r.hotelId = :hotelId
            order by r.createdAt desc
            """)
    List<String> findRecentReviewTexts(@Param("userId") Long userId,
                                       @Param("hotelId") Long hotelId,
                                       org.springframework.data.domain.Pageable pageable);
}
