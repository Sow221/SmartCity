package com.smartcity.websocket;

import com.smartcity.config.GeoConfig;
import org.glassfish.tyrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serveur WebSocket embarqué (Tyrus) pour le push temps réel des positions agents.
 * Port : autoconfigurable via GeoConfig (3002 par défaut).
 * URL  : ws://<ip>:<port>/ws/agent-missions
 */
public class WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private Server server;

    public void start() {
        try {
            int port      = GeoConfig.getWebSocketPort();
            String ip     = GeoConfig.getLocalIp();
            server = new Server(ip, port, "/ws", null, AgentMissionEndpoint.class);
            server.start();
            logger.info("✅ WebSocket Server démarré sur ws://{}:{}/ws/agent-missions", ip, port);
        } catch (Exception e) {
            logger.warn("⚠️ Impossible de démarrer le WebSocket Server: {}", e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
                logger.info("✅ WebSocket Server arrêté");
            } catch (Exception e) {
                logger.warn("⚠️ Erreur arrêt WebSocket Server: {}", e.getMessage());
            }
        }
    }
}
