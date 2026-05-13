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
        if (accessor.getSessionAttributes() == null) return;
        Long userProfileId = (Long) accessor.getSessionAttributes().get("userProfileId");
        if (userProfileId != null) {
            sessionRegistry.register(userProfileId, accessor.getSessionId());
            log.info("WebSocket connected - userProfileId: {}", userProfileId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getSessionAttributes() == null) return;
        Long userProfileId = (Long) accessor.getSessionAttributes().get("userProfileId");
        if (userProfileId != null) {
            sessionRegistry.unregister(userProfileId);
            log.info("WebSocket disconnected - userProfileId: {}", userProfileId);
        }
    }
}