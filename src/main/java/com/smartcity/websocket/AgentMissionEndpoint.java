package com.smartcity.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint WebSocket pour le push temps réel des missions aux agents.
 */
@ServerEndpoint("/ws/agent-missions")
public class AgentMissionEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(AgentMissionEndpoint.class);
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        logger.info("[WebSocket] Agent connecté: {}", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        logger.info("[WebSocket] Agent déconnecté: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.debug("[WebSocket] Message reçu de {}", session.getId());
        if (message != null && message.contains("gps")) {
            broadcast(message);
        }
    }

    public static void pushMission(String missionJson) {
        broadcast(missionJson);
    }

    public static void pushHistoriqueProfil(String type) {
        broadcast(String.format("{\"type\":\"%s\"}", type));
    }

    /**
     * Broadcast position realtime update pour tous les agents connectés.
     * Appelé depuis GpsApiServer lors de chaque mise à jour GPS.
     */
    public static void broadcastPosition(int agentId, double lat, double lon) {
        String positionJson = String.format(
            "{\"type\":\"position\",\"agentId\":%d,\"lat\":%f,\"lon\":%f,\"timestamp\":%d}",
            agentId, lat, lon, System.currentTimeMillis()
        );
        broadcast(positionJson);
    }

    private static void broadcast(String message) {
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.warn("Erreur WebSocket broadcast: {}", e.getMessage());
                }
            }
        }
    }
}
