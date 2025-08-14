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
        
        // Registrar listener para cambios de logo
        AppConfigService.addLogoChangeListener(this::onLogoChanged);
        
        // Inicializar base de datos en un hilo separado
        initializeDatabase();
        
        // Mostrar credenciales por defecto en desarrollo
        showDefaultCredentials();
    }
    
    private void initializeDatabase() {
        new Thread(() -> {
            try {
                // Verificación rápida de tablas sin logs verbosos
                boolean tablasExisten = com.example.demo2.service.DatabaseInitService.verificarTablasRapido();
                
                if (!tablasExisten) {
                    javafx.application.Platform.runLater(() -> 
                        showMessage("Configurando base de datos...", "info"));
                    
                    // Crear tablas básicas sin logs excesivos
                    com.example.demo2.service.DatabaseInitService.inicializarBaseDatosRapido();
                }
                
                // Solo crear usuario admin si no existe (esencial para login)
                authService.crearSuperAdminInicial();
                
                javafx.application.Platform.runLater(() -> 
                    showMessage("Sistema listo", "info"));
                
                // Crear datos de prueba en segundo plano (opcional)
                crearDatosPruebaEnSegundoPlano();
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> 
                    showMessage("Error inicializando sistema: " + e.getMessage(), "error"));
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Crea datos de prueba en segundo plano sin afectar el login
     */
    private void crearDatosPruebaEnSegundoPlano() {
        new Thread(() -> {
            try {
                // Esperar un poco para no interferir con el login
                Thread.sleep(2000);
                
                // Actualizar estructuras si es necesario
                com.example.demo2.service.DatabaseInitService.actualizarEstructuraUsuarios();
                com.example.demo2.service.DatabaseInitService.actualizarEstructuraPrestamos();
                
                // Crear datos de prueba
                com.example.demo2.service.UsuarioService.getInstance().crearUsuariosDePrueba();
                com.example.demo2.service.LectorService.getInstance().crearLectoresDePrueba();
                com.example.demo2.service.PrestamoService.getInstance().crearPrestamosDePrueba();
                
                System.out.println("✅ Datos de prueba creados en segundo plano");
                
            } catch (Exception e) {
                System.err.println("⚠️ Error creando datos de prueba: " + e.getMessage());
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
     * Configura el logo personalizable en el login - VERSIÓN SIMPLIFICADA
     */
    private void setupLoginLogo() {
        if (logoContainer == null || loginLogo == null || loginLogoImage == null) {
            return;
        }
        
        try {
            // Buscar logo personalizado en orden de prioridad
            String[] logoFiles = {
                "config/logo-login.png",
                "config/logo-app.png", 
                "config/logo-personalizado.png"
            };
            
            for (String logoPath : logoFiles) {
                java.io.File logoFile = new java.io.File(logoPath);
                if (logoFile.exists()) {
                    cargarLogoPersonalizado(logoFile);
                    return;
                }
            }
            
            // Si no se encuentra ningún logo personalizado, mostrar el por defecto
            mostrarLogoDefaultLogin();
            
        } catch (Exception e) {
            System.err.println("Error en setupLoginLogo: " + e.getMessage());
            mostrarLogoDefaultLogin();
        }
    }
    
    private void cargarLogoPersonalizado(java.io.File logoFile) {
        try {
            String logoUrl = logoFile.toURI().toURL().toExternalForm();
            Image logoImage = new Image(logoUrl);
            
            loginLogoImage.setImage(logoImage);
            loginLogoImage.setFitWidth(120);
            loginLogoImage.setFitHeight(120);
            loginLogoImage.setPreserveRatio(true);
            loginLogoImage.setVisible(true);
            loginLogo.setVisible(false);
            
            System.out.println("Logo personalizado cargado: " + logoFile.getName());
            
        } catch (Exception e) {
            System.err.println("Error cargando logo personalizado: " + e.getMessage());
            mostrarLogoDefaultLogin();
        }
    }
    
    /**
     * Muestra el logo por defecto en el login (icono de libro)
     */
    private void mostrarLogoDefaultLogin() {
        if (loginLogo != null && loginLogoImage != null) {
            // SOLUCION: Asegurar que la actualización se ejecute en el hilo de JavaFX
            javafx.application.Platform.runLater(() -> {
                loginLogo.setIconLiteral("fas-book");
                loginLogo.setIconColor(Color.web("#3B82F6"));
                loginLogo.setIconSize(50);
                loginLogo.setVisible(true);
                loginLogoImage.setVisible(false);
                System.out.println("🔄 Logo por defecto de login actualizado en JavaFX Thread");
            });
        }
    }
    
    /**
     * Método que se ejecuta cuando cambia el logo
     * @param tipoLogo Tipo de logo que cambió ("app" o "login")
     */
    private void onLogoChanged(String tipoLogo) {
        // Solo actualizar si es el logo de login
        if (tipoLogo.equals("login")) {
            setupLoginLogo();
            System.out.println("🔄 Logo de inicio de sesión actualizado por cambio de configuración");
        }
    }
}