package com.swetlox_app.swetlox.util;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class StompEventListener {

    private final ConcurrentHashMap<String, String> activeUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        activeUsers.put(sessionId, "Connected");
        System.out.println("User connected: " + sessionId);
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        activeUsers.remove(sessionId);
        System.out.println("User disconnected: " + sessionId);
    }

    public int getActiveUserCount() {
        return activeUsers.size();
    }
}
