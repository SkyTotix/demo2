package com.example.demo2.controller;

// Servicios del sistema
import com.example.demo2.service.AppConfigService;
import com.example.demo2.service.AuthService;

// Importaciones de JavaFX para la interfaz de usuario
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * CONTROLADOR DE PANTALLA DE LOGIN - PUNTO DE ENTRADA AL SISTEMA
 * =============================================================
 * 
 * Este controlador gestiona la primera interacción del usuario con BiblioSystem.
 * Es responsable de la autenticación, inicialización del sistema y redirección
 * a la aplicación principal según el rol del usuario autenticado.
 * 
 * RESPONSABILIDADES PRINCIPALES:
 * 1. Gestionar la interfaz de login (campos de usuario y contraseña)
 * 2. Validar credenciales a través del AuthService
 * 3. Inicializar la base de datos y crear estructuras necesarias
 * 4. Crear usuarios por defecto si no existen (SuperAdmin inicial)
 * 5. Manejar estados de carga y mensajes de error/éxito
 * 6. Redirigir a la aplicación principal tras autenticación exitosa
 * 
 * FLUJO DE AUTENTICACIÓN:
 * 1. Usuario ingresa credenciales -> 2. Validación con base de datos
 * 3. Si válido -> Cargar MainController -> 4. Dashboard específico por rol
 * 
 * INICIALIZACIÓN DEL SISTEMA:
 * - Verifica existencia de tablas en Oracle Cloud
 * - Crea estructura de base de datos si no existe
 * - Inicializa usuarios por defecto (admin/admin123)
 * - Configura datos de prueba para desarrollo
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Conectado con login-view.fxml (definición de interfaz)
 * - Utiliza AuthService para validación y gestión de sesiones
 * - Invoca DatabaseInitService para preparar el sistema
 * - Redirige a MainController tras login exitoso
 * - Aplica estilos definidos en styles/login.css
 */
public class LoginController {
    
    // === ELEMENTOS DE LA INTERFAZ DE USUARIO (FXML) ===
    @FXML
    private FontIcon loginLogo;            // Logo personalizable en el login
    
    @FXML
    private ImageView loginLogoImage;      // Logo personalizado con imagen
    
    @FXML
    private StackPane logoContainer;       // Contenedor del logo
    
    @FXML
    private TextField usernameField;        // Campo de texto para nombre de usuario
    
    @FXML
    private PasswordField passwordField;   // Campo de contraseña (oculta el texto)
    
    @FXML
    private Button loginButton;            // Botón principal de inicio de sesión
    
    @FXML
    private Label messageLabel;            // Etiqueta para mostrar mensajes al usuario
    
    @FXML
    private ProgressIndicator loadingIndicator;  // Indicador visual de carga/procesamiento
    
    // === SERVICIOS UTILIZADOS ===
    private AuthService authService;       // Servicio de autenticación y gestión de sesiones
    
    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        loadingIndicator.setVisible(false);
        
        // Configurar eventos
        loginButton.setOnAction(event -> handleLogin());
        
        // Permitir login con Enter
        usernameField.setOnAction(event -> handleLogin());
        passwordField.setOnAction(event -> handleLogin());
        
        // Configurar logo personalizable
        setupLoginLogo();
        
        // Inicializar base de datos en un hilo separado
        initializeDatabase();
        
        // Mostrar credenciales por defecto en desarrollo
        showDefaultCredentials();
    }
    
    private void initializeDatabase() {
        new Thread(() -> {
            try {
                javafx.application.Platform.runLater(() -> 
                    showMessage("Verificando base de datos...", "info"));
                
                // Primero listar las tablas existentes para debug
                com.example.demo2.service.DatabaseInitService.listarTablas();
                
                // Verificar si las tablas existen
                boolean tablasExisten = com.example.demo2.service.DatabaseInitService.verificarTablas();
                
                if (!tablasExisten) {
                    javafx.application.Platform.runLater(() -> 
                        showMessage("Creando estructura de base de datos...", "info"));
                    
                    // Intentar crear las tablas
                    com.example.demo2.service.DatabaseInitService.inicializarBaseDatos();
                    
                    // Verificar nuevamente
                    if (!com.example.demo2.service.DatabaseInitService.verificarTablas()) {
                        // Si aún no existen, forzar recreación
                        javafx.application.Platform.runLater(() -> 
                            showMessage("Forzando recreación de tablas...", "info"));
                        
                        com.example.demo2.service.DatabaseInitService.recrearTablas();
                    }
                }
                
                // Verificar una vez más antes de continuar
                if (com.example.demo2.service.DatabaseInitService.verificarTablas()) {
                    // Actualizar estructura de la tabla usuarios con nuevos campos
                    com.example.demo2.service.DatabaseInitService.actualizarEstructuraUsuarios();
                    
                    // Actualizar estructura de la tabla prestamos para corregir problemas de columnas
                    com.example.demo2.service.DatabaseInitService.actualizarEstructuraPrestamos();
                    
                    // Crear usuario super admin inicial si no existe
                    authService.crearSuperAdminInicial();
                    
                    // Crear usuarios de prueba con teléfonos
                    com.example.demo2.service.UsuarioService.getInstance().crearUsuariosDePrueba();
                    
                    // Crear lectores de prueba
                    com.example.demo2.service.LectorService.getInstance().crearLectoresDePrueba();
                    
                    // Crear préstamos de prueba
                    com.example.demo2.service.PrestamoService.getInstance().crearPrestamosDePrueba();
                    
                    javafx.application.Platform.runLater(() -> 
                        showMessage("Sistema listo", "info"));
                } else {
                    javafx.application.Platform.runLater(() -> 
                        showMessage("Error: No se pudieron crear las tablas", "error"));
                }
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> 
                    showMessage("Error inicializando sistema: " + e.getMessage(), "error"));
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Por favor ingrese usuario y contraseña", "error");
            return;
        }
        
        // Mostrar indicador de carga
        setLoading(true);
        
        // Realizar login en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                boolean success = authService.login(username, password);
                
                // Volver al hilo de JavaFX para actualizar la UI
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    
                    if (success) {
                        showMessage("Bienvenido " + authService.getUsuarioActual().getNombreCompleto(), "success");
                        openMainApplication();
                    } else {
                        showMessage("Usuario o contraseña incorrectos", "error");
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    showMessage("Error de conexión: " + e.getMessage(), "error");
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
    }
    
    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().clear();
        
        // Aplicar el estilo correcto según el tipo de mensaje
        switch (type.toLowerCase()) {
            case "success":
                messageLabel.getStyleClass().add("success-message");
                break;
            case "error":
                messageLabel.getStyleClass().add("error-message");
                break;
            case "info":
            default:
                messageLabel.getStyleClass().add("status-message");
                break;
        }
        
        messageLabel.setVisible(true);
    }
    
    private void openMainApplication() {
        try {
            // Cargar la pantalla principal según el rol del usuario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/main-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/example/demo2/styles/main.css").toExternalForm());
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("BiblioSystem - " + authService.getUsuarioActual().getTipoUsuario().getDescripcion());
            
            // Habilitar redimensionamiento y maximizar ventana
            stage.setResizable(true);
            stage.setScene(scene);
            stage.setMaximized(true);
            
        } catch (IOException e) {
            showMessage("Error al cargar la aplicación principal", "error");
            e.printStackTrace();
        }
    }
    
    private void showDefaultCredentials() {
        // Comentado para no mostrar credenciales por defecto
        // if (messageLabel != null) {
        //     showMessage("Usuario por defecto: admin / admin123", "info");
        // }
    }
    
    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recuperar Contraseña");
        alert.setHeaderText("Contacte al Administrador");
        alert.setContentText("Para recuperar su contraseña, contacte al administrador del sistema.");
        alert.showAndWait();
    }
    
    /**
     * Configura el logo personalizable en el login
     */
    private void setupLoginLogo() {
        if (logoContainer != null && loginLogo != null && loginLogoImage != null) {
            AppConfigService configService = AppConfigService.getInstance();
            var config = configService.getConfiguracion();
            
            if (config.isLogoPersonalizado()) {
                // Intentar cargar logo personalizado desde archivo fijo
                try {
                    java.io.File logoFile = new java.io.File(Paths.get("config", "logo-personalizado.png").toString());
                    if (logoFile.exists()) {
                        String logoUrl = logoFile.toURI().toURL().toExternalForm() + "?t=" + System.currentTimeMillis();
                        Image logoImage = new Image(logoUrl, true); // Cargar asincrónicamente
                        loginLogoImage.setImage(logoImage);
                        loginLogoImage.setFitWidth(50);
                        loginLogoImage.setFitHeight(50);
                        loginLogoImage.setPreserveRatio(true);
                        loginLogoImage.setVisible(true);
                        loginLogo.setVisible(false);
                        System.out.println("✅ Logo personalizado cargado en login desde: " + logoFile.getAbsolutePath());
                    } else {
                        // Si no existe el archivo, mostrar logo por defecto
                        mostrarLogoDefaultLogin();
                        System.out.println("⚠️ Archivo de logo personalizado no encontrado en login");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error cargando logo personalizado en login: " + e.getMessage());
                    mostrarLogoDefaultLogin();
                }
            } else {
                // Usar logo por defecto
                mostrarLogoDefaultLogin();
            }
            
            System.out.println("🎨 Logo del login configurado: " + (config.isLogoPersonalizado() ? "Personalizado" : "Por defecto"));
        }
    }
    
    /**
     * Muestra el logo por defecto en el login (icono de libro)
     */
    private void mostrarLogoDefaultLogin() {
        if (loginLogo != null && loginLogoImage != null) {
            loginLogo.setIconLiteral("fas-book");
            loginLogo.setIconColor(Color.web("#3B82F6"));
            loginLogo.setIconSize(50);
            loginLogo.setVisible(true);
            loginLogoImage.setVisible(false);
        }
    }
} 