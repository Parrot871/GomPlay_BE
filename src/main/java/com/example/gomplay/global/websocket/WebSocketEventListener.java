package com.example.gomplay.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketSessionRegistry sessionRegistry;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Long userProfileId = null;

        // 1. sessionAttributes에서 꺼내기
        if (accessor.getSessionAttributes() != null) {
            Object val = accessor.getSessionAttributes().get("userProfileId");
            if (val instanceof Long) {
                userProfileId = (Long) val;
            }
        }

        // 2. 못 찾으면 Principal에서 꺼내기 (CustomHandshakeHandler에서 등록한 값)
        if (userProfileId == null && accessor.getUser() != null) {
            try {
                userProfileId = Long.parseLong(accessor.getUser().getName());
            } catch (NumberFormatException ignored) {}
        }

        if (userProfileId != null) {
            sessionRegistry.register(userProfileId, accessor.getSessionId());
            log.info("WebSocket connected - userProfileId: {}", userProfileId);
        } else {
            log.warn("WebSocket connected - userProfileId 없음 (인증 실패 가능성)");
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Long userProfileId = null;

        // 1. sessionAttributes에서 꺼내기
        if (accessor.getSessionAttributes() != null) {
            Object val = accessor.getSessionAttributes().get("userProfileId");
            if (val instanceof Long) {
                userProfileId = (Long) val;
            }
        }

        // 2. 못 찾으면 Principal에서 꺼내기
        if (userProfileId == null && accessor.getUser() != null) {
            try {
                userProfileId = Long.parseLong(accessor.getUser().getName());
            } catch (NumberFormatException ignored) {}
        }

        if (userProfileId != null) {
            sessionRegistry.unregister(userProfileId);
            log.info("WebSocket disconnected - userProfileId: {}", userProfileId);
        }
    }
}