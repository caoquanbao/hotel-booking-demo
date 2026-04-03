package com.example.demo.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("JWT filter hit: {}", request.getRequestURI());

        String authorizationHeader = request.getHeader("Authorization");
        log.info("Authorization header = {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.info("No Bearer token found for {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        log.info("Token extracted = {}", accessToken);

        try {
            if (accessToken.isEmpty()) {
                log.warn("Bearer header is present but token is empty");
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.info("SecurityContext already contains authentication for {}",
                        SecurityContextHolder.getContext().getAuthentication().getName());
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtProvider.extractEmail(accessToken);
            log.info("Username from token = {}", email);

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            log.info("Loaded user = {}", userDetails.getUsername());

            boolean valid = jwtProvider.isValidAccessToken(accessToken, userDetails);
            log.info("Token valid = {}", valid);

            if (valid && userDetails.isAccountNonLocked()) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Authentication set for {}", userDetails.getUsername());
            } else if (!userDetails.isAccountNonLocked()) {
                log.warn("User is locked: {}", userDetails.getUsername());
            } else {
                log.warn("Token validation returned false for {}", email);
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            log.warn("JWT authentication failed for path {}: {}", request.getRequestURI(), ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
