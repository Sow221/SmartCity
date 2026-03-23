package com.smartcity.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Utilitaire d'animations pour SmartCity.
 * Fournit des animations réutilisables pour améliorer l'expérience utilisateur.
 * 
 * @author SmartCity Team
 * @version 1.0
 */
public class AnimationUtils {

    // =========================================================================
    // DURÉES STANDARD
    // =========================================================================
    
    /** Durée très rapide (100ms) - micro-interactions */
    public static final Duration DURATION_FAST = Duration.millis(100);
    
    /** Durée normale (200ms) - transitions standard */
    public static final Duration DURATION_NORMAL = Duration.millis(200);
    
    /** Durée moyenne (300ms) - animations visibles */
    public static final Duration DURATION_MEDIUM = Duration.millis(300);
    
    /** Durée lente (500ms) - animations importantes */
    public static final Duration DURATION_SLOW = Duration.millis(500);
    
    /** Durée très lente (800ms) - animations dramatiques */
    public static final Duration DURATION_SLOWER = Duration.millis(800);

    // =========================================================================
    // ANIMATIONS DE FADE (OPACITÉ)
    // =========================================================================

    /**
     * Fait apparaître un élément en fondu.
     * 
     * @param node L'élément à animer
     * @param duration La durée de l'animation
     * @return La transition créée
     */
    public static FadeTransition fadeIn(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_OUT);
        return fade;
    }

    /**
     * Fait apparaître un élément en fondu (durée normale).
     */
    public static FadeTransition fadeIn(Node node) {
        return fadeIn(node, DURATION_NORMAL);
    }

    /**
     * Fait disparaître un élément en fondu.
     * 
     * @param node L'élément à animer
     * @param duration La durée de l'animation
     * @return La transition créée
     */
    public static FadeTransition fadeOut(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setInterpolator(Interpolator.EASE_IN);
        return fade;
    }

    /**
     * Fait disparaître un élément en fondu (durée normale).
     */
    public static FadeTransition fadeOut(Node node) {
        return fadeOut(node, DURATION_NORMAL);
    }

    // =========================================================================
    // ANIMATIONS DE SLIDE (TRANSLATION)
    // =========================================================================

    /**
     * Fait glisser un élément depuis la gauche.
     */
    public static TranslateTransition slideInFromLeft(Node node, Duration duration) {
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(-50);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        return slide;
    }

    public static TranslateTransition slideInFromLeft(Node node) {
        return slideInFromLeft(node, DURATION_MEDIUM);
    }

    /**
     * Fait glisser un élément depuis la droite.
     */
    public static TranslateTransition slideInFromRight(Node node, Duration duration) {
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(50);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        return slide;
    }

    public static TranslateTransition slideInFromRight(Node node) {
        return slideInFromRight(node, DURATION_MEDIUM);
    }

    /**
     * Fait glisser un élément depuis le haut.
     */
    public static TranslateTransition slideInFromTop(Node node, Duration duration) {
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromY(-30);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        return slide;
    }

    public static TranslateTransition slideInFromTop(Node node) {
        return slideInFromTop(node, DURATION_MEDIUM);
    }

    /**
     * Fait glisser un élément depuis le bas.
     */
    public static TranslateTransition slideInFromBottom(Node node, Duration duration) {
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        return slide;
    }

    public static TranslateTransition slideInFromBottom(Node node) {
        return slideInFromBottom(node, DURATION_MEDIUM);
    }

    // =========================================================================
    // ANIMATIONS DE SCALE (ÉCHELLE)
    // =========================================================================

    /**
     * Fait apparaître un élément avec un effet de zoom.
     */
    public static ScaleTransition scaleIn(Node node, Duration duration) {
        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        return scale;
    }

    public static ScaleTransition scaleIn(Node node) {
        return scaleIn(node, DURATION_MEDIUM);
    }

    /**
     * Fait disparaître un élément avec un effet de zoom.
     */
    public static ScaleTransition scaleOut(Node node, Duration duration) {
        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.8);
        scale.setToY(0.8);
        scale.setInterpolator(Interpolator.EASE_IN);
        return scale;
    }

    public static ScaleTransition scaleOut(Node node) {
        return scaleOut(node, DURATION_MEDIUM);
    }

    /**
     * Effet de "pop" - zoom rapide puis retour.
     */
    public static ScaleTransition pop(Node node) {
        ScaleTransition scale = new ScaleTransition(DURATION_FAST, node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.15);
        scale.setToY(1.15);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        return scale;
    }

    /**
     * Effet de pulsation continue.
     */
    public static ScaleTransition pulse(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(Animation.INDEFINITE);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        return scale;
    }

    // =========================================================================
    // ANIMATIONS COMBINÉES
    // =========================================================================

    /**
     * Fade + Slide depuis le bas (entrée de page).
     */
    public static ParallelTransition fadeSlideIn(Node node) {
        node.setOpacity(0);
        
        FadeTransition fade = fadeIn(node, DURATION_MEDIUM);
        TranslateTransition slide = slideInFromBottom(node, DURATION_MEDIUM);
        
        return new ParallelTransition(node, fade, slide);
    }

    /**
     * Fade + Slide vers le haut (sortie de page).
     */
    public static ParallelTransition fadeSlideOut(Node node) {
        FadeTransition fade = fadeOut(node, DURATION_MEDIUM);
        
        TranslateTransition slide = new TranslateTransition(DURATION_MEDIUM, node);
        slide.setFromY(0);
        slide.setToY(-20);
        slide.setInterpolator(Interpolator.EASE_IN);
        
        return new ParallelTransition(node, fade, slide);
    }

    /**
     * Fade + Scale (apparition modale).
     */
    public static ParallelTransition fadeScaleIn(Node node) {
        node.setOpacity(0);
        node.setScaleX(0.9);
        node.setScaleY(0.9);
        
        FadeTransition fade = fadeIn(node, DURATION_MEDIUM);
        ScaleTransition scale = scaleIn(node, DURATION_MEDIUM);
        
        return new ParallelTransition(node, fade, scale);
    }

    /**
     * Fade + Scale (disparition modale).
     */
    public static ParallelTransition fadeScaleOut(Node node) {
        FadeTransition fade = fadeOut(node, DURATION_MEDIUM);
        ScaleTransition scale = scaleOut(node, DURATION_MEDIUM);
        
        return new ParallelTransition(node, fade, scale);
    }

    // =========================================================================
    // ANIMATIONS DE SHAKE (ERREUR)
    // =========================================================================

    /**
     * Effet de secousse horizontale (pour erreurs de validation).
     */
    public static TranslateTransition shake(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setInterpolator(Interpolator.LINEAR);
        
        shake.setOnFinished(e -> node.setTranslateX(0));
        
        return shake;
    }

    /**
     * Effet de secousse avec changement de couleur de bordure.
     */
    public static void shakeWithError(Node node) {
        // Ajouter la classe d'erreur
        if (!node.getStyleClass().contains("input-error")) {
            node.getStyleClass().add("input-error");
        }
        
        // Jouer l'animation
        TranslateTransition shake = shake(node);
        shake.setOnFinished(e -> {
            node.setTranslateX(0);
            // Retirer la classe après 2 secondes
            new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                node.getStyleClass().remove("input-error");
            })).play();
        });
        shake.play();
    }

    // =========================================================================
    // ANIMATIONS DE ROTATION
    // =========================================================================

    /**
     * Rotation continue (pour icônes de chargement).
     */
    public static RotateTransition spin(Node node) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(1), node);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        return rotate;
    }

    /**
     * Rotation unique (pour bouton refresh).
     */
    public static RotateTransition rotate360(Node node) {
        RotateTransition rotate = new RotateTransition(DURATION_SLOW, node);
        rotate.setByAngle(360);
        rotate.setInterpolator(Interpolator.EASE_BOTH);
        return rotate;
    }

    // =========================================================================
    // ANIMATIONS DE HIGHLIGHT
    // =========================================================================

    /**
     * Effet de surbrillance temporaire.
     */
    public static Timeline highlight(Node node, Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(0);
        
        node.setEffect(glow);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 0)),
            new KeyFrame(DURATION_MEDIUM, new KeyValue(glow.radiusProperty(), 20)),
            new KeyFrame(DURATION_SLOW, new KeyValue(glow.radiusProperty(), 0))
        );
        
        timeline.setOnFinished(e -> node.setEffect(null));
        
        return timeline;
    }

    /**
     * Highlight vert (succès).
     */
    public static Timeline highlightSuccess(Node node) {
        return highlight(node, Color.web("#43A047"));
    }

    /**
     * Highlight rouge (erreur).
     */
    public static Timeline highlightError(Node node) {
        return highlight(node, Color.web("#EF5350"));
    }

    /**
     * Highlight bleu (info).
     */
    public static Timeline highlightInfo(Node node) {
        return highlight(node, Color.web("#1E88E5"));
    }

    // =========================================================================
    // ANIMATIONS DE COMPTEUR
    // =========================================================================

    /**
     * Animation de compteur numérique (pour les cards de statistiques).
     * 
     * @param label Le label à animer
     * @param startValue Valeur de départ
     * @param endValue Valeur finale
     * @param duration Durée de l'animation
     */
    public static Timeline animateCounter(Label label, int startValue, int endValue, Duration duration) {
        Timeline timeline = new Timeline();
        
        int steps = 30;
        double stepDuration = duration.toMillis() / steps;
        double increment = (endValue - startValue) / (double) steps;
        
        for (int i = 0; i <= steps; i++) {
            final int value = (int) (startValue + (increment * i));
            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(stepDuration * i),
                e -> label.setText(String.valueOf(value))
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        // S'assurer que la valeur finale est exacte
        timeline.getKeyFrames().add(new KeyFrame(
            duration,
            e -> label.setText(String.valueOf(endValue))
        ));
        
        return timeline;
    }

    /**
     * Animation de compteur avec durée par défaut.
     */
    public static Timeline animateCounter(Label label, int startValue, int endValue) {
        return animateCounter(label, startValue, endValue, DURATION_SLOW);
    }

    // =========================================================================
    // ANIMATIONS DE TRANSITION DE PAGE
    // =========================================================================

    /**
     * Transition de page : sortie de l'ancienne, entrée de la nouvelle.
     * 
     * @param oldPage L'ancienne page
     * @param newPage La nouvelle page
     * @param onComplete Action à exécuter après la transition
     */
    public static void pageTransition(Node oldPage, Node newPage, Runnable onComplete) {
        // Préparer la nouvelle page
        newPage.setOpacity(0);
        newPage.setVisible(true);
        newPage.setManaged(true);
        
        // Animation de sortie
        ParallelTransition exitAnim = fadeSlideOut(oldPage);
        
        // Animation d'entrée
        ParallelTransition enterAnim = fadeSlideIn(newPage);
        
        // Séquence
        SequentialTransition sequence = new SequentialTransition(exitAnim, enterAnim);
        
        sequence.setOnFinished(e -> {
            oldPage.setVisible(false);
            oldPage.setManaged(false);
            oldPage.setOpacity(1);
            oldPage.setTranslateY(0);
            
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        sequence.play();
    }

    /**
     * Transition de page simple (sans callback).
     */
    public static void pageTransition(Node oldPage, Node newPage) {
        pageTransition(oldPage, newPage, null);
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    /**
     * Joue une animation et exécute une action à la fin.
     */
    public static void playAndThen(Animation animation, Runnable onComplete) {
        animation.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        animation.play();
    }

    /**
     * Arrête toutes les animations sur un nœud.
     */
    public static void stopAllAnimations(Node node) {
        node.setOpacity(1);
        node.setScaleX(1);
        node.setScaleY(1);
        node.setTranslateX(0);
        node.setTranslateY(0);
        node.setRotate(0);
    }

    /**
     * Crée un délai avant d'exécuter une action.
     */
    public static Timeline delay(Duration duration, Runnable action) {
        Timeline timeline = new Timeline(new KeyFrame(duration, e -> action.run()));
        return timeline;
    }
}
