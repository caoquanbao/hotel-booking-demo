package com.example.demo.service;

import com.example.demo.entity.ReviewUser;
import com.example.demo.repository.ReviewUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final ReviewUserRepository reviewUserRepository;

    public Long getCurrentUserId() {
        String email = getCurrentUserEmail();
        ReviewUser user = reviewUserRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authenticated user not found"));
        return user.getId();
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Missing authentication");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String principalValue
                && !principalValue.isBlank()
                && !"anonymousUser".equals(principalValue)) {
            return principalValue;
        }

        String name = authentication.getName();
        if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
            return name;
        }

        throw new AuthenticationCredentialsNotFoundException("Missing authenticated principal");
    }
}
