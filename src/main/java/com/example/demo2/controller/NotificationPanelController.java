package com.example.demo2.controller;

import com.example.demo2.service.NotificationService;
import com.example.demo2.service.NotificationService.Notification;
import com.example.demo2.service.NotificationService.NotificationType;
import com.example.demo2.utils.IconHelper;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Predicate;

/**
 * Controlador para el panel de notificaciones
 */
public class NotificationPanelController {
    
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnClearAll;
    @FXML private ToggleButton btnFilterAll;
    @FXML private ToggleButton btnFilterUnread;
    @FXML private ToggleButton btnFilterSuccess;
    @FXML private ToggleButton btnFilterInfo;
    @FXML private ToggleButton btnFilterWarning;
    @FXML private ToggleButton btnFilterError;
    @FXML private ScrollPane notificationsScrollPane;
    @FXML private VBox notificationsContainer;
    @FXML private Label lblTotalNotifications;
    @FXML private Label lblUnreadNotifications;
    
    private NotificationService notificationService;
    private FilteredList<Notification> filteredNotifications;
    private ToggleGroup filterGroup;
    
    @FXML
    public void initialize() {
        notificationService = NotificationService.getInstance();
        
        // Configurar iconos de los botones de filtro
        configurarIconosBotones();
        
        // Configurar grupo de filtros
        configurarFiltros();
        
        // Configurar lista filtrada
        filteredNotifications = new FilteredList<>(notificationService.getNotifications());
        
        // Cargar notificaciones iniciales
        cargarNotificaciones();
        
        // Escuchar cambios en las notificaciones
        notificationService.getNotifications().addListener((javafx.collections.ListChangeListener<Notification>) change -> {
            cargarNotificaciones();
            actualizarEstadisticas();
        });
        
        // Actualizar estadísticas iniciales
        actualizarEstadisticas();
        
        System.out.println("🔔 Panel de notificaciones inicializado");
    }
    
    private void configurarIconosBotones() {
        // Botones de filtro con iconos de Ikonli
        btnFilterAll.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.LIST, IconHelper.SMALL_SIZE, IconHelper.SECONDARY_COLOR));
        btnFilterAll.setText("");
        btnFilterAll.setTooltip(new Tooltip("Todas las notificaciones"));
        
        btnFilterUnread.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CIRCLE, IconHelper.SMALL_SIZE, IconHelper.PRIMARY_COLOR));
        btnFilterUnread.setText("");
        btnFilterUnread.setTooltip(new Tooltip("No leídas"));
        
        btnFilterSuccess.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.SUCCESS_COLOR));
        btnFilterSuccess.setText("");
        btnFilterSuccess.setTooltip(new Tooltip("Éxito"));
        
        btnFilterInfo.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.INFO_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.PRIMARY_COLOR));
        btnFilterInfo.setText("");
        btnFilterInfo.setTooltip(new Tooltip("Información"));
        
        btnFilterWarning.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_TRIANGLE, IconHelper.SMALL_SIZE, IconHelper.WARNING_COLOR));
        btnFilterWarning.setText("");
        btnFilterWarning.setTooltip(new Tooltip("Advertencias"));
        
        btnFilterError.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.ERROR_COLOR));
        btnFilterError.setText("");
        btnFilterError.setTooltip(new Tooltip("Errores"));
        
        // Botones de acción con iconos
        btnMarkAllRead.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_DOUBLE, IconHelper.SMALL_SIZE, IconHelper.SUCCESS_COLOR));
        btnMarkAllRead.setText("");
        btnMarkAllRead.setTooltip(new Tooltip("Marcar todas como leídas"));
        
        btnClearAll.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TRASH_ALT, IconHelper.SMALL_SIZE, IconHelper.ERROR_COLOR));
        btnClearAll.setText("");
        btnClearAll.setTooltip(new Tooltip("Eliminar todas"));
    }
    
    private void configurarFiltros() {
        filterGroup = new ToggleGroup();
        btnFilterAll.setToggleGroup(filterGroup);
        btnFilterUnread.setToggleGroup(filterGroup);
        btnFilterSuccess.setToggleGroup(filterGroup);
        btnFilterInfo.setToggleGroup(filterGroup);
        btnFilterWarning.setToggleGroup(filterGroup);
        btnFilterError.setToggleGroup(filterGroup);
        
        // Seleccionar "Todas" por defecto
        btnFilterAll.setSelected(true);
    }
    
    private void cargarNotificaciones() {
        notificationsContainer.getChildren().clear();
        
        if (filteredNotifications.isEmpty()) {
            // Mostrar mensaje cuando no hay notificaciones
            Label emptyLabel = new Label("No hay notificaciones");
            emptyLabel.getStyleClass().add("empty-notifications");
            notificationsContainer.getChildren().add(emptyLabel);
        } else {
            // Agregar cada notificación
            for (Notification notification : filteredNotifications) {
                VBox notificationItem = crearElementoNotificacion(notification);
                notificationsContainer.getChildren().add(notificationItem);
            }
        }
    }
    
    private VBox crearElementoNotificacion(Notification notification) {
        VBox notificationItem = new VBox(5);
        notificationItem.getStyleClass().add("notification-item");
        if (!notification.isRead()) {
            notificationItem.getStyleClass().add("notification-unread");
        }
        notificationItem.getStyleClass().add(notification.getStyleClass());
        
        // Header de la notificación
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Icono de la notificación usando Ikonli según el tipo
        FontIcon notificationIcon = obtenerIconoNotificacion(notification.getType());
        Label iconLabel = new Label();
        iconLabel.setGraphic(notificationIcon);
        iconLabel.getStyleClass().add("notification-item-icon");
        
        Label titleLabel = new Label(notification.getTitle());
        titleLabel.getStyleClass().add("notification-item-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label timeLabel = new Label(notification.getFormattedTime());
        timeLabel.getStyleClass().add("notification-item-time");
        
        // Botón para eliminar con icono de Ikonli
        Button actionButton = new Button();
        actionButton.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES, IconHelper.SMALL_SIZE, IconHelper.ERROR_COLOR));
        actionButton.getStyleClass().add("notification-action-button");
        actionButton.setTooltip(new Tooltip("Eliminar notificación"));
        actionButton.setOnAction(e -> {
            notificationService.removeNotification(notification);
            cargarNotificaciones();
            actualizarEstadisticas();
        });
        
        header.getChildren().addAll(iconLabel, titleLabel, spacer, timeLabel, actionButton);
        
        // Mensaje de la notificación
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.getStyleClass().add("notification-item-message");
        messageLabel.setWrapText(true);
        
        notificationItem.getChildren().addAll(header, messageLabel);
        
        // Marcar como leída al hacer clic
        notificationItem.setOnMouseClicked(e -> {
            if (!notification.isRead()) {
                notificationService.markAsRead(notification);
                notificationItem.getStyleClass().remove("notification-unread");
                actualizarEstadisticas();
            }
        });
        
        return notificationItem;
    }
    
    private FontIcon obtenerIconoNotificacion(NotificationType tipo) {
        switch (tipo) {
            case SUCCESS:
                return IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.SUCCESS_COLOR);
            case INFO:
                return IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.INFO_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.PRIMARY_COLOR);
            case WARNING:
                return IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_TRIANGLE, IconHelper.SMALL_SIZE, IconHelper.WARNING_COLOR);
            case ERROR:
                return IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.ERROR_COLOR);
            default:
                return IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.BELL, IconHelper.SMALL_SIZE, IconHelper.SECONDARY_COLOR);
        }
    }
    
    private void actualizarEstadisticas() {
        int total = notificationService.getNotifications().size();
        int unread = notificationService.getUnreadNotifications().size();
        
        lblTotalNotifications.setText("Total: " + total);
        lblUnreadNotifications.setText("No leídas: " + unread);
    }
    
    @FXML
    private void handleMarkAllRead() {
        notificationService.markAllAsRead();
        cargarNotificaciones();
        actualizarEstadisticas();
        System.out.println("🔔 Todas las notificaciones marcadas como leídas");
    }
    
    @FXML
    private void handleClearAll() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar");
        confirmAlert.setHeaderText("¿Limpiar todas las notificaciones?");
        confirmAlert.setContentText("Esta acción eliminará todas las notificaciones.\n¿Está seguro?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                notificationService.clearAllNotifications();
                cargarNotificaciones();
                actualizarEstadisticas();
                System.out.println("🔔 Todas las notificaciones eliminadas");
            }
        });
    }
    
    @FXML
    private void handleFilterAll() {
        filteredNotifications.setPredicate(null);
        cargarNotificaciones();
        System.out.println("🔔 Filtro: Todas las notificaciones");
    }
    
    @FXML
    private void handleFilterUnread() {
        filteredNotifications.setPredicate(notification -> !notification.isRead());
        cargarNotificaciones();
        System.out.println("🔔 Filtro: Solo no leídas");
    }
    
    @FXML
    private void handleFilterSuccess() {
        filteredNotifications.setPredicate(notification -> notification.getType() == NotificationType.SUCCESS);
        cargarNotificaciones();
        System.out.println("🔔 Filtro: Solo éxito");
    }
    
    @FXML
    private void handleFilterInfo() {
        filteredNotifications.setPredicate(notification -> notification.getType() == NotificationType.INFO);
        cargarNotificaciones();
        System.out.println("🔔 Filtro: Solo información");
    }
    
    @FXML
    private void handleFilterWarning() {
        filteredNotifications.setPredicate(notification -> notification.getType() == NotificationType.WARNING);
        cargarNotificaciones();
        System.out.println("🔔 Filtro: Solo advertencias");
    }
    
    @FXML
    private void handleFilterError() {
        filteredNotifications.setPredicate(notification -> notification.getType() == NotificationType.ERROR);
        cargarNotificaciones();
        System.out.println("🔔 Filtro: Solo errores");
    }
} 