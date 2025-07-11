package com.example.demo2.service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para manejar configuraciones de la aplicación
 * Permite personalizar colores y logo
 */
public class AppConfigService {
    
    // === PATRÓN SINGLETON ===
    private static AppConfigService instance;
    
    // === CONFIGURACIÓN ACTUAL ===
    private AppConfiguration configuracionActual;
    
    // === ARCHIVOS DE CONFIGURACIÓN ===
    private static final String CONFIG_FILE = "config/app-config.properties";
    private static final String CONFIG_DIR = "config";
    
    // === LISTENERS PARA CAMBIOS ===
    private static List<Runnable> logoChangeListeners = new ArrayList<>();
    
    private AppConfigService() {
        cargarConfiguracion();
    }
    
    public static AppConfigService getInstance() {
        if (instance == null) {
            instance = new AppConfigService();
        }
        return instance;
    }
    
    /**
     * Obtiene la configuración actual
     */
    public AppConfiguration getConfiguracion() {
        return configuracionActual;
    }
    
    /**
     * Carga la configuración desde archivo
     */
    public void cargarConfiguracion() {
        try {
            Properties props = new Properties();
            File configFile = new File(CONFIG_FILE);
            
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                
                // Cargar configuración desde propiedades
                configuracionActual = new AppConfiguration(
                    Boolean.parseBoolean(props.getProperty("logo.personalizado", "false"))
                );
                
                // Cargar última modificación si existe
                String ultimaModStr = props.getProperty("ultima.modificacion");
                if (ultimaModStr != null && !ultimaModStr.isEmpty()) {
                    configuracionActual.setUltimaModificacion(LocalDateTime.parse(ultimaModStr));
                }
                
                System.out.println("✅ Configuración cargada desde archivo");
            } else {
                // Crear configuración por defecto
                configuracionActual = new AppConfiguration();
                System.out.println("📝 Usando configuración por defecto");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error cargando configuración: " + e.getMessage());
            configuracionActual = new AppConfiguration(); // Fallback a configuración por defecto
        }
    }
    
    /**
     * Guarda la configuración en archivo
     */
    public void guardarConfiguracion() throws IOException {
        // Crear directorio si no existe
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        Properties props = new Properties();
        props.setProperty("logo.personalizado", String.valueOf(configuracionActual.isLogoPersonalizado()));
        
        // Actualizar fecha de modificación
        configuracionActual.setUltimaModificacion(LocalDateTime.now());
        props.setProperty("ultima.modificacion", configuracionActual.getUltimaModificacion().toString());
        
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuración de BiblioSystem - Generado automáticamente");
        }
        
        System.out.println("💾 Configuración guardada en: " + CONFIG_FILE);
    }
    

    
    // === MÉTODOS PARA LOGO ===
    
    public void setLogoPersonalizado(boolean personalizado) {
        configuracionActual.setLogoPersonalizado(personalizado);
    }
    
    public static void addLogoChangeListener(Runnable listener) {
        logoChangeListeners.add(listener);
    }
    
    public static void notificarCambioLogo() {
        for (Runnable listener : logoChangeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Error notificando cambio de logo: " + e.getMessage());
            }
        }
    }
    

    
    /**
     * Clase interna para almacenar la configuración
     */
    public static class AppConfiguration {
        private boolean logoPersonalizado;
        private LocalDateTime ultimaModificacion;
        
        // Constructor por defecto
        public AppConfiguration() {
            this(false);
        }
        
        // Constructor con logo personalizado
        public AppConfiguration(boolean logoPersonalizado) {
            this.logoPersonalizado = logoPersonalizado;
        }
        
        // Getters y Setters
        public boolean isLogoPersonalizado() { return logoPersonalizado; }
        public void setLogoPersonalizado(boolean logoPersonalizado) { this.logoPersonalizado = logoPersonalizado; }
        
        public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
        public void setUltimaModificacion(LocalDateTime ultimaModificacion) { 
            this.ultimaModificacion = ultimaModificacion; 
        }
    }
} 