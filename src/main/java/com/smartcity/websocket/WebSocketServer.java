package com.smartcity.websocket;

import org.glassfish.tyrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serveur WebSocket embarqué (Tyrus) pour le push temps réel vers les agents.
 * Port : 8082
 * URL  : ws://localhost:8082/ws/agent-missions
 */
public class WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    public static final int PORT = 8082;

    private Server server;

    public void start() {
        server = new Server("localhost", PORT, "/ws", null, AgentMissionEndpoint.class);
        try {
            server.start();
            logger.info("WebSocket server démarré sur ws://localhost:{}/ws/agent-missions", PORT);
        } catch (Exception e) {
            logger.warn("Impossible de démarrer le WebSocket server (port {} occupé ?): {}", PORT, e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop();
            logger.info("WebSocket server arrêté");
        }
    }
}
