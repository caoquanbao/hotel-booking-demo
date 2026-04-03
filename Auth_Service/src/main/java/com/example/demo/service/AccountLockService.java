package com.example.demo.service;

import com.example.demo.exception.AccountLockedException;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AccountLockService {

    private final UserRepository userRepository;

    private static final int MAX_FAILED = 5;
    private static final int LOCK_MINUTES = 15;

    public void assertNotLocked(User user) {
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(Instant.now())) {
            throw new AccountLockedException();
        }
    }

    public void onLoginSuccess(User user) {
        // reset fail counter
        if (user.getFailedLoginCount() != 0 || user.getLockUntil() != null) {
            user.setFailedLoginCount(0);
            user.setLockUntil(null);
            userRepository.save(user);
        }
    }

    public void onLoginFailure(User user) {
        int failed = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(failed);

        if (failed >= MAX_FAILED) {
            user.setLockUntil(Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES));
            // optional: reset counter sau khi lock để lần sau đếm lại từ 0
            user.setFailedLoginCount(0);
        }

        userRepository.save(user);
    }
}
