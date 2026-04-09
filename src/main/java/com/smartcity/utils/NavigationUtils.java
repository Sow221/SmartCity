package com.smartcity.utils;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.util.Duration;

/**
 * Utilitaire de navigation partagé entre les dashboards.
 * Centralise la logique showPage dupliquée dans Admin/Agent/Citizen.
 */
public class NavigationUtils {

    private NavigationUtils() {}

    /**
     * Affiche une page, masque les autres, met à jour le bouton actif.
     * Joue une FadeTransition légère sur la page affichée.
     */
    public static void showPage(Node pageToShow, Node[] allPages,
                                Button activeButton, Button[] allButtons,
                                String activeStyleClass) {
        for (Node page : allPages) {
            if (page == null) continue;
            boolean visible = page == pageToShow;
            page.setVisible(visible);
            page.setManaged(visible);
        }
        if (pageToShow != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(180), pageToShow);
            fade.setFromValue(0.2);
            fade.setToValue(1.0);
            fade.play();
        }
        for (Button btn : allButtons) {
            if (btn == null) continue;
            btn.getStyleClass().remove(activeStyleClass);
            if (btn == activeButton) btn.getStyleClass().add(activeStyleClass);
        }
    }
}
