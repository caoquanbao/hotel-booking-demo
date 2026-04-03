package com.example.demo.repository;

import com.example.demo.entity.ReviewUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewUserRepository extends JpaRepository<ReviewUser, Long> {
    Optional<ReviewUser> findByEmail(String email);
}
