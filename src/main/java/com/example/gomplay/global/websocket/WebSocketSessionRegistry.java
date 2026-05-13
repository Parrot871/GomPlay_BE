package com.example.gomplay.global.websocket;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<Long, String> profileSessionMap = new ConcurrentHashMap<>();
    private final Set<Long> waitingPool = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void register(Long userProfileId, String sessionId) {
        profileSessionMap.put(userProfileId, sessionId);
    }

    public void unregister(Long userProfileId) {
        profileSessionMap.remove(userProfileId);
        waitingPool.remove(userProfileId);
    }

    public void addToWaiting(Long userProfileId) {
        waitingPool.add(userProfileId);
    }

    public void removeFromWaiting(Long userProfileId) {
        waitingPool.remove(userProfileId);
    }

    public Set<Long> getWaitingPool() {
        return Collections.unmodifiableSet(waitingPool);
    }

    public boolean isConnected(Long userProfileId) {
        return profileSessionMap.containsKey(userProfileId);
    }
}