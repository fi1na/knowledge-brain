package com.knowledgebrain.websocket;

import com.knowledgebrain.security.JwtUtil;
import com.knowledgebrain.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = extractToken(accessor);
        if (token == null) {
            log.warn("WebSocket CONNECT without token");
            throw new IllegalArgumentException("Missing authentication token");
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("WebSocket CONNECT with invalid token");
            throw new IllegalArgumentException("Invalid authentication token");
        }

        UUID userId = jwtUtil.getUserIdFromToken(token);
        String email = jwtUtil.getEmailFromToken(token);

        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());

        accessor.setUser(auth);
        log.info("WebSocket authenticated for user {}", userId);

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // Try Authorization header first: "Bearer eyJ..."
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Fallback: token header directly
        String token = accessor.getFirstNativeHeader("token");
        if (token != null && !token.isBlank()) {
            return token;
        }

        return null;
    }
}