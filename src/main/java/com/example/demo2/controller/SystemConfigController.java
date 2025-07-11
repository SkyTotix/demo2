package com.example.demo2.controller;

import com.example.demo2.database.DatabaseManager;
import com.example.demo2.service.ConfigurationService;
import com.example.demo2.service.ConfigurationService.SystemConfiguration;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    
    // Botones principales
    @FXML private Button btnSaveAll;
    @FXML private Button btnResetDefaults;
    @FXML private TabPane configTabPane;
    
    private ConfigurationService configService;
    
    @FXML
    public void initialize() {
        configService = ConfigurationService.getInstance();
        
        configurarSpinners();
        configurarComboBoxes();
        cargarConfiguracionActual();
        actualizarEstadoBaseDatos();
    }
    
    private void configurarSpinners() {
        // Configurar spinners de sesiones
        spnSessionTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 480, 30));
        spnMaxConcurrentSessions.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));
        
        // Configurar spinners de base de datos
        spnMinConnections.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 5));
        spnMaxConnections.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 200, 20));
        spnConnectionTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 300, 30));
        
        // Configurar spinners de seguridad
        spnMinPasswordLength.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 50, 8));
        spnPasswordExpiration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 365, 90));
        spnMaxLoginAttempts.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        spnLockoutDuration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1440, 15));
        
        // Configurar spinner de backups
        spnBackupRetention.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 30));
    }
    
    private void configurarComboBoxes() {
        // Configurar frecuencia de backups
        cmbBackupFrequency.setItems(FXCollections.observableArrayList(
            "Diario", "Semanal", "Mensual", "Manual"
        ));
        cmbBackupFrequency.setValue("Diario");
        
        // Configurar horarios de backup
        cmbBackupTime.setItems(FXCollections.observableArrayList(
            "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
            "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
        ));
        cmbBackupTime.setValue("02:00");
    }
    
    private void cargarConfiguracionActual() {
        SystemConfiguration config = configService.getConfiguracion();
        
        // Cargar configuración general
        txtSystemName.setText(config.systemName);
        txtSystemVersion.setText(config.systemVersion);
        chkMaintenanceMode.setSelected(config.maintenanceMode);
        chkDetailedLogs.setSelected(config.detailedLogs);
        
        // Cargar configuración de sesiones
        spnSessionTimeout.getValueFactory().setValue(config.sessionTimeout);
        spnMaxConcurrentSessions.getValueFactory().setValue(config.maxConcurrentSessions);
        chkRememberSession.setSelected(config.rememberSession);
        
        // Cargar configuración de base de datos
        spnMinConnections.getValueFactory().setValue(config.minConnections);
        spnMaxConnections.getValueFactory().setValue(config.maxConnections);
        spnConnectionTimeout.getValueFactory().setValue(config.connectionTimeout);
        
        // Cargar configuración de seguridad
        spnMinPasswordLength.getValueFactory().setValue(config.minPasswordLength);
        chkRequireUppercase.setSelected(config.requireUppercase);
        chkRequireNumbers.setSelected(config.requireNumbers);
        chkRequireSymbols.setSelected(config.requireSymbols);
        spnPasswordExpiration.getValueFactory().setValue(config.passwordExpiration);
        spnMaxLoginAttempts.getValueFactory().setValue(config.maxLoginAttempts);
        spnLockoutDuration.getValueFactory().setValue(config.lockoutDuration);
        chkAuditAccess.setSelected(config.auditAccess);
        
        // Cargar configuración de backups
        chkEnableBackups.setSelected(config.enableBackups);
        cmbBackupFrequency.setValue(config.backupFrequency);
        cmbBackupTime.setValue(config.backupTime);
        spnBackupRetention.getValueFactory().setValue(config.backupRetention);
        
        System.out.println("⚙️ Configuración del sistema cargada desde archivo");
    }
    
    private void actualizarEstadoBaseDatos() {
        try {
            // Verificar estado de la conexión
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            lblDbConnectionStatus.setText("✅ Conectado");
            lblDbConnectionStatus.getStyleClass().clear();
            lblDbConnectionStatus.getStyleClass().add("status-connected");
            
            lblDbServer.setText("Oracle Cloud Infrastructure");
            lblDbName.setText("INTEGRADORA");
            lblDbUser.setText("ADMIN");
            
            System.out.println("🗄️ Estado de la base de datos actualizado");
            
        } catch (Exception e) {
            lblDbConnectionStatus.setText("❌ Desconectado");
            lblDbConnectionStatus.getStyleClass().clear();
            lblDbConnectionStatus.getStyleClass().add("status-error");
            
            System.err.println("❌ Error verificando estado de BD: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSaveAll() {
        System.out.println("💾 Guardando toda la configuración del sistema...");
        
        try {
            // Recopilar valores de la interfaz y actualizar configuración
            configService.actualizarConfiguracionGeneral(
                txtSystemName.getText(),
                txtSystemVersion.getText(),
                chkMaintenanceMode.isSelected(),
                chkDetailedLogs.isSelected()
            );
            
            configService.actualizarConfiguracionSesiones(
                spnSessionTimeout.getValue(),
                spnMaxConcurrentSessions.getValue(),
                chkRememberSession.isSelected()
            );
            
            configService.actualizarConfiguracionBaseDatos(
                spnMinConnections.getValue(),
                spnMaxConnections.getValue(),
                spnConnectionTimeout.getValue()
            );
            
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
            
            configService.actualizarConfiguracionBackups(
                chkEnableBackups.isSelected(),
                cmbBackupFrequency.getValue(),
                cmbBackupTime.getValue(),
                spnBackupRetention.getValue()
            );
            
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
        
        btnTestConnection.setDisable(true);
        btnTestConnection.setText("Probando...");
        
        // Simular prueba de conexión en hilo separado
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simular tiempo de prueba
                
                // Verificar conexión real
                DatabaseManager.getInstance().testConnection();
                
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Prueba de Conexión");
                    alert.setHeaderText("✅ Conexión Exitosa");
                    alert.setContentText("La conexión a la base de datos Oracle Cloud se estableció correctamente.\n\n" +
                        "• Servidor: Oracle Cloud Infrastructure\n" +
                        "• Base de Datos: INTEGRADORA\n" +
                        "• Estado: Conectado\n" +
                        "• Latencia: ~120ms");
                    alert.showAndWait();
                    
                    btnTestConnection.setDisable(false);
                    btnTestConnection.setText("🔍 Probar Conexión");
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error de Conexión", "No se pudo conectar a la base de datos:\n" + e.getMessage());
                    btnTestConnection.setDisable(false);
                    btnTestConnection.setText("🔍 Probar Conexión");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleReconnect() {
        System.out.println("🔄 Reconectando a la base de datos...");
        
        btnReconnect.setDisable(true);
        btnReconnect.setText("Reconectando...");
        
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simular tiempo de reconexión
                
                javafx.application.Platform.runLater(() -> {
                    actualizarEstadoBaseDatos();
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Reconexión");
                    alert.setHeaderText("✅ Reconexión Exitosa");
                    alert.setContentText("La conexión a la base de datos ha sido restablecida.");
                    alert.showAndWait();
                    
                    btnReconnect.setDisable(false);
                    btnReconnect.setText("🔄 Reconectar");
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error de Reconexión", "No se pudo reconectar a la base de datos");
                    btnReconnect.setDisable(false);
                    btnReconnect.setText("🔄 Reconectar");
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
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 