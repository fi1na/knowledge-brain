package com.knowledgebrain.controller;

import com.knowledgebrain.dto.auth.AuthResponse;
import com.knowledgebrain.dto.auth.LoginRequest;
import com.knowledgebrain.dto.auth.RegisterRequest;
import com.knowledgebrain.entity.RefreshToken;
import com.knowledgebrain.security.UserPrincipal;
import com.knowledgebrain.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.jwt.refresh-token-expiration}")
    private java.time.Duration refreshTokenExpiration;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request);
        RefreshToken refreshToken = authService.createRefreshToken(authResponse.getUserId());
        addRefreshTokenCookie(response, refreshToken.getToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request);
        RefreshToken refreshToken = authService.createRefreshToken(authResponse.getUserId());
        addRefreshTokenCookie(response, refreshToken.getToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshTokenValue = extractRefreshTokenFromCookie(request);
        if (refreshTokenValue == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        AuthResponse authResponse = authService.refreshAccessToken(refreshTokenValue);

        // Rotate refresh token
        RefreshToken newRefreshToken = authService.rotateRefreshToken(refreshTokenValue);
        addRefreshTokenCookie(response, newRefreshToken.getToken());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse response
    ) {
        if (principal != null) {
            authService.logout(principal.getId());
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) refreshTokenExpiration.toSeconds());
        if (!cookieDomain.isBlank()) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        if (!cookieDomain.isBlank()) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
