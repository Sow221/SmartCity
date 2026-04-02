package com.smartcity.service;

import com.smartcity.model.Signalement;
import java.util.List;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * Stub Service for Agent real-time
 */
public class AgentWebSocketService {
    // Nouvelle version : push réel via WebSocket
    public void pushMissionToAgents(String missionJson) {
        com.smartcity.websocket.AgentMissionEndpoint.pushMission(missionJson);
    }
}