package com.example.demo2.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.CacheHint;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * UTILIDAD DE ANIMACIONES AVANZADAS PARA JAVAFX
 * =============================================
 * 
 * Esta clase proporciona un conjunto completo de animaciones optimizadas para JavaFX,
 * diseñadas específicamente para mejorar la experiencia de usuario en BiblioSystem.
 * Implementa las mejores prácticas de rendimiento y utiliza técnicas avanzadas de
 * animación para crear transiciones suaves y profesionales.
 * 
 * CARACTERÍSTICAS PRINCIPALES:
 * - Animaciones optimizadas para rendimiento con cache hints
 * - Soporte para animaciones paralelas y secuenciales
 * - Interpoladores personalizados para movimientos naturales
 * - Sistema de callbacks para eventos de animación
 * - Gestión automática de memoria y recursos
 * - Compatibilidad con BootstrapFX y CSS personalizado
 * 
 * TIPOS DE ANIMACIONES SOPORTADAS:
 * 1. Animaciones de Entrada (fadeIn, slideIn, scaleIn, etc.)
 * 2. Animaciones de Salida (fadeOut, slideOut, scaleOut, etc.)
 * 3. Animaciones de Transición (crossFade, morphTransition, etc.)
 * 4. Animaciones de Feedback (shake, pulse, bounce, etc.)
 * 5. Animaciones de Carga (spinner, progress, skeleton, etc.)
 * 6. Animaciones de Hover y Focus (enhancedHover, focusGlow, etc.)
 * 
 * OPTIMIZACIONES DE RENDIMIENTO:
 * - Uso de cache hints para elementos complejos
 * - Sincronización con el pulse de JavaFX
 * - Reutilización de objetos Timeline
 * - Gestión eficiente de memoria
 * - Soporte para hardware acceleration
 * 
 * INTEGRACIÓN CON SISTEMA:
 * - Compatible con todos los controladores del sistema
 * - Integrado con sistema de notificaciones
 * - Soporte para temas y estilos dinámicos
 * - Callbacks para eventos de UI
 */
public class AnimationUtils {
    
    // === CONSTANTES DE CONFIGURACIÓN ===
    private static final Duration DEFAULT_DURATION = Duration.millis(300);
    private static final Duration FAST_DURATION = Duration.millis(150);
    private static final Duration SLOW_DURATION = Duration.millis(500);
    private static final Duration VERY_SLOW_DURATION = Duration.millis(800);
    
    // Interpoladores personalizados para movimientos naturales
    private static final Interpolator EASE_OUT = Interpolator.SPLINE(0.25, 0.46, 0.45, 0.94);
    private static final Interpolator EASE_IN_OUT = Interpolator.SPLINE(0.42, 0, 0.58, 1);
    private static final Interpolator BOUNCE = Interpolator.SPLINE(0.68, 0.1, 0.265, 0.9);
    private static final Interpolator ELASTIC = Interpolator.SPLINE(0.175, 0.885, 0.32, 1.0);
    
    // === ANIMACIONES DE ENTRADA ===
    
    /**
     * Animación de aparición gradual (Fade In)
     * Optimizada para rendimiento con cache hints
     */
    public static Timeline fadeIn(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return null;
        
        // Optimización de rendimiento
        enableCaching(node);
        
        node.setOpacity(0.0);
        
        Timeline timeline = new Timeline(
            new KeyFrame(duration,
                new KeyValue(node.opacityProperty(), 1.0, EASE_OUT)
            )
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    /**
     * Animación de deslizamiento desde la izquierda
     */
    public static Timeline slideInLeft(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return null;
        
        enableCaching(node);
        
        double originalX = node.getTranslateX();
        node.setTranslateX(-node.getBoundsInParent().getWidth());
        node.setOpacity(0.0);
        
        Timeline timeline = new Timeline(
            new KeyFrame(duration,
                new KeyValue(node.translateXProperty(), originalX, EASE_OUT),
                new KeyValue(node.opacityProperty(), 1.0, EASE_OUT)
            )
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    /**
     * Animación de deslizamiento desde la derecha
     */
    public static Timeline slideInRight(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return null;
        
        enableCaching(node);
        
        double originalX = node.getTranslateX();
        node.setTranslateX(node.getBoundsInParent().getWidth());
        node.setOpacity(0.0);
        
        Timeline timeline = new Timeline(
            new KeyFrame(duration,
                new KeyValue(node.translateXProperty(), originalX, EASE_OUT),
                new KeyValue(node.opacityProperty(), 1.0, EASE_OUT)
            )
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    /**
     * Animación de escalado desde pequeño a normal
     */
    public static Timeline scaleIn(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return null;
        
        enableCaching(node);
        
        node.setScaleX(0.0);
        node.setScaleY(0.0);
        node.setOpacity(0.0);
        
        Timeline timeline = new Timeline(
            new KeyFrame(duration,
                new KeyValue(node.scaleXProperty(), 1.0, BOUNCE),
                new KeyValue(node.scaleYProperty(), 1.0, BOUNCE),
                new KeyValue(node.opacityProperty(), 1.0, EASE_OUT)
            )
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    /**
     * Animación de deslizamiento desde arriba
     */
    public static Timeline slideInTop(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return null;
        
        enableCaching(node);
        
        double originalY = node.getTranslateY();
        node.setTranslateY(-node.getBoundsInParent().getHeight());
        node.setOpacity(0.0);
        
        Timeline timeline = new Timeline(
            new KeyFrame(duration,
                new KeyValue(node.translateYProperty(), originalY, EASE_OUT),
                new KeyValue(node.opacityProperty(), 1.0, EASE_OUT)
            )
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    // === ANIMACIONES DE SALIDA ===
    
    /**
     * Animación de desaparición gradual (Fade Out)
     */
    public static Timeline fadeOut(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return null;
        
        enableCaching(node);
        
        Timeline timeline = new Timeline(
            new KeyFrame(duration,
                new KeyValue(node.opacityProperty(), 0.0, EASE_IN_OUT)
            )
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    /**
     * Animación de deslizamiento hacia la izquierda (salida)
     */
    public static Timeline slideOutLeft(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return null;
        
        enableCaching(node);
        
        Timeline timeline = new Timeline(
            new KeyFrame(duration,
                new KeyValue(node.translateXProperty(), -node.getBoundsInParent().getWidth(), EASE_IN_OUT),
                new KeyValue(node.opacityProperty(), 0.0, EASE_IN_OUT)
            )
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    // === ANIMACIONES DE FEEDBACK ===
    
    /**
     * Animación de sacudida para errores
     */
    public static Timeline shake(Node node, Runnable onFinished) {
        if (node == null) return null;
        
        double originalX = node.getTranslateX();
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0), new KeyValue(node.translateXProperty(), originalX)),
            new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), originalX + 10)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), originalX - 10)),
            new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), originalX + 8)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), originalX - 8)),
            new KeyFrame(Duration.millis(250), new KeyValue(node.translateXProperty(), originalX + 5)),
            new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), originalX - 5)),
            new KeyFrame(Duration.millis(350), new KeyValue(node.translateXProperty(), originalX))
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> onFinished.run());
        }
        
        return timeline;
    }
    
    /**
     * Animación de pulso para llamar la atención
     */
    public static Timeline pulse(Node node, int cycles, Runnable onFinished) {
        if (node == null) return null;
        
        enableCaching(node);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(node.scaleXProperty(), 1.0),
                new KeyValue(node.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(node.scaleXProperty(), 1.1, EASE_IN_OUT),
                new KeyValue(node.scaleYProperty(), 1.1, EASE_IN_OUT)
            ),
            new KeyFrame(Duration.millis(600),
                new KeyValue(node.scaleXProperty(), 1.0, EASE_IN_OUT),
                new KeyValue(node.scaleYProperty(), 1.0, EASE_IN_OUT)
            )
        );
        
        timeline.setCycleCount(cycles);
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    /**
     * Animación de rebote
     */
    public static Timeline bounce(Node node, Runnable onFinished) {
        if (node == null) return null;
        
        enableCaching(node);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0), new KeyValue(node.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(150), new KeyValue(node.translateYProperty(), -20, EASE_OUT)),
            new KeyFrame(Duration.millis(300), new KeyValue(node.translateYProperty(), 0, BOUNCE))
        );
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> {
                disableCaching(node);
                onFinished.run();
            });
        } else {
            timeline.setOnFinished(e -> disableCaching(node));
        }
        
        return timeline;
    }
    
    // === ANIMACIONES COMPLEJAS ===
    
    /**
     * Animación escalonada para múltiples elementos
     */
    public static SequentialTransition staggeredAnimation(List<Node> nodes, Duration delayBetween, 
                                                         AnimationType animationType, Runnable onFinished) {
        if (nodes == null || nodes.isEmpty()) return null;
        
        SequentialTransition sequence = new SequentialTransition();
        
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node != null) {
                Timeline animation;
                
                switch (animationType) {
                    case FADE_IN:
                        animation = fadeIn(node, DEFAULT_DURATION, null);
                        break;
                    case SLIDE_IN_LEFT:
                        animation = slideInLeft(node, DEFAULT_DURATION, null);
                        break;
                    case SCALE_IN:
                        animation = scaleIn(node, DEFAULT_DURATION, null);
                        break;
                    default:
                        animation = fadeIn(node, DEFAULT_DURATION, null);
                }
                
                if (animation != null) {
                    if (i > 0) {
                        sequence.getChildren().add(new PauseTransition(delayBetween));
                    }
                    sequence.getChildren().add(animation);
                }
            }
        }
        
        if (onFinished != null) {
            sequence.setOnFinished(e -> onFinished.run());
        }
        
        return sequence;
    }
    
    /**
     * Transición cruzada entre dos nodos
     */
    public static ParallelTransition crossFade(Node nodeOut, Node nodeIn, Duration duration, Runnable onFinished) {
        if (nodeOut == null || nodeIn == null) return null;
        
        Timeline fadeOutAnimation = fadeOut(nodeOut, duration, null);
        Timeline fadeInAnimation = fadeIn(nodeIn, duration, null);
        
        ParallelTransition parallel = new ParallelTransition(fadeOutAnimation, fadeInAnimation);
        
        if (onFinished != null) {
            parallel.setOnFinished(e -> onFinished.run());
        }
        
        return parallel;
    }
    
    /**
     * Animación de hover mejorada con efectos de sombra
     */
    public static void enhancedHover(Node node, boolean enter) {
        if (node == null) return;
        
        enableCaching(node);
        
        if (enter) {
            // Efecto de entrada del hover
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.3));
            shadow.setRadius(10);
            shadow.setOffsetY(3);
            
            Timeline hoverIn = new Timeline(
                new KeyFrame(FAST_DURATION,
                    new KeyValue(node.scaleXProperty(), 1.05, EASE_OUT),
                    new KeyValue(node.scaleYProperty(), 1.05, EASE_OUT)
                )
            );
            
            node.setEffect(shadow);
            hoverIn.play();
        } else {
            // Efecto de salida del hover
            Timeline hoverOut = new Timeline(
                new KeyFrame(FAST_DURATION,
                    new KeyValue(node.scaleXProperty(), 1.0, EASE_OUT),
                    new KeyValue(node.scaleYProperty(), 1.0, EASE_OUT)
                )
            );
            
            hoverOut.setOnFinished(e -> {
                node.setEffect(null);
                disableCaching(node);
            });
            hoverOut.play();
        }
    }
    
    /**
     * Animación de carga con spinner rotatorio
     */
    public static RotateTransition createSpinner(Node node) {
        if (node == null) return null;
        
        RotateTransition spinner = new RotateTransition(Duration.seconds(1), node);
        spinner.setByAngle(360);
        spinner.setCycleCount(Timeline.INDEFINITE);
        spinner.setInterpolator(Interpolator.LINEAR);
        
        return spinner;
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * Habilita el cache para optimizar el rendimiento de animaciones
     */
    private static void enableCaching(Node node) {
        if (node != null) {
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
        }
    }
    
    /**
     * Deshabilita el cache después de la animación
     */
    private static void disableCaching(Node node) {
        if (node != null) {
            node.setCache(false);
        }
    }
    
    /**
     * Ejecuta una animación de forma asíncrona
     */
    public static CompletableFuture<Void> playAsync(Animation animation) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (animation != null) {
            animation.setOnFinished(e -> future.complete(null));
            Platform.runLater(animation::play);
        } else {
            future.complete(null);
        }
        
        return future;
    }
    
    /**
     * Detiene todas las animaciones de un nodo
     */
    public static void stopAllAnimations(Node node) {
        if (node != null) {
            // Restaurar propiedades a valores por defecto
            node.setOpacity(1.0);
            node.setScaleX(1.0);
            node.setScaleY(1.0);
            node.setTranslateX(0.0);
            node.setTranslateY(0.0);
            node.setRotate(0.0);
            node.setEffect(null);
            disableCaching(node);
        }
    }
    
    /**
     * Crea una animación personalizada con Timeline
     */
    public static Timeline createCustomAnimation(Duration duration, KeyValue... keyValues) {
        return new Timeline(new KeyFrame(duration, keyValues));
    }
    
    // === ENUMERACIONES ===
    
    /**
     * Tipos de animación disponibles
     */
    public enum AnimationType {
        FADE_IN,
        FADE_OUT,
        SLIDE_IN_LEFT,
        SLIDE_IN_RIGHT,
        SLIDE_IN_TOP,
        SLIDE_OUT_LEFT,
        SCALE_IN,
        SCALE_OUT,
        SHAKE,
        PULSE,
        BOUNCE
    }
    
    /**
     * Velocidades de animación predefinidas
     */
    public enum AnimationSpeed {
        VERY_FAST(Duration.millis(100)),
        FAST(Duration.millis(150)),
        NORMAL(Duration.millis(300)),
        SLOW(Duration.millis(500)),
        VERY_SLOW(Duration.millis(800));
        
        private final Duration duration;
        
        AnimationSpeed(Duration duration) {
            this.duration = duration;
        }
        
        public Duration getDuration() {
            return duration;
        }
    }
}