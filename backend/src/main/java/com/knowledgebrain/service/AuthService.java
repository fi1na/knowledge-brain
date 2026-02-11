package com.knowledgebrain.service;

import com.knowledgebrain.dto.auth.AuthResponse;
import com.knowledgebrain.dto.auth.LoginRequest;
import com.knowledgebrain.dto.auth.RegisterRequest;
import com.knowledgebrain.entity.RefreshToken;
import com.knowledgebrain.entity.User;
import com.knowledgebrain.repository.RefreshTokenRepository;
import com.knowledgebrain.repository.UserRepository;
import com.knowledgebrain.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName().trim())
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtUtil.generateRefreshTokenValue())
                .expiresAt(Instant.now().plus(jwtUtil.getRefreshTokenExpiration()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isUsable()) {
            // If someone tries to use a revoked token, revoke ALL tokens for that user (token theft detection)
            if (refreshToken.isRevoked()) {
                refreshTokenRepository.revokeAllByUserId(refreshToken.getUser().getId());
                log.warn("Reuse of revoked refresh token detected for user: {}. All tokens revoked.",
                        refreshToken.getUser().getEmail());
            }
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        // Rotate: revoke the old token, create a new one
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        log.debug("Access token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .accessToken(newAccessToken)
                .build();
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldRefreshTokenValue) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldRefreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        return createRefreshToken(oldToken.getUser().getId());
    }

    @Transactional
    public void logout(UUID userId) {
        int revoked = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Logged out user: {}, revoked {} refresh tokens", userId, revoked);
    }
}
