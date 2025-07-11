package com.example.demo2.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Servicio para persistir y cargar la configuración del sistema
 */
public class ConfigurationService {
    
    private static ConfigurationService instance;
    private final String CONFIG_DIR = "config";
    private final String CONFIG_FILE = "system-config.json";
    private final String BACKUP_CONFIG_FILE = "system-config.backup.json";
    
    private final Gson gson;
    private SystemConfiguration currentConfig;
    private final NotificationService notificationService;
    
    private ConfigurationService() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        notificationService = NotificationService.getInstance();
        crearDirectorioConfig();
        cargarConfiguracion();
    }
    
    public static ConfigurationService getInstance() {
        if (instance == null) {
            instance = new ConfigurationService();
        }
        return instance;
    }
    
    /**
     * Crea el directorio de configuración si no existe
     */
    private void crearDirectorioConfig() {
        try {
            Path configPath = Paths.get(CONFIG_DIR);
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
                System.out.println("📁 Directorio de configuración creado: " + CONFIG_DIR);
            }
        } catch (IOException e) {
            System.err.println("❌ Error creando directorio de configuración: " + e.getMessage());
        }
    }
    
    /**
     * Carga la configuración desde el archivo JSON
     */
    public void cargarConfiguracion() {
        Path configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);
        
        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                currentConfig = gson.fromJson(json, SystemConfiguration.class);
                System.out.println("⚙️ Configuración cargada desde: " + configPath);
            } else {
                // Crear configuración por defecto si no existe
                currentConfig = crearConfiguracionPorDefecto();
                guardarConfiguracion();
                System.out.println("⚙️ Configuración por defecto creada");
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("❌ Error cargando configuración: " + e.getMessage());
            currentConfig = crearConfiguracionPorDefecto();
            
            notificationService.notifyWarning(
                "Configuración Restaurada",
                "Se restauró la configuración por defecto debido a un error"
            );
        }
    }
    
    /**
     * Guarda la configuración actual al archivo JSON
     */
    public boolean guardarConfiguracion() {
        try {
            // Crear backup de la configuración anterior
            crearBackup();
            
            // Guardar nueva configuración
            Path configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);
            String json = gson.toJson(currentConfig);
            Files.writeString(configPath, json);
            
            System.out.println("💾 Configuración guardada en: " + configPath);
            
            // ELIMINADO: Notificación innecesaria de guardado exitoso
            // Solo se notifican errores importantes
            
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Error guardando configuración: " + e.getMessage());
            
            notificationService.notifyError(
                "Error al Guardar",
                "No se pudo guardar la configuración del sistema"
            );
            
            return false;
        }
    }
    
    /**
     * Crea un backup de la configuración actual
     */
    private void crearBackup() {
        try {
            Path configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);
            Path backupPath = Paths.get(CONFIG_DIR, BACKUP_CONFIG_FILE);
            
            if (Files.exists(configPath)) {
                Files.copy(configPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("📄 Backup de configuración creado");
            }
        } catch (IOException e) {
            System.err.println("❌ Error creando backup: " + e.getMessage());
        }
    }
    
    /**
     * Restaura la configuración desde el backup
     */
    public boolean restaurarBackup() {
        try {
            Path backupPath = Paths.get(CONFIG_DIR, BACKUP_CONFIG_FILE);
            
            if (Files.exists(backupPath)) {
                String json = Files.readString(backupPath);
                currentConfig = gson.fromJson(json, SystemConfiguration.class);
                guardarConfiguracion();
                
                System.out.println("🔄 Configuración restaurada desde backup");
                
                // ELIMINADO: Notificación innecesaria de restauración
                
                return true;
            } else {
                System.err.println("❌ No se encontró archivo de backup");
                return false;
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("❌ Error restaurando backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Restaura la configuración por defecto
     */
    public void restaurarConfiguracionPorDefecto() {
        currentConfig = crearConfiguracionPorDefecto();
        guardarConfiguracion();
        
        // ELIMINADO: Notificación innecesaria de restauración por defecto
    }
    
    /**
     * Crea una configuración por defecto
     */
    private SystemConfiguration crearConfiguracionPorDefecto() {
        SystemConfiguration config = new SystemConfiguration();
        
        // Configuración general
        config.systemName = "BiblioSystem";
        config.systemVersion = "1.0.0";
        config.maintenanceMode = false;
        config.detailedLogs = true;
        
        // Configuración de sesiones
        config.sessionTimeout = 30;
        config.maxConcurrentSessions = 10;
        config.rememberSession = true;
        
        // Configuración de base de datos
        config.minConnections = 5;
        config.maxConnections = 20;
        config.connectionTimeout = 30;
        
        // Configuración de seguridad
        config.minPasswordLength = 8;
        config.requireUppercase = true;
        config.requireNumbers = true;
        config.requireSymbols = false;
        config.passwordExpiration = 90;
        config.maxLoginAttempts = 3;
        config.lockoutDuration = 15;
        config.auditAccess = true;
        
        // Configuración de backups
        config.enableBackups = true;
        config.backupFrequency = "Diario";
        config.backupTime = "02:00";
        config.backupRetention = 30;
        
        // Configuración de multas (valores por defecto)
        config.diasGraciaMulta = 3;        // 3 días de gracia
        config.montoMultaDiario = 5.0;     // $5.00 por día
        config.montoMultaMaxima = 100.0;   // Máximo $100.00
        
        return config;
    }
    
    // Getters para la configuración actual
    public SystemConfiguration getConfiguracion() {
        return currentConfig;
    }
    
    // Métodos para actualizar configuración específica
    public void actualizarConfiguracionGeneral(String systemName, String systemVersion, 
                                             boolean maintenanceMode, boolean detailedLogs) {
        currentConfig.systemName = systemName;
        currentConfig.systemVersion = systemVersion;
        currentConfig.maintenanceMode = maintenanceMode;
        currentConfig.detailedLogs = detailedLogs;
    }
    
    public void actualizarConfiguracionSesiones(int sessionTimeout, int maxConcurrentSessions, 
                                               boolean rememberSession) {
        currentConfig.sessionTimeout = sessionTimeout;
        currentConfig.maxConcurrentSessions = maxConcurrentSessions;
        currentConfig.rememberSession = rememberSession;
    }
    
    public void actualizarConfiguracionBaseDatos(int minConnections, int maxConnections, 
                                               int connectionTimeout) {
        currentConfig.minConnections = minConnections;
        currentConfig.maxConnections = maxConnections;
        currentConfig.connectionTimeout = connectionTimeout;
    }
    
    public void actualizarConfiguracionSeguridad(int minPasswordLength, boolean requireUppercase,
                                               boolean requireNumbers, boolean requireSymbols,
                                               int passwordExpiration, int maxLoginAttempts,
                                               int lockoutDuration, boolean auditAccess) {
        currentConfig.minPasswordLength = minPasswordLength;
        currentConfig.requireUppercase = requireUppercase;
        currentConfig.requireNumbers = requireNumbers;
        currentConfig.requireSymbols = requireSymbols;
        currentConfig.passwordExpiration = passwordExpiration;
        currentConfig.maxLoginAttempts = maxLoginAttempts;
        currentConfig.lockoutDuration = lockoutDuration;
        currentConfig.auditAccess = auditAccess;
    }
    
    public void actualizarConfiguracionBackups(boolean enableBackups, String backupFrequency,
                                             String backupTime, int backupRetention) {
        currentConfig.enableBackups = enableBackups;
        currentConfig.backupFrequency = backupFrequency;
        currentConfig.backupTime = backupTime;
        currentConfig.backupRetention = backupRetention;
    }
    
    public void actualizarConfiguracionMultas(int diasGraciaMulta, double montoMultaDiario, 
                                            double montoMultaMaxima) {
        currentConfig.diasGraciaMulta = diasGraciaMulta;
        currentConfig.montoMultaDiario = montoMultaDiario;
        currentConfig.montoMultaMaxima = montoMultaMaxima;
    }
    
    /**
     * Exporta la configuración a un archivo especificado
     */
    public boolean exportarConfiguracion(String rutaArchivo) {
        try {
            String json = gson.toJson(currentConfig);
            Files.writeString(Paths.get(rutaArchivo), json);
            
            // ELIMINADO: Notificación innecesaria de exportación exitosa
            
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error exportando configuración: " + e.getMessage());
            
            notificationService.notifyError(
                "Error al Exportar",
                "No se pudo exportar la configuración"
            );
            
            return false;
        }
    }
    
    /**
     * Importa configuración desde un archivo especificado
     */
    public boolean importarConfiguracion(String rutaArchivo) {
        try {
            String json = Files.readString(Paths.get(rutaArchivo));
            SystemConfiguration importedConfig = gson.fromJson(json, SystemConfiguration.class);
            
            // Validar configuración importada
            if (validarConfiguracion(importedConfig)) {
                currentConfig = importedConfig;
                guardarConfiguracion();
                
                // ELIMINADO: Notificación innecesaria de importación exitosa
                
                return true;
            } else {
                notificationService.notifyError(
                    "Configuración Inválida",
                    "El archivo de configuración contiene valores inválidos"
                );
                return false;
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("❌ Error importando configuración: " + e.getMessage());
            
            notificationService.notifyError(
                "Error al Importar",
                "No se pudo importar la configuración desde: " + rutaArchivo
            );
            
            return false;
        }
    }
    
    /**
     * Valida que una configuración tenga valores válidos
     */
    private boolean validarConfiguracion(SystemConfiguration config) {
        if (config == null) return false;
        
        // Validaciones básicas
        if (config.sessionTimeout < 5 || config.sessionTimeout > 480) return false;
        if (config.maxConcurrentSessions < 1 || config.maxConcurrentSessions > 100) return false;
        if (config.minConnections < 1 || config.minConnections > 50) return false;
        if (config.maxConnections < 5 || config.maxConnections > 200) return false;
        if (config.connectionTimeout < 5 || config.connectionTimeout > 300) return false;
        if (config.minPasswordLength < 4 || config.minPasswordLength > 50) return false;
        if (config.passwordExpiration < 0 || config.passwordExpiration > 365) return false;
        if (config.maxLoginAttempts < 1 || config.maxLoginAttempts > 10) return false;
        if (config.lockoutDuration < 1 || config.lockoutDuration > 1440) return false;
        if (config.backupRetention < 1 || config.backupRetention > 365) return false;
        if (config.diasGraciaMulta < 0 || config.diasGraciaMulta > 30) return false;
        if (config.montoMultaDiario < 0.1 || config.montoMultaDiario > 1000.0) return false;
        if (config.montoMultaMaxima < 1.0 || config.montoMultaMaxima > 10000.0) return false;
        
        return true;
    }
    
    /**
     * Clase para representar la configuración del sistema
     */
    public static class SystemConfiguration {
        // Configuración general
        public String systemName;
        public String systemVersion;
        public boolean maintenanceMode;
        public boolean detailedLogs;
        
        // Configuración de sesiones
        public int sessionTimeout;
        public int maxConcurrentSessions;
        public boolean rememberSession;
        
        // Configuración de base de datos
        public int minConnections;
        public int maxConnections;
        public int connectionTimeout;
        
        // Configuración de seguridad
        public int minPasswordLength;
        public boolean requireUppercase;
        public boolean requireNumbers;
        public boolean requireSymbols;
        public int passwordExpiration;
        public int maxLoginAttempts;
        public int lockoutDuration;
        public boolean auditAccess;
        
        // Configuración de backups
        public boolean enableBackups;
        public String backupFrequency;
        public String backupTime;
        public int backupRetention;
        
        // Configuración de multas por préstamos
        public int diasGraciaMulta;        // Días de gracia antes de aplicar multa
        public double montoMultaDiario;    // Monto de multa por día de retraso
        public double montoMultaMaxima;    // Monto máximo de multa por préstamo
        
        @Override
        public String toString() {
            return "SystemConfiguration{" +
                "systemName='" + systemName + '\'' +
                ", systemVersion='" + systemVersion + '\'' +
                ", maintenanceMode=" + maintenanceMode +
                ", detailedLogs=" + detailedLogs +
                '}';
        }
    }
} 