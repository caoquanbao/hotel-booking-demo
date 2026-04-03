package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.OtpResendRequest;
import com.example.demo.dto.OtpVerifyRequest;
import com.example.demo.dto.RefreshRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
                                   HttpServletRequest http) {

        String ip = http.getRemoteAddr();
        String userAgent = http.getHeader("User-Agent");

        AuthResponse response = authService.login(req, ip, userAgent);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request,
                                       HttpServletRequest http) {

        AuthResponse response = authService.verifyOtp(
                request,
                http.getRemoteAddr(),
                http.getHeader("User-Agent")
        );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // RESEND OTP
    // =========================================================

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody OtpResendRequest request,
                                       HttpServletRequest http) {

        authService.resendOtp(
                request,
                http.getRemoteAddr(),
                http.getHeader("User-Agent")
        );

        return ResponseEntity.ok(new SuccessResponse("OTP_SENT"));
    }

    // =========================================================
    // REFRESH
    // =========================================================

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {

        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam("email") String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        authService.logout(user.getId());

        return ResponseEntity.ok(new SuccessResponse("LOGOUT_SUCCESS"));
    }

    // =========================================================
    // RESPONSE WRAPPERS
    // =========================================================

    public record SuccessResponse(String message) {}
}
