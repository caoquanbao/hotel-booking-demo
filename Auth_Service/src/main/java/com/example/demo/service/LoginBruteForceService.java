package com.example.demo.service;

import com.example.demo.exception.TooManyLoginAttemptsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginBruteForceService {

    // window-based counter: giữ timestamps trong 1 cửa sổ thời gian
    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    // tuỳ chỉnh policy
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final int MAX_PER_IP = 30;       // 30 attempts / 5 min / IP
    private static final int MAX_PER_EMAIL = 10;    // 10 attempts / 5 min / email

    public void checkAllowed(String ip, String email) {
        if (!allow("IP_" + ip, MAX_PER_IP)) {
            throw new TooManyLoginAttemptsException("Có quá nhiều lần đăng nhập từ IP này. Vui lòng thử lại sau.");
        }
        if (email != null && !allow("EMAIL_" + email.toLowerCase(), MAX_PER_EMAIL)) {
            throw new TooManyLoginAttemptsException("Có quá nhiều lần đăng nhập cho email này. Vui lòng thử lại sau.");
        }
    }

    public void onFailed(String ip, String email) {
        record("IP_" + ip);
        if (email != null) record("EMAIL_" + email.toLowerCase());
    }

    public void onSuccess(String ip, String email) {
        // optional: reset bucket để user đăng nhập xong “sạch”
        clear("IP_" + ip);
        if (email != null) clear("EMAIL_" + email.toLowerCase());
    }

    private boolean allow(String key, int max) {
        Deque<Instant> q = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        Instant now = Instant.now();

        // cleanup
        while (!q.isEmpty() && q.peekFirst().isBefore(now.minus(WINDOW))) {
            q.pollFirst();
        }
        return q.size() < max;
    }

    private void record(String key) {
        Deque<Instant> q = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        Instant now = Instant.now();

        while (!q.isEmpty() && q.peekFirst().isBefore(now.minus(WINDOW))) {
            q.pollFirst();
        }
        q.addLast(now);
    }

    private void clear(String key) {
        attempts.remove(key);
    }
}
