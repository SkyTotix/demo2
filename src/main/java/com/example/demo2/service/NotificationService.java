package com.example.demo2.service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Servicio para gestionar notificaciones en tiempo real
 */
public class NotificationService {
    
    private static NotificationService instance;
    private final ObservableList<Notification> notifications;
    private final List<Consumer<Notification>> listeners;
    private Timer notificationTimer;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private NotificationService() {
        notifications = FXCollections.observableArrayList();
        listeners = new ArrayList<>();
        iniciarGeneradorNotificaciones();
    }
    
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * Agrega una notificación al sistema
     */
    public void addNotification(String title, String message, NotificationType type) {
        Notification notification = new Notification(
            title, 
            message, 
            type, 
            LocalDateTime.now()
        );
        
        Platform.runLater(() -> {
            notifications.add(0, notification); // Agregar al inicio
            
            // Mantener máximo 50 notificaciones
            if (notifications.size() > 50) {
                notifications.remove(notifications.size() - 1);
            }
            
            // Notificar a todos los listeners
            for (Consumer<Notification> listener : listeners) {
                listener.accept(notification);
            }
            
            System.out.println("🔔 Nueva notificación: " + title + " - " + message);
        });
    }
    
    /**
     * Agrega un listener para nuevas notificaciones
     */
    public void addNotificationListener(Consumer<Notification> listener) {
        listeners.add(listener);
    }
    
    /**
     * Obtiene todas las notificaciones
     */
    public ObservableList<Notification> getNotifications() {
        return notifications;
    }
    
    /**
     * Obtiene notificaciones no leídas
     */
    public ObservableList<Notification> getUnreadNotifications() {
        return notifications.filtered(n -> !n.isRead());
    }
    
    /**
     * Marca una notificación como leída
     */
    public void markAsRead(Notification notification) {
        notification.setRead(true);
    }
    
    /**
     * Marca todas las notificaciones como leídas
     */
    public void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
    }
    
    /**
     * Elimina una notificación
     */
    public void removeNotification(Notification notification) {
        Platform.runLater(() -> notifications.remove(notification));
    }
    
    /**
     * Limpia todas las notificaciones
     */
    public void clearAllNotifications() {
        Platform.runLater(() -> notifications.clear());
    }
    
    /**
     * Inicia el generador de notificaciones automáticas para simular actividad del sistema
     * DESACTIVADO: Solo notificaciones importantes y relevantes
     */
    private void iniciarGeneradorNotificaciones() {
        // *** GENERADOR AUTOMÁTICO DESACTIVADO ***
        // Se eliminaron las notificaciones automáticas innecesarias
        System.out.println("🔕 Generador automático de notificaciones DESACTIVADO para mejor experiencia de usuario");
    }
    
    /**
     * MÉTODO ELIMINADO: Se eliminó el generador de notificaciones aleatorias
     * para evitar spam innecesario de notificaciones automáticas
     */
    
    /**
     * Detiene el generador de notificaciones
     */
    public void shutdown() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
    }
    
    // Métodos de conveniencia para diferentes tipos de notificaciones
    public void notifySuccess(String title, String message) {
        addNotification(title, message, NotificationType.SUCCESS);
    }
    
    public void notifyInfo(String title, String message) {
        addNotification(title, message, NotificationType.INFO);
    }
    
    public void notifyWarning(String title, String message) {
        addNotification(title, message, NotificationType.WARNING);
    }
    
    public void notifyError(String title, String message) {
        addNotification(title, message, NotificationType.ERROR);
    }
    
    /**
     * Clase interna para representar una notificación
     */
    public static class Notification {
        private final String title;
        private final String message;
        private final NotificationType type;
        private final LocalDateTime timestamp;
        private boolean read;
        
        public Notification(String title, String message, NotificationType type, LocalDateTime timestamp) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
            this.read = false;
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public NotificationType getType() { return type; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        
        public void setRead(boolean read) { this.read = read; }
        
        public String getFormattedTime() {
            return timestamp.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        }
        
        public String getIcon() {
            switch (type) {
                case SUCCESS: return "✅";
                case WARNING: return "⚠️";
                case ERROR: return "❌";
                case INFO:
                default: return "ℹ️";
            }
        }
        
        public String getStyleClass() {
            return "notification-" + type.name().toLowerCase();
        }
    }
    
    /**
     * Enum para tipos de notificación
     */
    public enum NotificationType {
        SUCCESS, INFO, WARNING, ERROR
    }
} 