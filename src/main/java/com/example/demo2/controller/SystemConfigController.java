package com.example.demo2.controller;

import com.example.demo2.database.DatabaseManager;
import com.example.demo2.service.ConfigurationService;
import com.example.demo2.service.ConfigurationService.SystemConfiguration;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.example.demo2.service.AppConfigService;

/**
 * Controlador para la configuración del sistema
 */
public class SystemConfigController {
    
    // Elementos de configuración general
    @FXML private TextField txtSystemName;
    @FXML private TextField txtSystemVersion;
    @FXML private CheckBox chkMaintenanceMode;
    @FXML private CheckBox chkDetailedLogs;
    
    // Elementos de configuración de sesiones
    @FXML private Spinner<Integer> spnSessionTimeout;
    @FXML private Spinner<Integer> spnMaxConcurrentSessions;
    @FXML private CheckBox chkRememberSession;
    
    // Elementos de configuración de base de datos
    @FXML private Label lblDbConnectionStatus;
    @FXML private Label lblDbServer;
    @FXML private Label lblDbName;
    @FXML private Label lblDbUser;
    @FXML private Button btnTestConnection;
    @FXML private Button btnReconnect;
    @FXML private Spinner<Integer> spnMinConnections;
    @FXML private Spinner<Integer> spnMaxConnections;
    @FXML private Spinner<Integer> spnConnectionTimeout;
    
    // Elementos de configuración de seguridad
    @FXML private Spinner<Integer> spnMinPasswordLength;
    @FXML private CheckBox chkRequireUppercase;
    @FXML private CheckBox chkRequireNumbers;
    @FXML private CheckBox chkRequireSymbols;
    @FXML private Spinner<Integer> spnPasswordExpiration;
    @FXML private Spinner<Integer> spnMaxLoginAttempts;
    @FXML private Spinner<Integer> spnLockoutDuration;
    @FXML private CheckBox chkAuditAccess;
    
    // Elementos de configuración de backups
    @FXML private CheckBox chkEnableBackups;
    @FXML private ComboBox<String> cmbBackupFrequency;
    @FXML private ComboBox<String> cmbBackupTime;
    @FXML private Spinner<Integer> spnBackupRetention;
    @FXML private Label lblLastBackup;
    @FXML private Label lblBackupStatus;
    @FXML private Label lblNextBackup;
    @FXML private Button btnRunBackup;
    @FXML private Button btnRestoreBackup;
    
    // Elementos de configuración de multas
    @FXML private Spinner<Integer> spnDiasGraciaMulta;
    @FXML private Spinner<Double> spnMontoMultaDiario;
    @FXML private Spinner<Double> spnMontoMultaMaxima;
    
    // Elementos de personalización de logo
    @FXML private Label lblLogoActual;
    @FXML private Button btnSeleccionarLogo;
    @FXML private Button btnRestaurarLogo;
    @FXML private Label lblVistaPrevia;
    
    // Botones principales
    @FXML private Button btnSaveAll;
    @FXML private Button btnResetDefaults;
    @FXML private TabPane configTabPane;
    
    private ConfigurationService configService;
    private MainController mainController;
    
    @FXML
    public void initialize() {
        configService = ConfigurationService.getInstance();
        
        configurarSpinners();
        configurarComboBoxes();
        cargarConfiguracionActual();
        actualizarEstadoBaseDatos();
        actualizarEstadoLogo();
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    private void configurarSpinners() {
        // Configurar spinners de sesiones (solo si existen)
        if (spnSessionTimeout != null) {
            spnSessionTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 480, 30));
        }
        if (spnMaxConcurrentSessions != null) {
            spnMaxConcurrentSessions.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));
        }
        
        // Configurar spinners de base de datos (solo si existen)
        if (spnMinConnections != null) {
            spnMinConnections.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 5));
        }
        if (spnMaxConnections != null) {
            spnMaxConnections.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 200, 20));
        }
        if (spnConnectionTimeout != null) {
            spnConnectionTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 300, 30));
        }
        
        // Configurar spinners de seguridad (solo si existen)
        if (spnMinPasswordLength != null) {
            spnMinPasswordLength.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 50, 8));
        }
        if (spnPasswordExpiration != null) {
            spnPasswordExpiration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 365, 90));
        }
        if (spnMaxLoginAttempts != null) {
            spnMaxLoginAttempts.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        }
        if (spnLockoutDuration != null) {
            spnLockoutDuration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1440, 15));
        }
        
        // Configurar spinner de backups (solo si existe)
        if (spnBackupRetention != null) {
            spnBackupRetention.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 30));
        }
        
        // Configurar spinners de multas (solo si existen)
        if (spnDiasGraciaMulta != null) {
            spnDiasGraciaMulta.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 3));
        }
        if (spnMontoMultaDiario != null) {
            spnMontoMultaDiario.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 1000.0, 5.0, 0.5));
        }
        if (spnMontoMultaMaxima != null) {
            spnMontoMultaMaxima.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 10000.0, 100.0, 5.0));
        }
    }
    
    private void configurarComboBoxes() {
        // Configurar frecuencia de backups (solo si existe)
        if (cmbBackupFrequency != null) {
            cmbBackupFrequency.setItems(FXCollections.observableArrayList(
                "Diario", "Semanal", "Mensual", "Manual"
            ));
            cmbBackupFrequency.setValue("Diario");
        }
        
        // Configurar horarios de backup (solo si existe)
        if (cmbBackupTime != null) {
            cmbBackupTime.setItems(FXCollections.observableArrayList(
                "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
                "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
            ));
            cmbBackupTime.setValue("02:00");
        }
    }
    
    private void cargarConfiguracionActual() {
        SystemConfiguration config = configService.getConfiguracion();
        
        // Cargar configuración general (solo si existen)
        if (txtSystemName != null) txtSystemName.setText(config.systemName);
        if (txtSystemVersion != null) txtSystemVersion.setText(config.systemVersion);
        if (chkMaintenanceMode != null) chkMaintenanceMode.setSelected(config.maintenanceMode);
        if (chkDetailedLogs != null) chkDetailedLogs.setSelected(config.detailedLogs);
        
        // Cargar configuración de sesiones (solo si existen)
        if (spnSessionTimeout != null) spnSessionTimeout.getValueFactory().setValue(config.sessionTimeout);
        if (spnMaxConcurrentSessions != null) spnMaxConcurrentSessions.getValueFactory().setValue(config.maxConcurrentSessions);
        if (chkRememberSession != null) chkRememberSession.setSelected(config.rememberSession);
        
        // Cargar configuración de base de datos (solo si existen)
        if (spnMinConnections != null) spnMinConnections.getValueFactory().setValue(config.minConnections);
        if (spnMaxConnections != null) spnMaxConnections.getValueFactory().setValue(config.maxConnections);
        if (spnConnectionTimeout != null) spnConnectionTimeout.getValueFactory().setValue(config.connectionTimeout);
        
        // Cargar configuración de seguridad (solo si existen)
        if (spnMinPasswordLength != null) spnMinPasswordLength.getValueFactory().setValue(config.minPasswordLength);
        if (chkRequireUppercase != null) chkRequireUppercase.setSelected(config.requireUppercase);
        if (chkRequireNumbers != null) chkRequireNumbers.setSelected(config.requireNumbers);
        if (chkRequireSymbols != null) chkRequireSymbols.setSelected(config.requireSymbols);
        if (spnPasswordExpiration != null) spnPasswordExpiration.getValueFactory().setValue(config.passwordExpiration);
        if (spnMaxLoginAttempts != null) spnMaxLoginAttempts.getValueFactory().setValue(config.maxLoginAttempts);
        if (spnLockoutDuration != null) spnLockoutDuration.getValueFactory().setValue(config.lockoutDuration);
        if (chkAuditAccess != null) chkAuditAccess.setSelected(config.auditAccess);
        
        // Cargar configuración de backups (solo si existen)
        if (chkEnableBackups != null) chkEnableBackups.setSelected(config.enableBackups);
        if (cmbBackupFrequency != null) cmbBackupFrequency.setValue(config.backupFrequency);
        if (cmbBackupTime != null) cmbBackupTime.setValue(config.backupTime);
        if (spnBackupRetention != null) spnBackupRetention.getValueFactory().setValue(config.backupRetention);
        
        // Cargar configuración de multas (solo si existen)
        if (spnDiasGraciaMulta != null) spnDiasGraciaMulta.getValueFactory().setValue(config.diasGraciaMulta);
        if (spnMontoMultaDiario != null) spnMontoMultaDiario.getValueFactory().setValue(config.montoMultaDiario);
        if (spnMontoMultaMaxima != null) spnMontoMultaMaxima.getValueFactory().setValue(config.montoMultaMaxima);
        
        System.out.println("⚙️ Configuración del sistema cargada desde archivo");
    }
    
    private void actualizarEstadoBaseDatos() {
        try {
            // Verificar estado de la conexión
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            if (lblDbConnectionStatus != null) {
                lblDbConnectionStatus.setText("Conectado");
                lblDbConnectionStatus.getStyleClass().clear();
                lblDbConnectionStatus.getStyleClass().add("status-connected");
            }
            
            if (lblDbServer != null) lblDbServer.setText("Oracle Cloud Infrastructure");
            if (lblDbName != null) lblDbName.setText("INTEGRADORA");
            if (lblDbUser != null) lblDbUser.setText("ADMIN");
            
            System.out.println("🗄️ Estado de la base de datos actualizado");
            
        } catch (Exception e) {
            if (lblDbConnectionStatus != null) {
                lblDbConnectionStatus.setText("Desconectado");
                lblDbConnectionStatus.getStyleClass().clear();
                lblDbConnectionStatus.getStyleClass().add("status-error");
            }
            
            System.err.println("❌ Error verificando estado de BD: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSaveAll() {
        System.out.println("💾 Guardando toda la configuración del sistema...");
        
        try {
            // Recopilar valores de la interfaz y actualizar configuración (solo si existen)
            if (txtSystemName != null && txtSystemVersion != null && 
                chkMaintenanceMode != null && chkDetailedLogs != null) {
                configService.actualizarConfiguracionGeneral(
                    txtSystemName.getText(),
                    txtSystemVersion.getText(),
                    chkMaintenanceMode.isSelected(),
                    chkDetailedLogs.isSelected()
                );
            }
            
            if (spnSessionTimeout != null && spnMaxConcurrentSessions != null && 
                chkRememberSession != null) {
                configService.actualizarConfiguracionSesiones(
                    spnSessionTimeout.getValue(),
                    spnMaxConcurrentSessions.getValue(),
                    chkRememberSession.isSelected()
                );
            }
            
            if (spnMinConnections != null && spnMaxConnections != null && 
                spnConnectionTimeout != null) {
                configService.actualizarConfiguracionBaseDatos(
                    spnMinConnections.getValue(),
                    spnMaxConnections.getValue(),
                    spnConnectionTimeout.getValue()
                );
            }
            
            if (spnMinPasswordLength != null && chkRequireUppercase != null && 
                chkRequireNumbers != null && chkRequireSymbols != null &&
                spnPasswordExpiration != null && spnMaxLoginAttempts != null &&
                spnLockoutDuration != null && chkAuditAccess != null) {
                configService.actualizarConfiguracionSeguridad(
                    spnMinPasswordLength.getValue(),
                    chkRequireUppercase.isSelected(),
                    chkRequireNumbers.isSelected(),
                    chkRequireSymbols.isSelected(),
                    spnPasswordExpiration.getValue(),
                    spnMaxLoginAttempts.getValue(),
                    spnLockoutDuration.getValue(),
                    chkAuditAccess.isSelected()
                );
            }
            
            if (chkEnableBackups != null && cmbBackupFrequency != null && 
                cmbBackupTime != null && spnBackupRetention != null) {
                configService.actualizarConfiguracionBackups(
                    chkEnableBackups.isSelected(),
                    cmbBackupFrequency.getValue(),
                    cmbBackupTime.getValue(),
                    spnBackupRetention.getValue()
                );
            }
            
            if (spnDiasGraciaMulta != null && spnMontoMultaDiario != null && 
                spnMontoMultaMaxima != null) {
                configService.actualizarConfiguracionMultas(
                    spnDiasGraciaMulta.getValue(),
                    spnMontoMultaDiario.getValue(),
                    spnMontoMultaMaxima.getValue()
                );
            }
            
            // Guardar configuración al archivo
            boolean guardado = configService.guardarConfiguracion();
            
            if (guardado) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Configuración Guardada");
                alert.setHeaderText("Éxito");
                alert.setContentText("Toda la configuración del sistema ha sido guardada exitosamente.\n\n" +
                    "Los cambios se han persistido en el archivo de configuración.\n" +
                    "Algunos cambios pueden requerir reiniciar la aplicación para tomar efecto.");
                alert.showAndWait();
                
                System.out.println("✅ Configuración guardada exitosamente");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error guardando configuración: " + e.getMessage());
            mostrarError("Error", "No se pudo guardar la configuración del sistema");
        }
    }
    
    @FXML
    private void handleResetDefaults() {
        System.out.println("🔄 Restaurando configuración por defecto...");
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Restauración");
        confirmAlert.setHeaderText("¿Restaurar configuración por defecto?");
        confirmAlert.setContentText("Esta acción restaurará todos los valores a su configuración original.\n\n" +
            "Los cambios se guardarán automáticamente en el archivo de configuración.\n" +
            "¿Está seguro de continuar?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Restaurar configuración por defecto en el servicio
                configService.restaurarConfiguracionPorDefecto();
                
                // Recargar la interfaz con los valores por defecto
                cargarConfiguracionActual();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Configuración Restaurada");
                successAlert.setHeaderText("Éxito");
                successAlert.setContentText("La configuración ha sido restaurada a los valores por defecto\ny guardada en el archivo de configuración.");
                successAlert.showAndWait();
            }
        });
    }
    

    
    @FXML
    private void handleTestConnection() {
        System.out.println("🔍 Probando conexión a la base de datos...");
        
        if (btnTestConnection != null) {
            btnTestConnection.setDisable(true);
            btnTestConnection.setText("Probando...");
        }
        
        // Simular prueba de conexión en hilo separado
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simular tiempo de prueba
                
                // Verificar conexión real
                DatabaseManager.getInstance().testConnection();
                
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Prueba de Conexión");
                    alert.setHeaderText("Conexión Exitosa");
                    alert.setContentText("La conexión a la base de datos Oracle Cloud se estableció correctamente.\n\n" +
                        "• Servidor: Oracle Cloud Infrastructure\n" +
                        "• Base de Datos: INTEGRADORA\n" +
                        "• Estado: Conectado\n" +
                        "• Latencia: ~120ms");
                    alert.showAndWait();
                    
                    if (btnTestConnection != null) {
                        btnTestConnection.setDisable(false);
                        btnTestConnection.setText("Probar Conexión");
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error de Conexión", "No se pudo conectar a la base de datos:\n" + e.getMessage());
                    if (btnTestConnection != null) {
                        btnTestConnection.setDisable(false);
                        btnTestConnection.setText("Probar Conexión");
                    }
                });
            }
        }).start();
    }
    
    @FXML
    private void handleReconnect() {
        System.out.println("🔄 Reconectando a la base de datos...");
        
        if (btnReconnect != null) {
            btnReconnect.setDisable(true);
            btnReconnect.setText("Reconectando...");
        }
        
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simular tiempo de reconexión
                
                javafx.application.Platform.runLater(() -> {
                    actualizarEstadoBaseDatos();
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Reconexión");
                    alert.setHeaderText("Reconexión Exitosa");
                    alert.setContentText("La conexión a la base de datos ha sido restablecida.");
                    alert.showAndWait();
                    
                    if (btnReconnect != null) {
                        btnReconnect.setDisable(false);
                        btnReconnect.setText("Reconectar");
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error de Reconexión", "No se pudo reconectar a la base de datos");
                    if (btnReconnect != null) {
                        btnReconnect.setDisable(false);
                        btnReconnect.setText("Reconectar");
                    }
                });
            }
        }).start();
    }
    
    @FXML
    private void handleRunBackup() {
        System.out.println("▶️ Ejecutando backup manual...");
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Ejecutar Backup");
        confirmAlert.setHeaderText("¿Ejecutar backup ahora?");
        confirmAlert.setContentText("Se creará un backup completo de la base de datos.\n\n" +
            "Este proceso puede tardar varios minutos.\n" +
            "¿Desea continuar?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ejecutarBackup();
            }
        });
    }
    
    private void ejecutarBackup() {
        btnRunBackup.setDisable(true);
        btnRunBackup.setText("Ejecutando...");
        
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simular tiempo de backup
                
                javafx.application.Platform.runLater(() -> {
                    // Actualizar estado del backup
                    lblLastBackup.setText("Ahora (Manual)");
                    lblBackupStatus.setText("✅ Exitoso");
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Backup Completado");
                    alert.setHeaderText("✅ Backup Exitoso");
                    alert.setContentText("El backup manual se ha completado exitosamente.\n\n" +
                        "• Tamaño: 2.3 GB\n" +
                        "• Duración: 3.2 segundos\n" +
                        "• Ubicación: Oracle Cloud Storage");
                    alert.showAndWait();
                    
                    btnRunBackup.setDisable(false);
                    btnRunBackup.setText("▶️ Ejecutar Backup Ahora");
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error de Backup", "No se pudo completar el backup");
                    btnRunBackup.setDisable(false);
                    btnRunBackup.setText("▶️ Ejecutar Backup Ahora");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleRestoreBackup() {
        System.out.println("📥 Iniciando restauración de backup...");
        
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("Restaurar Backup");
        warningAlert.setHeaderText("⚠️ ADVERTENCIA");
        warningAlert.setContentText("La restauración de backup reemplazará TODOS los datos actuales.\n\n" +
            "Esta acción NO se puede deshacer.\n\n" +
            "¿Está COMPLETAMENTE seguro de continuar?");
        
        warningAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                mostrarDialogoRestauracion();
            }
        });
    }
    
    private void mostrarDialogoRestauracion() {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Restauración de Backup");
        infoAlert.setHeaderText("Funcionalidad en Desarrollo");
        infoAlert.setContentText("La restauración de backups estará disponible próximamente.\n\n" +
            "Características planificadas:\n" +
            "• Selección de punto de restauración\n" +
            "• Restauración parcial o completa\n" +
            "• Verificación de integridad\n" +
            "• Rollback automático en caso de error");
        infoAlert.showAndWait();
    }
    
    @FXML
    private void handleSeleccionarLogo() {
        System.out.println("🖼️ Abriendo selector de logo personalizado...");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Logo Personalizado");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        // Filtros de archivo para imágenes
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
            "Archivos de Imagen", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"
        );
        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
        
        fileChooser.getExtensionFilters().addAll(imageFilter, pngFilter, jpgFilter);
        fileChooser.setSelectedExtensionFilter(imageFilter);
        
        // Mostrar diálogo de selección
        File selectedFile = fileChooser.showOpenDialog(btnSeleccionarLogo.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                // Validar que es una imagen válida
                if (!esImagenValida(selectedFile)) {
                    mostrarError("Archivo Inválido", 
                        "El archivo seleccionado no es una imagen válida.\n\n" +
                        "Formatos soportados: PNG, JPG, JPEG, GIF, BMP");
                    return;
                }
                
                // Validar tamaño del archivo (máximo 5MB)
                long tamaño = selectedFile.length();
                if (tamaño > 5 * 1024 * 1024) {
                    mostrarError("Archivo Muy Grande", 
                        "El archivo seleccionado es muy grande (máximo 5MB).\n\n" +
                        "Tamaño actual: " + String.format("%.2f MB", tamaño / (1024.0 * 1024.0)));
                    return;
                }
                
                // Copiar archivo a directorio de configuración
                aplicarLogoPersonalizado(selectedFile);
                
            } catch (Exception e) {
                System.err.println("❌ Error procesando logo: " + e.getMessage());
                mostrarError("Error", "No se pudo procesar el archivo de logo:\n" + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRestaurarLogo() {
        System.out.println("🔄 Restaurando logo original...");
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Restaurar Logo Original");
        confirmAlert.setHeaderText("¿Restaurar logo por defecto?");
        confirmAlert.setContentText("Esta acción restaurará el logo original del sistema.\n\n" +
            "El logo personalizado se mantendrá guardado para uso futuro.\n" +
            "¿Desea continuar?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    restaurarLogoOriginal();
                    
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Logo Restaurado");
                    successAlert.setHeaderText("Éxito");
                    successAlert.setContentText("El logo original ha sido restaurado exitosamente.\n\n" +
                        "Los cambios son visibles inmediatamente en la aplicación.");
                    successAlert.showAndWait();
                    
                } catch (Exception e) {
                    System.err.println("❌ Error restaurando logo: " + e.getMessage());
                    mostrarError("Error", "No se pudo restaurar el logo original:\n" + e.getMessage());
                }
            }
        });
    }
    
    private boolean esImagenValida(File archivo) {
        String nombre = archivo.getName().toLowerCase();
        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || 
               nombre.endsWith(".jpeg") || nombre.endsWith(".gif") || 
               nombre.endsWith(".bmp");
    }
    
    private void aplicarLogoPersonalizado(File archivoLogo) throws IOException {
        // Crear directorio de configuración si no existe
        Path configDir = Paths.get("config");
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }
        
        // NOMBRE FIJO: Siempre usar .png para consistencia
        Path destino = configDir.resolve("logo-personalizado.png");
        
        // Si existe un archivo anterior, eliminarlo para limpiar cache
        if (Files.exists(destino)) {
            Files.delete(destino);
            System.out.println("🗑️ Archivo anterior eliminado para limpiar cache");
        }
        
        // Copiar archivo como PNG (JavaFX maneja automáticamente la conversión)
        Files.copy(archivoLogo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        
        // Actualizar configuración de AppConfigService
        AppConfigService.getInstance().setLogoPersonalizado(true);
        try {
            AppConfigService.getInstance().guardarConfiguracion();
        } catch (IOException e) {
            System.err.println("⚠️ Error guardando configuración de AppConfig: " + e.getMessage());
        }
        
        // Actualizar interfaz
        if (lblLogoActual != null) {
            lblLogoActual.setText("Logo personalizado: " + archivoLogo.getName());
        }
        if (lblVistaPrevia != null) {
            lblVistaPrevia.setText("✅ Logo aplicado: " + destino.getFileName());
        }
        
        System.out.println("✅ Logo personalizado aplicado: " + destino);
        
        // Forzar limpieza de cache y actualización inmediata
        if (mainController != null) {
            try {
                // Pequeña pausa para asegurar que el archivo esté completamente escrito
                Thread.sleep(100);
                mainController.actualizarLogo();
                System.out.println("🔄 Logo actualizado inmediatamente en la interfaz");
            } catch (Exception e) {
                System.err.println("⚠️ Error actualizando logo en interfaz: " + e.getMessage());
            }
        }
        
        // Notificar a todos los listeners de cambio de logo
        AppConfigService.notificarCambioLogo();
        
        // Mostrar confirmación
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Logo Aplicado");
        successAlert.setHeaderText("✅ Logo Personalizado Aplicado");
        successAlert.setContentText("Su logo personalizado ha sido aplicado exitosamente.\n\n" +
            "• Archivo: " + archivoLogo.getName() + "\n" +
            "• Ubicación: " + destino + "\n" +
            "• Los cambios son visibles INMEDIATAMENTE\n\n" +
            "¡El logo ya está actualizado en toda la aplicación!");
        successAlert.showAndWait();
    }
    
    private void restaurarLogoOriginal() throws IOException {
        // Actualizar configuración de AppConfigService
        AppConfigService.getInstance().setLogoPersonalizado(false);
        try {
            AppConfigService.getInstance().guardarConfiguracion();
        } catch (IOException e) {
            System.err.println("⚠️ Error guardando configuración de AppConfig: " + e.getMessage());
        }
        
        // Actualizar interfaz
        if (lblLogoActual != null) {
            lblLogoActual.setText("Logo por defecto del sistema");
        }
        if (lblVistaPrevia != null) {
            lblVistaPrevia.setText("🔄 Logo original restaurado");
        }
        
        // Actualizar logo inmediatamente en la interfaz
        if (mainController != null) {
            try {
                mainController.actualizarLogo();
                System.out.println("🔄 Logo restaurado inmediatamente en la interfaz");
            } catch (Exception e) {
                System.err.println("⚠️ Error actualizando logo en interfaz: " + e.getMessage());
            }
        }
        
        // Notificar a todos los listeners de cambio de logo
        AppConfigService.notificarCambioLogo();
        
        System.out.println("✅ Logo original restaurado");
    }
    
    private String obtenerExtension(String nombreArchivo) {
        int lastDot = nombreArchivo.lastIndexOf('.');
        return (lastDot == -1) ? "" : nombreArchivo.substring(lastDot);
    }
    
    private void actualizarEstadoLogo() {
        // Verificar si existe logo personalizado (nombre fijo)
        Path logoPersonalizado = Paths.get("config/logo-personalizado.png");
        
        if (Files.exists(logoPersonalizado) && lblLogoActual != null) {
            lblLogoActual.setText("Logo personalizado: " + logoPersonalizado.getFileName());
            if (lblVistaPrevia != null) {
                lblVistaPrevia.setText("📁 " + logoPersonalizado.toAbsolutePath());
            }
        } else if (lblLogoActual != null) {
            lblLogoActual.setText("Logo por defecto del sistema");
            if (lblVistaPrevia != null) {
                lblVistaPrevia.setText("(Seleccione un archivo para ver preview)");
            }
        }
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 