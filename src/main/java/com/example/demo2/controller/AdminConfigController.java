package com.example.demo2.controller;

import com.example.demo2.service.AppConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para las configuraciones del administrador
 * Permite personalizar colores y logo de la aplicación
 */
public class AdminConfigController {
    
    // === ELEMENTOS DE LA INTERFAZ ===
    
    // Logo
    @FXML private StackPane logoPreview;
    @FXML private FontIcon currentLogoIcon;
    @FXML private ImageView customLogoImage;
    @FXML private RadioButton rbLogoDefault;
    @FXML private RadioButton rbLogoCustom;
    @FXML private ToggleGroup logoGroup;
    @FXML private Button btnAplicarLogo;
    @FXML private Button btnVistaPrevia;
    @FXML private Button btnSeleccionarArchivo;
    
    // Información
    @FXML private Label lblEstadoConfig;
    @FXML private Label lblUltimaModificacion;
    @FXML private Label lblMensaje;
    
    // Acciones
    @FXML private Button btnGuardarConfig;
    @FXML private Button btnCancelar;
    
    // === SERVICIOS ===
    private AppConfigService configService;
    
    // === VARIABLES DE CONTROL ===
    private File archivoLogoSeleccionado;
    private String rutaLogoPersonalizado = "config/logo-personalizado.png";
    
    @FXML
    public void initialize() {
        configService = AppConfigService.getInstance();
        
        // Inicializar grupo de radio buttons
        logoGroup = new ToggleGroup();
        rbLogoDefault.setToggleGroup(logoGroup);
        rbLogoCustom.setToggleGroup(logoGroup);
        
        // Cargar configuración actual
        cargarConfiguracionActual();
        
        // Configurar listeners
        configurarListeners();
        
        System.out.println("✅ Controlador de configuraciones inicializado");
    }
    
    /**
     * Carga la configuración actual en los controles
     */
    private void cargarConfiguracionActual() {
        var config = configService.getConfiguracion();
        
        // Cargar configuración de logo
        if (config.isLogoPersonalizado()) {
            rbLogoCustom.setSelected(true);
            mostrarLogoPersonalizado();
        } else {
            rbLogoDefault.setSelected(true);
            mostrarLogoDefault();
        }
        
        // Actualizar información
        lblEstadoConfig.setText(config.isLogoPersonalizado() ? "Logo Personalizado" : "Logo Por Defecto");
        if (config.getUltimaModificacion() != null) {
            lblUltimaModificacion.setText(config.getUltimaModificacion().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
    }
    
    /**
     * Configura los listeners para cambios en tiempo real
     */
    private void configurarListeners() {
        // Listeners para cambios de color en tiempo real - SIN auto-aplicar
        // Los colores se aplicarán solo cuando se presione "Aplicar"
        
        // Configurar botón de seleccionar archivo
        btnSeleccionarArchivo.setOnAction(e -> seleccionarArchivoLogo());
        
        // Configurar ImageView para logo personalizado
        if (customLogoImage != null) {
            customLogoImage.setVisible(false);
            customLogoImage.setFitWidth(48);
            customLogoImage.setFitHeight(48);
            customLogoImage.setPreserveRatio(true);
        }
    }
    

    
    // === MÉTODOS PARA LOGO ===
    
    @FXML
    private void cambiarLogoDefault() {
        mostrarLogoDefault();
        mostrarMensaje("Logo por defecto seleccionado", "info");
    }
    
    @FXML
    private void cambiarLogoCustom() {
        mostrarLogoPersonalizado();
        mostrarMensaje("Logo personalizado seleccionado", "info");
    }
    
    @FXML
    private void aplicarLogo() {
        boolean logoPersonalizado = rbLogoCustom.isSelected();
        
        if (logoPersonalizado && archivoLogoSeleccionado != null) {
            // Guardar imagen personalizada
            try {
                // Crear directorio config si no existe
                File configDir = new File("config");
                if (!configDir.exists()) {
                    configDir.mkdirs();
                }
                
                // Copiar archivo seleccionado a la carpeta config
                File destino = new File(rutaLogoPersonalizado);
                Files.copy(archivoLogoSeleccionado.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                mostrarMensaje("✅ Logo personalizado guardado y aplicado", "success");
                
            } catch (IOException e) {
                mostrarMensaje("❌ Error guardando logo: " + e.getMessage(), "error");
                System.err.println("Error guardando logo: " + e.getMessage());
                return;
            }
        }
        
        configService.setLogoPersonalizado(logoPersonalizado);
        
        // Aplicar cambio en la aplicación principal
        AppConfigService.notificarCambioLogo();
        
        String mensaje = logoPersonalizado ? "Logo personalizado aplicado" : "Logo por defecto aplicado";
        mostrarMensaje("✅ " + mensaje, "success");
        actualizarEstadoConfiguracion();
    }
    
    @FXML
    private void vistaPrevia() {
        mostrarMensaje("Vista previa actualizada", "info");
        // La vista previa ya se actualiza automáticamente con los radio buttons
    }
    
    /**
     * Abre el selector de archivos para elegir logo personalizado
     */
    private void seleccionarArchivoLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Logo Personalizado");
        
        // Configurar filtros para imágenes
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
            "Imágenes (*.png, *.jpg, *.jpeg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);
        
        // Configurar directorio inicial
        File initialDir = new File(System.getProperty("user.home") + "/Desktop");
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        
        // Mostrar el diálogo
        Stage stage = (Stage) btnSeleccionarArchivo.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            archivoLogoSeleccionado = selectedFile;
            
            // Mostrar vista previa inmediata
            try {
                Image previewImage = new Image(selectedFile.toURI().toString());
                if (customLogoImage != null) {
                    customLogoImage.setImage(previewImage);
                    customLogoImage.setVisible(true);
                    currentLogoIcon.setVisible(false);
                }
                
                // Seleccionar automáticamente la opción de logo personalizado
                rbLogoCustom.setSelected(true);
                
                mostrarMensaje("✅ Logo seleccionado: " + selectedFile.getName(), "success");
                
            } catch (Exception e) {
                mostrarMensaje("❌ Error cargando imagen: " + e.getMessage(), "error");
                System.err.println("Error cargando imagen: " + e.getMessage());
            }
        }
    }
    
    private void mostrarLogoDefault() {
        currentLogoIcon.setIconLiteral("fas-book");
        currentLogoIcon.setIconColor(Color.web("#3B82F6"));
        currentLogoIcon.setIconSize(48);
    }
    
    private void mostrarLogoPersonalizado() {
        // Verificar si hay un archivo seleccionado recientemente
        if (archivoLogoSeleccionado != null && customLogoImage != null) {
            try {
                // Mostrar la imagen seleccionada
                Image logoImage = new Image(archivoLogoSeleccionado.toURI().toString());
                customLogoImage.setImage(logoImage);
                customLogoImage.setVisible(true);
                currentLogoIcon.setVisible(false);
                return;
            } catch (Exception e) {
                System.err.println("Error cargando archivo seleccionado: " + e.getMessage());
            }
        }
        
        // Verificar si existe logo personalizado guardado
        File logoFile = new File(rutaLogoPersonalizado);
        if (logoFile.exists() && customLogoImage != null) {
            try {
                Image logoImage = new Image(logoFile.toURI().toString());
                customLogoImage.setImage(logoImage);
                customLogoImage.setVisible(true);
                currentLogoIcon.setVisible(false);
            } catch (Exception e) {
                System.err.println("Error cargando logo personalizado guardado: " + e.getMessage());
                mostrarIconoPorDefectoPersonalizado();
            }
        } else {
            mostrarIconoPorDefectoPersonalizado();
        }
    }
    
    private void mostrarIconoPorDefectoPersonalizado() {
        currentLogoIcon.setIconLiteral("fas-building");
        currentLogoIcon.setIconColor(Color.web("#2563EB"));
        currentLogoIcon.setIconSize(48);
        currentLogoIcon.setVisible(true);
        if (customLogoImage != null) {
            customLogoImage.setVisible(false);
        }
    }
    
    // === MÉTODOS DE ACCIÓN ===
    
    @FXML
    private void guardarConfiguracion() {
        try {
            configService.guardarConfiguracion();
            mostrarMensaje("✅ Configuración guardada exitosamente", "success");
            actualizarEstadoConfiguracion();
        } catch (Exception e) {
            mostrarMensaje("❌ Error al guardar configuración: " + e.getMessage(), "error");
            System.err.println("Error guardando configuración: " + e.getMessage());
        }
    }
    
    @FXML
    private void cancelar() {
        // Mostrar confirmación si hay cambios sin guardar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar Configuración");
        confirmacion.setHeaderText("¿Cancelar cambios?");
        confirmacion.setContentText("Los cambios no guardados se perderán.");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Recargar configuración original
                configService.cargarConfiguracion();
                cargarConfiguracionActual();
                AppConfigService.notificarCambioLogo();
                mostrarMensaje("Cambios cancelados", "info");
            }
        });
    }
    
    // === MÉTODOS AUXILIARES ===
    
    /**
     * Convierte un Color de JavaFX a string hexadecimal
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
    

    
    /**
     * Actualiza el estado de la configuración
     */
    private void actualizarEstadoConfiguracion() {
        lblEstadoConfig.setText("Personalizada");
        lblUltimaModificacion.setText(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    
    /**
     * Muestra un mensaje al usuario
     */
    private void mostrarMensaje(String mensaje, String tipo) {
        lblMensaje.setText(mensaje);
        
        // Aplicar estilo según el tipo
        lblMensaje.getStyleClass().removeAll("success-message", "error-message", "info-message");
        switch (tipo) {
            case "success":
                lblMensaje.getStyleClass().add("success-message");
                break;
            case "error":
                lblMensaje.getStyleClass().add("error-message");
                break;
            case "info":
                lblMensaje.getStyleClass().add("info-message");
                break;
        }
        
        // Limpiar mensaje después de 3 segundos
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> lblMensaje.setText(""))
        );
        timeline.play();
    }
} 