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
    private final List<Consumer<List<Signalement>>> listeners = Collections.synchronizedList(new ArrayList<>());
    private ScheduledExecutorService scheduler;

    public void connect() {
        System.out.println("🔔 WebSocket Agent connecté (demo mode)");
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::simulateMissionUpdate, 5, 30, TimeUnit.SECONDS);
    }

    public void disconnect() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addListener(Consumer<List<Signalement>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<List<Signalement>> listener) {
        listeners.remove(listener);
    }

    public void simulateMissionUpdate() {
        List<Signalement> update = new ArrayList<>();
        Signalement urgent = new Signalement();
        urgent.setIdSignalement(999);
        urgent.setStatut("URGENT");
        urgent.setZoneNom("Guediawaye Centre");
        update.add(urgent);

        List<Consumer<List<Signalement>>> copy;
        synchronized(listeners) {
            copy = new ArrayList<>(listeners);
        }
        for (Consumer<List<Signalement>> l : copy) {
            l.accept(update);
        }
    }
}