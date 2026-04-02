package com.smartcity.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Endpoint WebSocket pour le push temps réel des missions aux agents
 */
@ServerEndpoint("/ws/agent-missions")
public class AgentMissionEndpoint {
    // Méthode utilitaire pour push une mise à jour historique/profil à tous les
    // agents connectés
    public static void pushHistoriqueProfil(String type) {
        String msg = String.format("{\"type\":\"%s\"}", type);
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(msg);
                } catch (IOException e) {
                    // Ignorer
                }
            }
        }
    }

    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("[WebSocket] Agent connecté: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("[WebSocket] Agent déconnecté: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Traiter les messages reçus des agents (missions ou GPS)
        System.out.println("[WebSocket] Message reçu de " + session.getId() + ": " + message);
        if (message != null && message.contains("gps")) {
            // Diffuser la position GPS à tous les clients connectés
            for (Session s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        // Ignorer
                    }
                }
            }
        }
    }

    // Méthode utilitaire pour push une mission à tous les agents connectés
    public static void pushMission(String missionJson) {
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(missionJson);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
