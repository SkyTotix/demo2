package com.example.demo2.controller;

// Modelo de enumeración que define los tipos de usuario del sistema
import com.example.demo2.models.enums.TipoUsuario;

// Servicios del sistema que proporcionan lógica de negocio
import com.example.demo2.service.AppConfigService;    // Configuraciones de la aplicación
import com.example.demo2.service.AuthService;        // Gestión de autenticación y sesiones
import com.example.demo2.service.NotificationService; // Sistema de notificaciones
import com.example.demo2.service.UsuarioService;      // Operaciones CRUD de usuarios
import com.example.demo2.service.LibroService;        // Operaciones CRUD de libros
import com.example.demo2.service.PrestamoService;        // Operaciones CRUD de préstamos

// Controladores del sistema
import com.example.demo2.controller.SystemConfigController; // Controlador de configuraciones

// Utilidades del sistema
import com.example.demo2.utils.IconHelper;
import com.example.demo2.utils.AnimationUtils;

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
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.application.Platform;

// Frameworks de estilos para interfaz moderna
import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import com.example.demo2.models.Prestamo;

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
 * - Menú: Dashboard, Usuarios, Préstamos, Libros
 * - Acciones: Nuevo Usuario, Nuevo Préstamo, Devolución, Gestión de Libros
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
    // @FXML private Label welcomeLabel;           // Título de bienvenida personalizado - Comentado: no existe en FXML
    @FXML private Label dashboardSubtitle;      // Subtítulo descriptivo del rol

    // @FXML private FlowPane quickActionsPane;   // Panel de acciones rápidas - Comentado: no existe en FXML
    
    // ESTADÍSTICAS - Elementos de la sección de estadísticas
    @FXML private VBox statisticsSection;           // Sección completa de estadísticas
    @FXML private Label lblProximosVencer;          // Contador de préstamos próximos a vencer
    @FXML private Label lblConMulta;                // Contador de usuarios con multa
    @FXML private Label lblUnaExistencia;           // Contador de libros con una existencia
    @FXML private ListView<String> listProximosVencer;  // Lista de préstamos próximos a vencer
    @FXML private ListView<String> listConMulta;        // Lista de usuarios con multa
    @FXML private ListView<String> listUnaExistencia;   // Lista de libros con una existencia
    @FXML private Button btnVerProximosVencer;  // Botón para ver detalles de próximos a vencer
    @FXML private Button btnVerConMulta;        // Botón para ver detalles de usuarios con multa
    @FXML private Button btnVerUnaExistencia;   // Botón para ver detalles de libros únicos

    
    // === SERVICIOS DEL SISTEMA ===
    private AuthService authService;            // Gestión de autenticación y usuario actual
    private NotificationService notificationService; // Sistema de notificaciones
    private UsuarioService usuarioService;      // Operaciones CRUD de usuarios
    private LibroService libroService;          // Operaciones CRUD de libros
    private PrestamoService prestamoService;    // Operaciones CRUD de préstamos
    private Popup notificationPopup;            // Popup para mostrar notificaciones
    
    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        notificationService = NotificationService.getInstance();
        usuarioService = UsuarioService.getInstance();
        libroService = LibroService.getInstance();
        prestamoService = PrestamoService.getInstance();
        
        // Configurar información del usuario
        setupUserInfo();
        
        // Configurar menú según el rol
        setupMenuByRole();
        
        // Configurar dashboard
        setupDashboard();
        
        // Configurar estadísticas según el rol
        setupStatistics();
        
        // Configurar acciones rápidas - COMENTADO: quickActionsPane no existe en FXML
        // setupQuickActions();
        
        // Configurar notificaciones
        setupNotifications();
        
        // Configurar logo personalizable
        setupLogo();
        
        // Aplicar animaciones de entrada después de que la UI esté completamente cargada
        Platform.runLater(this::applyEnhancedEntranceAnimations);
        
        // Configurar efectos de hover mejorados para elementos interactivos
        setupEnhancedInteractions();
    }
    
    /**
     * Configura la información del usuario en el header
     */
    private void setupUserInfo() {
        if (authService.getUsuarioActual() != null) {
            userNameLabel.setText(authService.getUsuarioActual().getNombreCompleto());
            userRoleLabel.setText(authService.getUsuarioActual().getTipoUsuario().getDescripcion());
            // welcomeLabel.setText("¡Bienvenido, " + authService.getUsuarioActual().getNombre() + "!"); // Comentado: welcomeLabel no existe en FXML
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
                // No agregar más items para simplificar
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
                addMenuItem(IconHelper.getBookIcon(), "Libros", "libros", false);
    
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
        
        // Comentado: dashboardSubtitle eliminado del FXML
        /*switch (tipoUsuario) {
            case SUPERADMIN:
                dashboardSubtitle.setText("Panel de administración del sistema - Control total");
                break;
            case ADMIN:
                dashboardSubtitle.setText("Panel de administración de biblioteca - Gestión completa");
                break;
            case BIBLIOTECARIO:
                dashboardSubtitle.setText("Panel de operaciones bibliotecarias - Gestión diaria");
                break;
        }*/
        
        // Proteger el título de clicks accidentales
        protegerTituloDashboard();
    }
    
    /**
     * Protege el título y subtítulo del dashboard contra eventos de click accidentales
     */
    private void protegerTituloDashboard() {
        // Asegurar que el título y subtítulo mantengan sus propiedades
        // welcomeLabel.setMouseTransparent(true); // Evitar que reciba eventos de mouse - Comentado: welcomeLabel no existe
        // dashboardSubtitle.setMouseTransparent(true); // Proteger también el subtítulo - Comentado: dashboardSubtitle eliminado del FXML
        
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
        /*if (welcomeLabel.getParent() != null) {
            welcomeLabel.getParent().setOnMouseClicked(event -> {
                // Consumir el evento para que no afecte los títulos
                event.consume();
            });
        }*/ // Comentado: welcomeLabel no existe
        

        
        System.out.println("🔒 Protección del título y subtítulo del dashboard activada");
    }
    
    /**
     * Restaura el título y subtítulo del dashboard a su estado correcto
     */
    private void restaurarTituloDashboard() {
        /*if (welcomeLabel != null) {
            // Asegurar que el estilo CSS se mantenga para el título
            welcomeLabel.getStyleClass().clear();
            welcomeLabel.getStyleClass().add("dashboard-title");
            
            // Agregar estilo inline como respaldo para el título
            welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'System Bold';");
        }*/ // Comentado: welcomeLabel no existe
        
        /*if (dashboardSubtitle != null) {
            // Asegurar que el estilo CSS se mantenga para el subtítulo
            dashboardSubtitle.getStyleClass().clear();
            dashboardSubtitle.getStyleClass().add("dashboard-subtitle");
            
            // Agregar estilo inline como respaldo para el subtítulo
            dashboardSubtitle.setStyle("-fx-font-size: 14px; -fx-font-family: 'System';");
        }*/ // Comentado: dashboardSubtitle eliminado del FXML
    }
    

    

    
    /**
     * Configura las acciones rápidas - COMENTADO: quickActionsPane no existe en FXML
     */
    /*
    private void setupQuickActions() {
        quickActionsPane.getChildren().clear();
        
        TipoUsuario tipoUsuario = authService.getUsuarioActual().getTipoUsuario();
        
        switch (tipoUsuario) {
            case SUPERADMIN:
                addQuickAction(IconHelper.getNewAdminIcon(), "Nuevo Admin", "nuevo-admin");
                // Remover las otras acciones para simplificar el dashboard
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
     * Agrega un botón de acción rápida con icono - COMENTADO: quickActionsPane no existe en FXML
     */
    /*
    private void addQuickAction(FontIcon icon, String text, String action) {
        Button actionButton = new Button(text);
        actionButton.setGraphic(icon);
        actionButton.getStyleClass().add("quick-action-button");
        actionButton.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
        actionButton.setOnAction(e -> handleQuickAction(action));
        
        quickActionsPane.getChildren().add(actionButton);
    }
    */
    

    
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
            case "buscar-libros":
                // Redirigir a la gestión completa de libros
                cargarVistaLibros();
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
        
        // Aplicar efecto de carga completada y actualizar estadísticas solo si aplica
        javafx.application.Platform.runLater(() -> {
            contentScrollPane.getStyleClass().remove("content-transition");
            contentScrollPane.getStyleClass().add("content-loaded");
            
            // Recargar estadísticas solo para ADMIN
            TipoUsuario tipoUsuario = authService.getUsuarioActual().getTipoUsuario();
            if (tipoUsuario == TipoUsuario.ADMIN && 
                statisticsSection != null && statisticsSection.isVisible()) {
                cargarEstadisticasInteractivas();
            }
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
     * Carga la vista de configuraciones del sistema
     */
    private void cargarVistaConfiguraciones() {
        try {
            // Agregar efecto de transición
            contentScrollPane.getStyleClass().add("content-transition");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/simple-config-view.fxml"));
            Parent configView = loader.load();
            
            // Obtener el controlador y pasar referencia del MainController
            SystemConfigController configController = loader.getController();
            if (configController != null) {
                configController.setMainController(this);
                System.out.println("🔗 Referencia de MainController establecida en SystemConfigController");
            }
            
            // Reemplazar el contenido del ScrollPane con la vista de configuraciones
            contentScrollPane.setContent(configView);
            
            // Aplicar efecto de carga completada
            javafx.application.Platform.runLater(() -> {
                contentScrollPane.getStyleClass().remove("content-transition");
                contentScrollPane.getStyleClass().add("content-loaded");
            });
            
            System.out.println("⚙️ Vista de configuraciones del sistema cargada en el dashboard");
            
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
     * Configura las estadísticas según el rol del usuario
     */
    private void setupStatistics() {
        TipoUsuario tipoUsuario = authService.getUsuarioActual().getTipoUsuario();
        
        // Solo mostrar estadísticas para ADMIN (no para SUPERADMIN para simplificar)
        if (tipoUsuario == TipoUsuario.ADMIN) {
            if (statisticsSection != null) {
                statisticsSection.setVisible(true);
                statisticsSection.setManaged(true);
                
                // Configurar event handlers para doble clic en las listas
                configurarEventosListas();
                
                // Cargar estadísticas iniciales
                cargarEstadisticasInteractivas();
            }
        } else {
            if (statisticsSection != null) {
                statisticsSection.setVisible(false);
                statisticsSection.setManaged(false);
            }
        }
    }
    
    /**
     * Carga las estadísticas rápidas para mostrar en el dashboard
     */
    private void cargarEstadisticasRapidas() {
        // Cargar en hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Obtener estadísticas de préstamos
                var statsP = prestamoService.obtenerEstadisticasRapidas();
                
                // Obtener estadísticas de libros
                var statsL = libroService.obtenerEstadisticasRapidas();
                
                // Actualizar UI en JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    if (lblProximosVencer != null) {
                        lblProximosVencer.setText(String.valueOf(statsP.prestamosProximosAVencer));
                    }
                    if (lblConMulta != null) {
                        lblConMulta.setText(String.valueOf(statsP.prestamosConMulta));
                    }
                    if (lblUnaExistencia != null) {
                        lblUnaExistencia.setText(String.valueOf(statsL.librosUnaExistencia));
                    }
                    
                    System.out.println("📊 Estadísticas del dashboard actualizadas");
                });
                
            } catch (Exception e) {
                System.err.println("❌ Error cargando estadísticas rápidas: " + e.getMessage());
                
                // Mostrar valores por defecto en caso de error
                javafx.application.Platform.runLater(() -> {
                    if (lblProximosVencer != null) lblProximosVencer.setText("--");
                    if (lblConMulta != null) lblConMulta.setText("--");
                    if (lblUnaExistencia != null) lblUnaExistencia.setText("--");
                });
            }
        }).start();
    }
    
    /**
     * Configura los event handlers para las listas de estadísticas
     */
    private void configurarEventosListas() {
        System.out.println("⚙️ Configurando eventos de listas de estadísticas...");
        
        if (listProximosVencer == null) {
            System.err.println("⚠️ listProximosVencer es null!");
        } else {
            System.out.println("✅ Configurando handler para listProximosVencer");
            listProximosVencer.setOnMouseClicked(event -> {
                System.out.println("🖱️ Evento de clic detectado en listProximosVencer (clics: " + event.getClickCount() + ")");
                if (event.getClickCount() == 2) {
                    String seleccionado = listProximosVencer.getSelectionModel().getSelectedItem();
                    if (seleccionado != null) {
                        System.out.println("🔍 Mostrando detalle para: " + seleccionado);
                        mostrarDetallePrestamoProximoVencer(seleccionado);
                    } else {
                        System.out.println("⚠️ No hay ítem seleccionado");
                    }
                }
            });
        }
        
        if (listConMulta == null) {
            System.err.println("⚠️ listConMulta es null!");
        } else {
            System.out.println("✅ Configurando handler para listConMulta");
            listConMulta.setOnMouseClicked(event -> {
                System.out.println("🖱️ Evento de clic detectado en listConMulta (clics: " + event.getClickCount() + ")");
                if (event.getClickCount() == 2) {
                    String seleccionado = listConMulta.getSelectionModel().getSelectedItem();
                    if (seleccionado != null) {
                        System.out.println("🔍 Mostrando detalle para: " + seleccionado);
                        mostrarDetallePrestamoConMulta(seleccionado);
                    } else {
                        System.out.println("⚠️ No hay ítem seleccionado");
                    }
                }
            });
        }
        
        if (listUnaExistencia == null) {
            System.err.println("⚠️ listUnaExistencia es null!");
        } else {
            System.out.println("✅ Configurando handler para listUnaExistencia");
            listUnaExistencia.setOnMouseClicked(event -> {
                System.out.println("🖱️ Evento de clic detectado en listUnaExistencia (clics: " + event.getClickCount() + ")");
                if (event.getClickCount() == 2) {
                    String seleccionado = listUnaExistencia.getSelectionModel().getSelectedItem();
                    if (seleccionado != null) {
                        System.out.println("🔍 Mostrando detalle para: " + seleccionado);
                        mostrarDetalleLibroUnaExistencia(seleccionado);
                    } else {
                        System.out.println("⚠️ No hay ítem seleccionado");
                    }
                }
            });
        }
    }
    
    /**
     * Carga las estadísticas interactivas del dashboard
     */
    private void cargarEstadisticasInteractivas() {
        new Thread(() -> {
            try {
                // Cargar préstamos próximos a vencer
                cargarProximosVencer();
                
                // Cargar préstamos con multa
                cargarConMulta();
                
                // Cargar libros con una existencia
                cargarUnaExistencia();
                
                System.out.println("📊 Estadísticas interactivas del dashboard cargadas");
                
            } catch (Exception e) {
                System.err.println("❌ Error cargando estadísticas interactivas: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Carga la lista de préstamos próximos a vencer
     */
    private void cargarProximosVencer() {
        try {
            var prestamos = prestamoService.obtenerPrestamosProximosAVencer(3);
            
            javafx.application.Platform.runLater(() -> {
                if (listProximosVencer != null && lblProximosVencer != null) {
                    listProximosVencer.getItems().clear();
                    
                    if (prestamos.isEmpty()) {
                        listProximosVencer.getItems().add("✨ No hay préstamos próximos a vencer");
                        lblProximosVencer.setText("(0)");
                    } else {
                        for (var prestamo : prestamos) {
                            String item = String.format("📅 %s - %s", 
                                prestamo.getLectorNombre(),
                                prestamo.getLibroTitulo());
                            listProximosVencer.getItems().add(item);
                        }
                        lblProximosVencer.setText("(" + prestamos.size() + ")");
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error cargando próximos a vencer: " + e.getMessage());
        }
    }
    
    /**
     * Carga la lista de préstamos con multa
     */
    private void cargarConMulta() {
        try {
            var prestamos = prestamoService.obtenerPrestamosConMulta();
            
            javafx.application.Platform.runLater(() -> {
                if (listConMulta != null && lblConMulta != null) {
                    listConMulta.getItems().clear();
                    
                    if (prestamos.isEmpty()) {
                        listConMulta.getItems().add("✨ No hay usuarios con multa");
                        lblConMulta.setText("(0)");
                    } else {
                        for (var prestamo : prestamos) {
                            String item = String.format("💰 %s - $%.2f", 
                                prestamo.getLectorNombre(),
                                prestamo.getMulta());
                            listConMulta.getItems().add(item);
                        }
                        lblConMulta.setText("(" + prestamos.size() + ")");
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error cargando con multa: " + e.getMessage());
        }
    }
    
    /**
     * Carga la lista de libros con una existencia
     */
    private void cargarUnaExistencia() {
        try {
            var libros = libroService.obtenerLibrosConUnaExistencia();
            
            javafx.application.Platform.runLater(() -> {
                if (listUnaExistencia != null && lblUnaExistencia != null) {
                    listUnaExistencia.getItems().clear();
                    
                    if (libros.isEmpty()) {
                        listUnaExistencia.getItems().add("✨ No hay libros con ejemplar único");
                        lblUnaExistencia.setText("(0)");
                    } else {
                        for (var libro : libros) {
                            String item = String.format("📖 %s - %s", 
                                libro.getTitulo(),
                                libro.getAutor());
                            listUnaExistencia.getItems().add(item);
                        }
                        lblUnaExistencia.setText("(" + libros.size() + ")");
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error cargando una existencia: " + e.getMessage());
        }
    }
    
    /**
     * Muestra detalle de un préstamo próximo a vencer
     */
    private void mostrarDetallePrestamoProximoVencer(String item) {
        // Extraer nombre del lector del texto
        String nombreLector = item.split(" - ")[0].replace("📅 ", "");
        
        new Thread(() -> {
            try {
                var prestamos = prestamoService.obtenerPrestamosProximosAVencer(3);
                var prestamo = prestamos.stream()
                    .filter(p -> p.getLectorNombre().equals(nombreLector))
                    .findFirst()
                    .orElse(null);
                
                if (prestamo != null) {
                    javafx.application.Platform.runLater(() -> {
                        mostrarDialogoDetallePrestamo(prestamo, "⏰ Detalle - Próximo a Vencer");
                    });
                }
            } catch (Exception e) {
                System.err.println("❌ Error mostrando detalle: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Muestra detalle de un préstamo con multa
     */
    private void mostrarDetallePrestamoConMulta(String item) {
        // Extraer nombre del lector del texto  
        String nombreLector = item.split(" - ")[0].replace("💰 ", "");
        
        new Thread(() -> {
            try {
                var prestamos = prestamoService.obtenerPrestamosConMulta();
                var prestamo = prestamos.stream()
                    .filter(p -> p.getLectorNombre().equals(nombreLector))
                    .findFirst()
                    .orElse(null);
                
                if (prestamo != null) {
                    javafx.application.Platform.runLater(() -> {
                        mostrarDialogoDetallePrestamo(prestamo, "💰 Detalle - Con Multa");
                    });
                }
            } catch (Exception e) {
                System.err.println("❌ Error mostrando detalle: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Muestra detalle de un libro con una existencia
     */
    private void mostrarDetalleLibroUnaExistencia(String item) {
        // Extraer título del libro del texto
        String titulo = item.split(" - ")[0].replace("📖 ", "");
        
        new Thread(() -> {
            try {
                var libros = libroService.obtenerLibrosConUnaExistencia();
                var libro = libros.stream()
                    .filter(l -> l.getTitulo().equals(titulo))
                    .findFirst()
                    .orElse(null);
                
                if (libro != null) {
                    javafx.application.Platform.runLater(() -> {
                        mostrarDialogoDetalleLibro(libro);
                    });
                }
            } catch (Exception e) {
                System.err.println("❌ Error mostrando detalle: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Muestra un diálogo con detalles completos de un préstamo
     */
    private void mostrarDialogoDetallePrestamo(Prestamo prestamo, String titulo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del Préstamo");
        alert.setHeaderText(titulo);
        
        StringBuilder content = new StringBuilder();
        content.append("📋 Información del préstamo:\n\n");
        content.append("• Código: ").append(prestamo.getCodigoPrestamo()).append("\n");
        content.append("• Lector: ").append(prestamo.getLectorNombre()).append(" (").append(prestamo.getLectorCodigo()).append(")\n");
        content.append("• Libro: ").append(prestamo.getLibroTitulo()).append(" (ISBN: ").append(prestamo.getLibroIsbn()).append(")\n");
        content.append("• Estado: ").append(prestamo.getEstado()).append("\n");
        content.append("• Fecha préstamo: ").append(prestamo.getFechaPrestamo().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        content.append("• Fecha devolución esperada: ").append(prestamo.getFechaDevolucionEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        
        if (prestamo.getFechaDevolucionReal() != null) {
            content.append("• Fecha devolución real: ").append(prestamo.getFechaDevolucionReal().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }
        
        content.append("• Multa: S/. ").append(String.format("%.2f", prestamo.getMulta())).append(" (").append(prestamo.isMultaPagada() ? "Pagada" : "Pendiente").append(")\n");
        content.append("• Bibliotecario préstamo: ").append(prestamo.getBibliotecarioPrestamoNombre()).append("\n");
        
        if (prestamo.getBibliotecarioDevolucionNombre() != null) {
            content.append("• Bibliotecario devolución: ").append(prestamo.getBibliotecarioDevolucionNombre()).append("\n");
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    /**
     * Muestra un diálogo con detalles completos de un libro
     */
    private void mostrarDialogoDetalleLibro(Object libro) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del Libro");
        alert.setHeaderText("📖 Libro con Ejemplar Único");
        
        StringBuilder content = new StringBuilder();
        // Aquí irían los detalles del libro cuando tengamos el objeto completo
        content.append("📚 Información del libro:\n\n");
        content.append("• Título: ").append("Título del libro").append("\n");
        content.append("• Autor: ").append("Autor del libro").append("\n");
        content.append("• ISBN: ").append("ISBN").append("\n");
        content.append("• Editorial: ").append("Editorial").append("\n");
        content.append("• Año: ").append("Año").append("\n");
        content.append("• Cantidad: 1 ejemplar (ÚNICO)\n\n");
        content.append("⚠️ IMPORTANTE: Este libro NO debe prestarse para preservar\n");
        content.append("el único ejemplar disponible en la biblioteca.");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    /**
     * =============================
     * MANEJADORES DE ESTADÍSTICAS
     * =============================
     */
    
    @FXML
    private void handleVerProximosVencer() {
        System.out.println("🔍 Mostrando préstamos próximos a vencer...");
        mostrarDialogoProximosVencer();
    }
    
    @FXML
    private void handleVerConMulta() {
        System.out.println("💰 Mostrando usuarios con multa...");
        mostrarDialogoConMulta();
    }
    
    @FXML
    private void handleVerUnaExistencia() {
        System.out.println("📖 Mostrando libros con una existencia...");
        mostrarDialogoUnaExistencia();
    }
    
    /**
     * Muestra un diálogo con los préstamos próximos a vencer
     */
    private void mostrarDialogoProximosVencer() {
        new Thread(() -> {
            try {
                var prestamos = prestamoService.obtenerPrestamosProximosAVencer(3);
                
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Préstamos Próximos a Vencer");
                    alert.setHeaderText("⏰ Préstamos que vencen en los próximos 3 días");
                    
                    if (prestamos.isEmpty()) {
                        alert.setContentText("¡Excelente! No hay préstamos próximos a vencer.");
                    } else {
                        StringBuilder content = new StringBuilder();
                        content.append("Se encontraron ").append(prestamos.size()).append(" préstamos próximos a vencer:\n\n");
                        
                        for (int i = 0; i < Math.min(prestamos.size(), 10); i++) {
                            var p = prestamos.get(i);
                            content.append("• ").append(p.getLectorNombre())
                                   .append(" - ").append(p.getLibroTitulo())
                                   .append(" (Vence: ").append(p.getFechaDevolucionEsperada()).append(")\n");
                        }
                        
                        if (prestamos.size() > 10) {
                            content.append("\n... y ").append(prestamos.size() - 10).append(" más.");
                        }
                        
                        content.append("\n\n💡 Considera enviar recordatorios a estos usuarios.");
                        alert.setContentText(content.toString());
                    }
                    
                    alert.showAndWait();
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudieron cargar los préstamos próximos a vencer: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Muestra un diálogo con los usuarios que tienen multa
     */
    private void mostrarDialogoConMulta() {
        new Thread(() -> {
            try {
                var prestamos = prestamoService.obtenerPrestamosConMulta();
                
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Usuarios con Multa");
                    alert.setHeaderText("💰 Usuarios que deben multa por retraso");
                    
                    if (prestamos.isEmpty()) {
                        alert.setContentText("¡Perfecto! No hay usuarios con multas pendientes.");
                    } else {
                        StringBuilder content = new StringBuilder();
                        content.append("Se encontraron ").append(prestamos.size()).append(" usuarios con multa:\n\n");
                        
                        double totalMulta = 0;
                        for (int i = 0; i < Math.min(prestamos.size(), 10); i++) {
                            var p = prestamos.get(i);
                            totalMulta += p.getMulta();
                            content.append("• ").append(p.getLectorNombre())
                                   .append(" - $").append(String.format("%.2f", p.getMulta()))
                                   .append(" (").append(p.getLibroTitulo()).append(")\n");
                        }
                        
                        if (prestamos.size() > 10) {
                            content.append("\n... y ").append(prestamos.size() - 10).append(" más.");
                        }
                        
                        content.append("\n\n💰 Total estimado en multas: $").append(String.format("%.2f", totalMulta));
                        content.append("\n⚠️ Gestionar cobro de multas pendientes.");
                        alert.setContentText(content.toString());
                    }
                    
                    alert.showAndWait();
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudieron cargar los préstamos con multa: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Muestra un diálogo con los libros que tienen una sola existencia
     */
    private void mostrarDialogoUnaExistencia() {
        new Thread(() -> {
            try {
                var libros = libroService.obtenerLibrosConUnaExistencia();
                
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Libros con Ejemplar Único");
                    alert.setHeaderText("📖 Libros que NO deben prestarse (solo 1 ejemplar)");
                    
                    if (libros.isEmpty()) {
                        alert.setContentText("No hay libros con ejemplar único en el catálogo.");
                    } else {
                        StringBuilder content = new StringBuilder();
                        content.append("Se encontraron ").append(libros.size()).append(" libros con ejemplar único:\n\n");
                        
                        for (int i = 0; i < Math.min(libros.size(), 15); i++) {
                            var libro = libros.get(i);
                            content.append("• ").append(libro.getTitulo())
                                   .append(" - ").append(libro.getAutor())
                                   .append(" (").append(libro.getIsbn()).append(")\n");
                        }
                        
                        if (libros.size() > 15) {
                            content.append("\n... y ").append(libros.size() - 15).append(" más.");
                        }
                        
                        content.append("\n\n⚠️ IMPORTANTE: Estos libros NO deben prestarse para preservar");
                        content.append("\nel ejemplar único de la biblioteca.");
                        content.append("\n💡 Considera adquirir más ejemplares de estos títulos.");
                        alert.setContentText(content.toString());
                    }
                    
                    alert.showAndWait();
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error", "No se pudieron cargar los libros con una existencia: " + e.getMessage());
                });
            }
        }).start();
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
        // Registrar listener para cambios de logo
        AppConfigService.addLogoChangeListener(this::onLogoChanged);
        System.out.println("🎨 Logo del header configurado");
    }
    
    /**
     * Método que se ejecuta cuando cambia el logo
     */
    private void onLogoChanged(String tipoLogo) {
        actualizarLogo(tipoLogo);
    }
    
    /**
     * Actualiza el logo del header según la configuración
     * @param tipoLogo Tipo de logo a actualizar ("app" o "login"). Si es null, se asume "app".
     */
    public void actualizarLogo(String tipoLogo) {
        // Si no se especifica tipo, asumir que es el logo de la aplicación
        if (tipoLogo == null || !tipoLogo.equals("login")) {
            tipoLogo = "app";
        }
        
        // Solo actualizar el logo de la aplicación en este controlador
        if (tipoLogo.equals("app") && logoContainer != null && headerLogo != null && headerLogoImage != null) {
            try {
                // Buscar logo personalizado
                java.io.File logoFile = new java.io.File("config/logo-app.png");
                
                if (logoFile.exists()) {
                    cargarLogoPersonalizadoHeader(logoFile);
                } else {
                    mostrarLogoDefault();
                }
                
            } catch (Exception e) {
                System.err.println("Error actualizando logo: " + e.getMessage());
                mostrarLogoDefault();
            }
        }
    }
    
    private void cargarLogoPersonalizadoHeader(java.io.File logoFile) {
        try {
            String logoUrl = logoFile.toURI().toURL().toExternalForm();
            Image logoImage = new Image(logoUrl);
            
            headerLogoImage.setImage(logoImage);
            headerLogoImage.setFitWidth(80);
            headerLogoImage.setFitHeight(80);
            headerLogoImage.setPreserveRatio(true);
            headerLogoImage.setVisible(true);
            headerLogo.setVisible(false);
            
            System.out.println("Logo de aplicación cargado: " + logoFile.getName());
            
        } catch (Exception e) {
            System.err.println("Error cargando logo de aplicación: " + e.getMessage());
            mostrarLogoDefault();
        }
    }
    
    /**
     * Método sobrecargado para mantener compatibilidad con código existente
     */
    public void actualizarLogo() {
        actualizarLogo("app");
    }
    
    /**
     * Muestra el logo por defecto (icono de libro)
     */
    private void mostrarLogoDefault() {
        if (headerLogo != null && headerLogoImage != null) {
            // SOLUCION: Asegurar que la actualización se ejecute en el hilo de JavaFX
            javafx.application.Platform.runLater(() -> {
                headerLogo.setIconLiteral("fas-book");
                headerLogo.setIconColor(Color.web("#3B82F6"));
                headerLogo.setIconSize(40);
                headerLogo.setVisible(true);
                headerLogoImage.setVisible(false);
                System.out.println("🔄 Logo por defecto actualizado en JavaFX Thread");
            });
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
            
            Scene scene = new Scene(root, 500, 710);
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
    
    // ===================================================================
    // MÉTODOS DE ANIMACIÓN AVANZADA
    // ===================================================================
    
    /**
     * Aplica animaciones de entrada mejoradas a todos los elementos de la interfaz
     * Utiliza técnicas avanzadas de JavaFX para crear transiciones suaves y profesionales
     */
    private void applyEnhancedEntranceAnimations() {
        try {
            // Lista de elementos principales para animación escalonada
            java.util.List<Node> mainElements = new java.util.ArrayList<>();
            
            // Agregar elementos principales si existen
            if (logoContainer != null) mainElements.add(logoContainer);
            if (userNameLabel != null) mainElements.add(userNameLabel);
            if (userRoleLabel != null) mainElements.add(userRoleLabel);
            if (menuContainer != null) mainElements.add(menuContainer);
            if (dashboardContainer != null) mainElements.add(dashboardContainer);
            if (statisticsSection != null) mainElements.add(statisticsSection);
            
            // Aplicar animación escalonada a elementos principales
            if (!mainElements.isEmpty()) {
                SequentialTransition staggered = AnimationUtils.staggeredAnimation(
                    mainElements, 
                    Duration.millis(100), 
                    AnimationUtils.AnimationType.FADE_IN, 
                    null
                );
                if (staggered != null) {
                    staggered.play();
                }
            }
            
            // Animación especial para el logo con efecto de rebote
            if (logoContainer != null) {
                Timeline logoAnimation = AnimationUtils.scaleIn(logoContainer, Duration.millis(600), () -> {
                    // Después del scale-in, aplicar un pequeño rebote
                    Timeline bounce = AnimationUtils.bounce(logoContainer, null);
                    if (bounce != null) {
                        bounce.play();
                    }
                });
                if (logoAnimation != null) {
                    logoAnimation.setDelay(Duration.millis(200));
                    logoAnimation.play();
                }
            }
            
            // Animación deslizante para el menú lateral
            if (menuContainer != null) {
                Timeline menuSlide = AnimationUtils.slideInLeft(menuContainer, Duration.millis(500), null);
                if (menuSlide != null) {
                    menuSlide.setDelay(Duration.millis(300));
                    menuSlide.play();
                }
            }
            
            // Animación de aparición para las estadísticas
            if (statisticsSection != null) {
                Timeline statsAnimation = AnimationUtils.slideInTop(statisticsSection, Duration.millis(400), null);
                if (statsAnimation != null) {
                    statsAnimation.setDelay(Duration.millis(500));
                    statsAnimation.play();
                }
            }
            
            // Animación de pulso para elementos importantes
            if (notificationButton != null) {
                Timeline pulseNotification = AnimationUtils.pulse(notificationButton, 2, null);
                if (pulseNotification != null) {
                    pulseNotification.setDelay(Duration.millis(1000));
                    pulseNotification.play();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error aplicando animaciones de entrada: " + e.getMessage());
        }
    }
    
    /**
     * Configura efectos de hover mejorados para elementos interactivos
     * Añade feedback visual sofisticado para mejorar la experiencia de usuario
     */
    private void setupEnhancedInteractions() {
        try {
            // Configurar hover mejorado para botones principales
            setupButtonHoverEffects(logoutButton);
            setupButtonHoverEffects(notificationButton);
            setupButtonHoverEffects(btnVerProximosVencer);
            setupButtonHoverEffects(btnVerConMulta);
            setupButtonHoverEffects(btnVerUnaExistencia);
            
            // Configurar hover para el logo
            if (logoContainer != null) {
                logoContainer.setOnMouseEntered(e -> AnimationUtils.enhancedHover(logoContainer, true));
                logoContainer.setOnMouseExited(e -> AnimationUtils.enhancedHover(logoContainer, false));
            }
            
            // Configurar efectos para las listas de estadísticas
            setupListHoverEffects(listProximosVencer);
            setupListHoverEffects(listConMulta);
            setupListHoverEffects(listUnaExistencia);
            
        } catch (Exception e) {
            System.err.println("Error configurando interacciones mejoradas: " + e.getMessage());
        }
    }
    
    /**
     * Configura efectos de hover para botones
     */
    private void setupButtonHoverEffects(Button button) {
        if (button != null) {
            button.setOnMouseEntered(e -> {
                AnimationUtils.enhancedHover(button, true);
                // Añadir efecto de pulso sutil
                Timeline pulse = AnimationUtils.pulse(button, 1, null);
                if (pulse != null) {
                    pulse.play();
                }
            });
            button.setOnMouseExited(e -> AnimationUtils.enhancedHover(button, false));
        }
    }
    
    /**
     * Configura efectos de hover para listas
     */
    private void setupListHoverEffects(ListView<?> listView) {
        if (listView != null) {
            listView.setOnMouseEntered(e -> {
                Timeline scaleUp = new Timeline(
                    new KeyFrame(Duration.millis(150),
                        new KeyValue(listView.scaleXProperty(), 1.02),
                        new KeyValue(listView.scaleYProperty(), 1.02)
                    )
                );
                scaleUp.play();
            });
            
            listView.setOnMouseExited(e -> {
                Timeline scaleDown = new Timeline(
                    new KeyFrame(Duration.millis(150),
                        new KeyValue(listView.scaleXProperty(), 1.0),
                        new KeyValue(listView.scaleYProperty(), 1.0)
                    )
                );
                scaleDown.play();
            });
        }
    }
    
    /**
     * Aplica transición suave al cambiar de vista
     * Utiliza crossfade para transiciones elegantes entre contenidos
     */
    private void applyPageTransition(Node newContent) {
        if (contentArea != null && newContent != null) {
            try {
                // Si hay contenido actual, hacer crossfade
                if (!contentArea.getChildren().isEmpty()) {
                    Node currentContent = contentArea.getChildren().get(0);
                    
                    // Crear transición cruzada
                    ParallelTransition crossFade = AnimationUtils.crossFade(
                        currentContent, 
                        newContent, 
                        Duration.millis(300), 
                        () -> {
                            // Limpiar y establecer nuevo contenido
                            contentArea.getChildren().clear();
                            contentArea.getChildren().add(newContent);
                        }
                    );
                    
                    if (crossFade != null) {
                        // Añadir el nuevo contenido temporalmente para la animación
                        contentArea.getChildren().add(newContent);
                        crossFade.play();
                    } else {
                        // Fallback sin animación
                        contentArea.getChildren().clear();
                        contentArea.getChildren().add(newContent);
                    }
                } else {
                    // Primera carga, solo fade in
                    contentArea.getChildren().add(newContent);
                    Timeline fadeIn = AnimationUtils.fadeIn(newContent, Duration.millis(300), null);
                    if (fadeIn != null) {
                        fadeIn.play();
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error en transición de página: " + e.getMessage());
                // Fallback sin animación
                contentArea.getChildren().clear();
                contentArea.getChildren().add(newContent);
            }
        }
    }
    
    /**
     * Aplica animación de feedback para acciones exitosas
     */
    private void showSuccessFeedback(Node element) {
        if (element != null) {
            Timeline bounce = AnimationUtils.bounce(element, null);
            if (bounce != null) {
                bounce.play();
            }
        }
    }
    
    /**
     * Aplica animación de feedback para errores
     */
    private void showErrorFeedback(Node element) {
        if (element != null) {
            Timeline shake = AnimationUtils.shake(element, null);
            if (shake != null) {
                shake.play();
            }
        }
    }
    
    /**
     * Aplica animación de carga para operaciones asíncronas
     */
    private RotateTransition showLoadingAnimation(Node element) {
        if (element != null) {
            return AnimationUtils.createSpinner(element);
        }
        return null;
    }
    
    /**
     * Detiene todas las animaciones de un elemento
     */
    private void stopAnimations(Node element) {
        AnimationUtils.stopAllAnimations(element);
    }
}