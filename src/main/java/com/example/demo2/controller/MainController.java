package com.example.demo2.controller;

// Modelo de enumeración que define los tipos de usuario del sistema
import com.example.demo2.models.enums.TipoUsuario;

// Servicios del sistema que proporcionan lógica de negocio
import com.example.demo2.service.AppConfigService;    // Configuraciones de la aplicación
import com.example.demo2.service.AuthService;        // Gestión de autenticación y sesiones
import com.example.demo2.service.NotificationService; // Sistema de notificaciones
import com.example.demo2.service.UsuarioService;      // Operaciones CRUD de usuarios
import com.example.demo2.service.LibroService;        // Operaciones CRUD de libros

// Utilidad para iconos en la interfaz
import com.example.demo2.utils.IconHelper;

// Importaciones de JavaFX para la interfaz de usuario
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;

// Frameworks de estilos para interfaz moderna
import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

/**
 * CONTROLADOR PRINCIPAL DEL SISTEMA - DASHBOARD DINÁMICO POR ROL
 * =============================================================
 * 
 * Este es el controlador central de BiblioSystem que gestiona la interfaz principal
 * después del login exitoso. Proporciona un dashboard personalizado según el rol
 * del usuario autenticado y sirve como hub de navegación para todas las funciones.
 * 
 * CARACTERÍSTICAS PRINCIPALES:
 * 1. Dashboard dinámico que cambia según el rol (SuperAdmin, Admin, Bibliotecario)
 * 2. Estadísticas en tiempo real cargadas desde Oracle Cloud
 * 3. Menú lateral personalizado por permisos de usuario
 * 4. Sistema de notificaciones con badge de conteo
 * 5. Acciones rápidas contextuales para cada rol
 * 6. Actividad reciente del sistema
 * 7. Animaciones y transiciones suaves para mejor UX
 * 
 * VISTAS POR ROL:
 * 
 * SUPERADMIN:
 * - Estadísticas: Admins, Bibliotecarios, Total Usuarios, Estado BD
 * - Menú: Dashboard, Administradores, Configuración
 * - Acciones: Nuevo Admin, Configurar Sistema
 * 
 * ADMIN:
 * - Estadísticas: Bibliotecarios, Libros, Usuarios, Préstamos
 * - Menú: Dashboard, Bibliotecarios, Libros, Usuarios, Préstamos
 * - Acciones: Nuevo Bibliotecario, Nuevo Libro, Nuevo Usuario, Nuevo Préstamo
 * 
 * BIBLIOTECARIO:
 * - Estadísticas: Libros Totales, Disponibles, Prestados, Usuarios
 * - Menú: Dashboard, Usuarios, Préstamos, Buscar Libros
 * - Acciones: Nuevo Usuario, Nuevo Préstamo, Devolución, Buscar Libro
 * 
 * ASPECTOS TÉCNICOS:
 * - Carga de datos asíncrona para no bloquear la UI
 * - Manejo de errores con fallbacks visuales
 * - Animaciones CSS y JavaFX para transiciones suaves
 * - Sistema de notificaciones en tiempo real
 * - Navegación dinámica entre módulos del sistema
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Conectado con main-view.fxml (definición de interfaz)
 * - Utiliza todos los servicios principales del sistema
 * - Punto de navegación hacia otros controladores (AdminFormController, etc.)
 * - Integrado con el sistema de notificaciones
 * - Aplica estilos definidos en styles/main.css
 */
public class MainController {
    
    // === ELEMENTOS DE INTERFAZ DE USUARIO (FXML) ===
    
    // HEADER - Información del usuario y controles principales
    @FXML private FontIcon headerLogo;          // Logo personalizable en el header
    @FXML private ImageView headerLogoImage;    // Logo personalizado con imagen
    @FXML private StackPane logoContainer;      // Contenedor del logo
    @FXML private Label userNameLabel;          // Nombre completo del usuario autenticado
    @FXML private Label userRoleLabel;          // Rol/tipo de usuario (SuperAdmin, Admin, etc.)
    @FXML private Button logoutButton;          // Botón de cerrar sesión
    @FXML private Button notificationButton;    // Botón para mostrar notificaciones
    @FXML private Label notificationBadge;      // Badge con número de notificaciones no leídas
    
    // LAYOUT PRINCIPAL - Estructura del dashboard
    @FXML private VBox menuContainer;           // Contenedor del menú lateral
    @FXML private ScrollPane contentScrollPane; // Área de contenido con scroll
    @FXML private VBox dashboardContainer;      // Contenedor principal del dashboard
    @FXML private StackPane contentArea;        // Área donde se cargan diferentes vistas
    
    // DASHBOARD - Elementos del panel principal
    @FXML private Label welcomeLabel;           // Título de bienvenida personalizado
    @FXML private Label dashboardSubtitle;      // Subtítulo descriptivo del rol

    @FXML private FlowPane quickActionsPane;   // Panel de acciones rápidas

    
    // === SERVICIOS DEL SISTEMA ===
    private AuthService authService;            // Gestión de autenticación y usuario actual
    private NotificationService notificationService; // Sistema de notificaciones
    private UsuarioService usuarioService;      // Operaciones CRUD de usuarios
    private LibroService libroService;          // Operaciones CRUD de libros
    private Popup notificationPopup;            // Popup para mostrar notificaciones
    
    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        notificationService = NotificationService.getInstance();
        usuarioService = UsuarioService.getInstance();
        libroService = LibroService.getInstance();
        
        // Configurar información del usuario
        setupUserInfo();
        
        // Configurar menú según el rol
        setupMenuByRole();
        
        // Configurar dashboard
        setupDashboard();
        
        // Estadísticas removidas por solicitud del usuario
        
        // Configurar acciones rápidas
        setupQuickActions();
        
        // Configurar notificaciones
        setupNotifications();
        
        // Configurar logo personalizable
        setupLogo();
        
        // Registrar listener para cambios de logo
        AppConfigService.addLogoChangeListener(this::actualizarLogo);
    }
    
    /**
     * Configura la información del usuario en el header
     */
    private void setupUserInfo() {
        if (authService.getUsuarioActual() != null) {
            userNameLabel.setText(authService.getUsuarioActual().getNombreCompleto());
            userRoleLabel.setText(authService.getUsuarioActual().getTipoUsuario().getDescripcion());
            welcomeLabel.setText("¡Bienvenido, " + authService.getUsuarioActual().getNombre() + "!");
        }
    }
    
    /**
     * Configura el menú lateral según el rol del usuario
     */
    private void setupMenuByRole() {
        TipoUsuario tipoUsuario = authService.getUsuarioActual().getTipoUsuario();
        
        // Limpiar menú existente
        menuContainer.getChildren().clear();
        
        // Dashboard (común para todos)
        addMenuItem(IconHelper.getHomeIcon(), "Dashboard", "dashboard", true);
        
        switch (tipoUsuario) {
            case SUPERADMIN:
                addMenuSection("Gestión de Sistema");
                addMenuItem(IconHelper.getAdminIcon(), "Administradores", "administradores", false);
                break;
                
            case ADMIN:
                addMenuSection("Gestión de Personal");
                addMenuItem(IconHelper.getLibrarianIcon(), "Bibliotecarios", "bibliotecarios", false);
                
                addMenuSection("Gestión de Biblioteca");
                addMenuItem(IconHelper.getBookIcon(), "Libros", "libros", false);
                addMenuItem(IconHelper.getUsersIcon(), "Usuarios/Lectores", "lectores", true);
                addMenuItem(IconHelper.getLoanIcon(), "Préstamos", "prestamos", false);
    
                
                addMenuSection("Personalización");
                addMenuItem(IconHelper.getSettingsIcon(), "Configuraciones", "configuraciones", false);
                break;
                
            case BIBLIOTECARIO:
                addMenuSection("Operaciones Diarias");
                addMenuItem(IconHelper.getUsersIcon(), "Usuarios/Lectores", "lectores", true);
                addMenuItem(IconHelper.getLoanIcon(), "Préstamos", "prestamos", false);
                addMenuItem(IconHelper.getSearchIcon(), "Buscar Libros", "buscar-libros", false);
    
                break;
        }
    }
    
    /**
     * Agrega una sección al menú
     */
    private void addMenuSection(String title) {
        Label sectionLabel = new Label(title);
        sectionLabel.getStyleClass().add("menu-section");
        VBox.setMargin(sectionLabel, new javafx.geometry.Insets(16, 0, 8, 0));
        menuContainer.getChildren().add(sectionLabel);
    }
    
    /**
     * Agrega un elemento al menú con icono
     */
    private void addMenuItem(FontIcon icon, String text, String action, boolean active) {
        Button menuButton = new Button(text);
        menuButton.setGraphic(icon);
        menuButton.getStyleClass().add("menu-item");
        if (active) {
            menuButton.getStyleClass().add("menu-item-active");
        }
        menuButton.setMaxWidth(Double.MAX_VALUE);
        menuButton.setOnAction(e -> handleMenuAction(action));
        
        menuContainer.getChildren().add(menuButton);
    }
    
    /**
     * Configura el dashboard principal
     */
    private void setupDashboard() {
        TipoUsuario tipoUsuario = authService.getUsuarioActual().getTipoUsuario();
        
        switch (tipoUsuario) {
            case SUPERADMIN:
                dashboardSubtitle.setText("Panel de administración del sistema - Control total");
                break;
            case ADMIN:
                dashboardSubtitle.setText("Panel de administración de biblioteca - Gestión completa");
                break;
            case BIBLIOTECARIO:
                dashboardSubtitle.setText("Panel de operaciones bibliotecarias - Gestión diaria");
                break;
        }
        
        // Proteger el título de clicks accidentales
        protegerTituloDashboard();
    }
    
    /**
     * Protege el título y subtítulo del dashboard contra eventos de click accidentales
     */
    private void protegerTituloDashboard() {
        // Asegurar que el título y subtítulo mantengan sus propiedades
        welcomeLabel.setMouseTransparent(true); // Evitar que reciba eventos de mouse
        dashboardSubtitle.setMouseTransparent(true); // Proteger también el subtítulo
        
        // Restaurar título y subtítulo si es necesario
        restaurarTituloDashboard();
        
        // Programar restauración periódica para mantener el título y subtítulo correctos
        javafx.animation.Timeline restaurarTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> restaurarTituloDashboard())
        );
        restaurarTimer.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        restaurarTimer.play();
        
        // Evitar que el contenedor del dashboard capture clicks que puedan interferir
        dashboardContainer.setOnMouseClicked(event -> {
            // Consumir el evento para que no se propague
            event.consume();
        });
        
        // Proteger también el área de títulos específicamente
        if (welcomeLabel.getParent() != null) {
            welcomeLabel.getParent().setOnMouseClicked(event -> {
                // Consumir el evento para que no afecte los títulos
                event.consume();
            });
        }
        

        
        System.out.println("🔒 Protección del título y subtítulo del dashboard activada");
    }
    
    /**
     * Restaura el título y subtítulo del dashboard a su estado correcto
     */
    private void restaurarTituloDashboard() {
        if (welcomeLabel != null) {
            // Asegurar que el estilo CSS se mantenga para el título
            welcomeLabel.getStyleClass().clear();
            welcomeLabel.getStyleClass().add("dashboard-title");
            
            // Agregar estilo inline como respaldo para el título
            welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'System Bold';");
        }
        
        if (dashboardSubtitle != null) {
            // Asegurar que el estilo CSS se mantenga para el subtítulo
            dashboardSubtitle.getStyleClass().clear();
            dashboardSubtitle.getStyleClass().add("dashboard-subtitle");
            
            // Agregar estilo inline como respaldo para el subtítulo
            dashboardSubtitle.setStyle("-fx-font-size: 14px; -fx-font-family: 'System';");
        }
    }
    

    

    
    /**
     * Configura las acciones rápidas
     */
    private void setupQuickActions() {
        quickActionsPane.getChildren().clear();
        
        TipoUsuario tipoUsuario = authService.getUsuarioActual().getTipoUsuario();
        
        switch (tipoUsuario) {
            case SUPERADMIN:
                addQuickAction(IconHelper.getNewAdminIcon(), "Nuevo Admin", "nuevo-admin");
        
                addQuickAction(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.DATABASE, IconHelper.MEDIUM_SIZE, IconHelper.PRIMARY_COLOR), "Gestión BD", "gestion-bd");
                addQuickAction(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SHIELD_ALT, IconHelper.MEDIUM_SIZE, IconHelper.WARNING_COLOR), "Seguridad", "seguridad");
                addQuickAction(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.COG, IconHelper.MEDIUM_SIZE, IconHelper.SECONDARY_COLOR), "Mantenimiento", "mantenimiento");
                addQuickAction(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.DOWNLOAD, IconHelper.MEDIUM_SIZE, IconHelper.SUCCESS_COLOR), "Backup", "backup");
                break;
                
            case ADMIN:
                addQuickAction(IconHelper.getNewLibrarianIcon(), "Nuevo Bibliotecario", "nuevo-bibliotecario");
                addQuickAction(IconHelper.getNewBookIcon(), "Nuevo Libro", "nuevo-libro");
                addQuickAction(IconHelper.getNewUserIcon(), "Nuevo Lector", "nuevo-lector");
                addQuickAction(IconHelper.getNewLoanIcon(), "Nuevo Préstamo", "nuevo-prestamo");
                break;
                
            case BIBLIOTECARIO:
                addQuickAction(IconHelper.getNewUserIcon(), "Nuevo Lector", "nuevo-lector");
                addQuickAction(IconHelper.getNewLoanIcon(), "Nuevo Préstamo", "nuevo-prestamo");
                addQuickAction(IconHelper.getDevolutionIcon(), "Registrar Devolución", "devolucion");
                addQuickAction(IconHelper.getSearchBookIcon(), "Buscar Libro", "buscar-libro");
                break;
        }
    }
    
    /**
     * Agrega un botón de acción rápida con icono
     */
    private void addQuickAction(FontIcon icon, String text, String action) {
        Button actionButton = new Button(text);
        actionButton.setGraphic(icon);
        actionButton.getStyleClass().add("quick-action-button");
        actionButton.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
        actionButton.setOnAction(e -> handleQuickAction(action));
        
        quickActionsPane.getChildren().add(actionButton);
    }
    

    
    /**
     * Maneja las acciones del menú
     */
    private void handleMenuAction(String action) {
        System.out.println("Acción del menú: " + action);
        
        // Actualizar estado visual del menú
        actualizarMenuActivo(action);
        
        switch (action) {
            case "dashboard":
                mostrarDashboard();
                break;
            case "administradores":
                cargarVistaAdministradores();
                break;
            case "bibliotecarios":
                cargarVistaBibliotecarios();
                break;
            case "libros":
                cargarVistaLibros();
                break;
            case "configuraciones":
                cargarVistaConfiguraciones();
                break;
            case "lectores":
                cargarVistaLectores();
                break;
            case "prestamos":
                cargarVistaPrestamos();
                break;

            default:
                mostrarMensajeDesarrollo("Funcionalidad: " + action);
        }
    }
    
    /**
     * Maneja las acciones rápidas
     */
    private void handleQuickAction(String action) {
        System.out.println("Acción rápida: " + action);
        
        switch (action) {
            case "nuevo-admin":
                cargarVistaAdministradores();
                break;
            case "nuevo-lector":
                cargarVistaLectores();
                break;
            case "nuevo-prestamo":
                cargarVistaPrestamos();
                break;

            default:
                mostrarMensajeDesarrollo("Acción rápida: " + action);
        }
    }
    
    /**
     * Método auxiliar para manejar acciones del menú desde otros lugares
     */
    private void manejarAccionMenu(String action) {
        handleMenuAction(action);
    }
    
    /**
     * Actualiza el estado visual del menú (marcar item activo)
     */
    private void actualizarMenuActivo(String action) {
        // Remover clase activa de todos los botones del menú
        for (var node : menuContainer.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("menu-item-active");
            }
        }
        
        // Agregar clase activa al botón correspondiente
        for (var node : menuContainer.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String buttonText = btn.getText().toLowerCase();
                
                if ((action.equals("dashboard") && buttonText.contains("dashboard")) ||
                    (action.equals("administradores") && buttonText.contains("administradores")) ||
                    (action.equals("bibliotecarios") && buttonText.contains("bibliotecarios")) ||
                    (action.equals("libros") && buttonText.contains("libros")) ||
                    (action.equals("lectores") && buttonText.contains("lectores")) ||
                    (action.equals("prestamos") && buttonText.contains("préstamos")) ||
    
                    (action.equals("configuraciones") && buttonText.contains("configuraciones"))) {
                    btn.getStyleClass().add("menu-item-active");
                    break;
                }
            }
        }
    }
    
    /**
     * Muestra el dashboard principal
     */
    private void mostrarDashboard() {
        // Agregar efecto de transición suave
        contentScrollPane.getStyleClass().add("content-transition");
        
        // Mostrar el contenido del dashboard original
        contentScrollPane.setContent(dashboardContainer);
        contentScrollPane.setVisible(true);
        
        // Aplicar efecto de carga completada y actualizar estadísticas
        javafx.application.Platform.runLater(() -> {
            contentScrollPane.getStyleClass().remove("content-transition");
            contentScrollPane.getStyleClass().add("content-loaded");
            
            // Estadísticas removidas del dashboard
        });
    }
    

    

    
    /**
     * Carga la vista de administradores en el área de contenido
     */
    private void cargarVistaAdministradores() {
        try {
            // Agregar efecto de transición
            contentScrollPane.getStyleClass().add("content-transition");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/admin-management-view.fxml"));
            Parent adminView = loader.load();
            
            // Reemplazar el contenido del ScrollPane con la vista de administradores
            contentScrollPane.setContent(adminView);
            
            // Aplicar efecto de carga completada
            javafx.application.Platform.runLater(() -> {
                contentScrollPane.getStyleClass().remove("content-transition");
                contentScrollPane.getStyleClass().add("content-loaded");
            });
            
            System.out.println("✅ Vista de administradores cargada en el dashboard");
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando vista de administradores: " + e.getMessage());
            mostrarError("Error", "No se pudo cargar la gestión de administradores");
        }
    }
    
    /**
     * Carga la vista de bibliotecarios en el área de contenido
     */
    private void cargarVistaBibliotecarios() {
        try {
            // Agregar efecto de transición
            contentScrollPane.getStyleClass().add("content-transition");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/bibliotecario-management-view.fxml"));
            Parent bibliotecarioView = loader.load();
            
            // Reemplazar el contenido del ScrollPane con la vista de bibliotecarios
            contentScrollPane.setContent(bibliotecarioView);
            
            // Aplicar efecto de carga completada
            javafx.application.Platform.runLater(() -> {
                contentScrollPane.getStyleClass().remove("content-transition");
                contentScrollPane.getStyleClass().add("content-loaded");
            });
            
            System.out.println("✅ Vista de bibliotecarios cargada en el dashboard");
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando vista de bibliotecarios: " + e.getMessage());
            mostrarError("Error", "No se pudo cargar la gestión de bibliotecarios");
        }
    }
    

    
    /**
     * Carga la vista de gestión de libros en el área de contenido
     */
    private void cargarVistaLibros() {
        try {
            // Agregar efecto de transición
            contentScrollPane.getStyleClass().add("content-transition");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/libro-management-view.fxml"));
            Parent libroView = loader.load();
            
            // Reemplazar el contenido del ScrollPane con la vista de libros
            contentScrollPane.setContent(libroView);
            
            // Aplicar efecto de carga completada
            javafx.application.Platform.runLater(() -> {
                contentScrollPane.getStyleClass().remove("content-transition");
                contentScrollPane.getStyleClass().add("content-loaded");
            });
            
            System.out.println("📚 Vista de gestión de libros cargada en el dashboard");
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando vista de libros: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "No se pudo cargar la gestión de libros: " + e.getMessage());
        }
    }
    

    
    /**
     * Carga la vista de configuraciones del administrador
     */
    private void cargarVistaConfiguraciones() {
        try {
            // Agregar efecto de transición
            contentScrollPane.getStyleClass().add("content-transition");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/admin-config-view.fxml"));
            Parent configView = loader.load();
            
            // Reemplazar el contenido del ScrollPane con la vista de configuraciones
            contentScrollPane.setContent(configView);
            
            // Aplicar efecto de carga completada
            javafx.application.Platform.runLater(() -> {
                contentScrollPane.getStyleClass().remove("content-transition");
                contentScrollPane.getStyleClass().add("content-loaded");
            });
            
            System.out.println("⚙️ Vista de configuraciones cargada en el dashboard");
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando vista de configuraciones: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "No se pudo cargar las configuraciones: " + e.getMessage());
        }
    }
    
    /**
     * Carga la vista de gestión de lectores
     */
    private void cargarVistaLectores() {
        try {
            // Agregar efecto de transición
            contentScrollPane.getStyleClass().add("content-transition");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/lector-management-view.fxml"));
            Parent lectorView = loader.load();
            
            // Reemplazar el contenido del ScrollPane con la vista de lectores
            contentScrollPane.setContent(lectorView);
            
            // Aplicar efecto de carga completada
            javafx.application.Platform.runLater(() -> {
                contentScrollPane.getStyleClass().remove("content-transition");
                contentScrollPane.getStyleClass().add("content-loaded");
            });
            
            System.out.println("👥 Vista de gestión de lectores cargada en el dashboard");
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando vista de lectores: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "No se pudo cargar la gestión de lectores: " + e.getMessage());
        }
    }
    
    /**
     * Carga la vista de gestión de préstamos
     */
    private void cargarVistaPrestamos() {
        try {
            // Agregar efecto de transición
            contentScrollPane.getStyleClass().add("content-transition");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/prestamo-management-view.fxml"));
            Parent prestamoView = loader.load();
            
            // Reemplazar el contenido del ScrollPane con la vista de préstamos
            contentScrollPane.setContent(prestamoView);
            
            // Aplicar efecto de carga completada
            javafx.application.Platform.runLater(() -> {
                contentScrollPane.getStyleClass().remove("content-transition");
                contentScrollPane.getStyleClass().add("content-loaded");
            });
            
            System.out.println("📚 Vista de gestión de préstamos cargada en el dashboard");
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando vista de préstamos: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "No se pudo cargar la gestión de préstamos: " + e.getMessage());
        }
    }
    

    
    /**
     * Muestra un mensaje de funcionalidad en desarrollo
     */
    private void mostrarMensajeDesarrollo(String funcionalidad) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("En Desarrollo");
        alert.setHeaderText(funcionalidad);
        alert.setContentText("Esta funcionalidad está en desarrollo y estará disponible próximamente.");
        alert.showAndWait();
    }
    
    /**
     * Configura el sistema de notificaciones
     */
    private void setupNotifications() {
        // Configurar listener para actualizar el badge
        notificationService.addNotificationListener(notification -> {
            actualizarBadgeNotificaciones();
        });
        
        // Escuchar cambios en las notificaciones para actualizar el badge
        notificationService.getNotifications().addListener((javafx.collections.ListChangeListener<NotificationService.Notification>) change -> {
            actualizarBadgeNotificaciones();
        });
        
        // Actualizar badge inicial
        actualizarBadgeNotificaciones();
        
        // ELIMINADO: Notificación de bienvenida innecesaria
        // Solo notificaciones importantes de ahora en adelante
        
        System.out.println("🔔 Sistema de notificaciones configurado");
    }
    
    /**
     * Configura el logo inicial del header
     */
    private void setupLogo() {
        actualizarLogo();
        System.out.println("🎨 Logo del header configurado");
    }
    
    /**
     * Actualiza el logo del header según la configuración
     */
    private void actualizarLogo() {
        if (logoContainer != null && headerLogo != null && headerLogoImage != null) {
            AppConfigService configService = AppConfigService.getInstance();
            var config = configService.getConfiguracion();
            
            if (config.isLogoPersonalizado()) {
                // Intentar cargar logo personalizado desde archivo
                try {
                    java.io.File logoFile = new java.io.File("config/logo-personalizado.png");
                    if (logoFile.exists()) {
                        // Cargar y mostrar imagen personalizada
                        Image logoImage = new Image(logoFile.toURI().toString());
                        headerLogoImage.setImage(logoImage);
                        headerLogoImage.setVisible(true);
                        headerLogo.setVisible(false);
                        System.out.println("✅ Logo personalizado cargado desde: " + logoFile.getPath());
                    } else {
                        // Si no existe el archivo, mostrar logo por defecto
                        mostrarLogoDefault();
                        System.out.println("⚠️ Archivo de logo personalizado no encontrado, usando logo por defecto");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error cargando logo personalizado: " + e.getMessage());
                    mostrarLogoDefault();
                }
            } else {
                // Usar logo por defecto
                mostrarLogoDefault();
            }
            
            System.out.println("🔄 Logo del header actualizado: " + (config.isLogoPersonalizado() ? "Personalizado" : "Por defecto"));
        }
    }
    
    /**
     * Muestra el logo por defecto (icono de libro)
     */
    private void mostrarLogoDefault() {
        if (headerLogo != null && headerLogoImage != null) {
            headerLogo.setIconLiteral("fas-book");
            headerLogo.setIconColor(Color.web("#3B82F6"));
            headerLogo.setIconSize(24);
            headerLogo.setVisible(true);
            headerLogoImage.setVisible(false);
        }
    }
    

    
    /**
     * Actualiza el badge de notificaciones no leídas
     */
    private void actualizarBadgeNotificaciones() {
        javafx.application.Platform.runLater(() -> {
            int unreadCount = notificationService.getUnreadNotifications().size();
            
            if (unreadCount > 0) {
                notificationBadge.setText(String.valueOf(unreadCount));
                notificationBadge.setVisible(true);
                // Agregar efecto de pulso para notificaciones no leídas
                notificationBadge.getStyleClass().add("pulse-animation");
                notificationButton.getStyleClass().add("important-button");
            } else {
                notificationBadge.setVisible(false);
                notificationBadge.getStyleClass().remove("pulse-animation");
                notificationButton.getStyleClass().remove("important-button");
            }
        });
    }
    
    /**
     * Muestra el panel de notificaciones
     */
    @FXML
    private void handleShowNotifications() {
        try {
            if (notificationPopup != null && notificationPopup.isShowing()) {
                notificationPopup.hide();
                return;
            }
            
            // Cargar el panel de notificaciones
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/notifications-panel.fxml"));
            Parent notificationPanel = loader.load();
            
            // Crear popup
            notificationPopup = new Popup();
            notificationPopup.getContent().add(notificationPanel);
            notificationPopup.setAutoHide(true);
            notificationPopup.setHideOnEscape(true);
            
            // Posicionar el popup relativo al botón de notificaciones
            notificationPopup.show(notificationButton, 
                notificationButton.localToScreen(notificationButton.getBoundsInLocal()).getMinX() - 300,
                notificationButton.localToScreen(notificationButton.getBoundsInLocal()).getMaxY() + 5
            );
            
            System.out.println("🔔 Panel de notificaciones mostrado");
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando panel de notificaciones: " + e.getMessage());
            mostrarError("Error", "No se pudo cargar el panel de notificaciones");
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Maneja el cierre de sesión
     */
    @FXML
    private void handleLogout() {
        try {
            // Cerrar sesión
            authService.logout();
            
            // Volver a la pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/login-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 500, 650);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/example/demo2/styles/login.css").toExternalForm());
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setTitle("BiblioSystem - Iniciar Sesión");
            
            // Desactivar maximizado y restaurar tamaño normal
            stage.setMaximized(false);
            stage.setResizable(false);
            
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Mostrar error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al cerrar sesión");
            alert.setContentText("No se pudo volver a la pantalla de login.");
            alert.showAndWait();
        }
    }
    

} 