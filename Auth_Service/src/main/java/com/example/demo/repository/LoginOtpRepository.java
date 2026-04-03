package com.example.demo.repository;

import com.example.demo.entity.LoginOtp;
import com.example.demo.entity.OtpPurpose;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginOtpRepository extends JpaRepository<LoginOtp, Long> {

    long deleteByUser_Id(Long userId);

    @Query("""
        select o from LoginOtp o
        where o.user.id = :userId
          and o.purpose = :purpose
          and o.usedAt is null
          and o.expiresAt > :now
        order by o.createdAt desc
    """)
    List<LoginOtp> findActiveOtps(@Param("userId") Long userId,
                                 @Param("purpose") OtpPurpose purpose,
                                 @Param("now") Instant now);

    default Optional<LoginOtp> findLatestActive(Long userId, OtpPurpose purpose) {
        List<LoginOtp> list = findActiveOtps(userId, purpose, Instant.now());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
