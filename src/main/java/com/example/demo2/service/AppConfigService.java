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
    private static List<LogoChangeListener> logoChangeListeners = new ArrayList<>();
    
    // === INTERFAZ PARA LISTENERS DE CAMBIO DE LOGO ===
    public interface LogoChangeListener {
        void onLogoChanged(String tipoLogo);
    }
    
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
                boolean logoAppPersonalizado = Boolean.parseBoolean(props.getProperty("logo.app.personalizado", 
                    props.getProperty("logo.personalizado", "false")));
                boolean logoLoginPersonalizado = Boolean.parseBoolean(props.getProperty("logo.login.personalizado", 
                    props.getProperty("logo.personalizado", "false")));
                
                configuracionActual = new AppConfiguration(logoAppPersonalizado, logoLoginPersonalizado);
                
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
        props.setProperty("logo.app.personalizado", String.valueOf(configuracionActual.isLogoAppPersonalizado()));
        props.setProperty("logo.login.personalizado", String.valueOf(configuracionActual.isLogoLoginPersonalizado()));
        // Mantener para compatibilidad con versiones anteriores
        props.setProperty("logo.personalizado", String.valueOf(configuracionActual.isLogoAppPersonalizado()));
        
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
    
    public void setLogoAppPersonalizado(boolean personalizado) {
        configuracionActual.setLogoAppPersonalizado(personalizado);
    }
    
    public void setLogoLoginPersonalizado(boolean personalizado) {
        configuracionActual.setLogoLoginPersonalizado(personalizado);
    }
    
    // Método para compatibilidad con versiones anteriores
    public void setLogoPersonalizado(boolean personalizado) {
        configuracionActual.setLogoPersonalizado(personalizado);
    }
    
    public static void addLogoChangeListener(LogoChangeListener listener) {
        logoChangeListeners.add(listener);
    }
    
    // Método para compatibilidad con versiones anteriores
    public static void addLogoChangeListener(Runnable listener) {
        logoChangeListeners.add(tipoLogo -> listener.run());
    }
    
    public static void notificarCambioLogo(String tipoLogo) {
        System.out.println("🔄 Notificando cambio de logo: " + tipoLogo + " a " + logoChangeListeners.size() + " listeners");
        
        // Forzar actualización inmediata en el hilo de JavaFX
        javafx.application.Platform.runLater(() -> {
            for (LogoChangeListener listener : logoChangeListeners) {
                try {
                    listener.onLogoChanged(tipoLogo);
                    System.out.println("✅ Listener notificado exitosamente para logo: " + tipoLogo);
                } catch (Exception e) {
                    System.err.println("❌ Error notificando cambio de logo " + tipoLogo + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    // Método para compatibilidad con versiones anteriores
    public static void notificarCambioLogo() {
        notificarCambioLogo("app");
        notificarCambioLogo("login");
    }
    

    
    /**
     * Clase interna para almacenar la configuración
     */
    public static class AppConfiguration {
        private boolean logoAppPersonalizado;
        private boolean logoLoginPersonalizado;
        private LocalDateTime ultimaModificacion;
        
        // Constructor por defecto
        public AppConfiguration() {
            this(false, false);
        }
        
        // Constructor con logos personalizados
        public AppConfiguration(boolean logoAppPersonalizado, boolean logoLoginPersonalizado) {
            this.logoAppPersonalizado = logoAppPersonalizado;
            this.logoLoginPersonalizado = logoLoginPersonalizado;
        }
        
        // Constructor para compatibilidad con versiones anteriores
        public AppConfiguration(boolean logoPersonalizado) {
            this(logoPersonalizado, logoPersonalizado);
        }
        
        // Getters y Setters
        public boolean isLogoAppPersonalizado() { return logoAppPersonalizado; }
        public void setLogoAppPersonalizado(boolean logoAppPersonalizado) { this.logoAppPersonalizado = logoAppPersonalizado; }
        
        public boolean isLogoLoginPersonalizado() { return logoLoginPersonalizado; }
        public void setLogoLoginPersonalizado(boolean logoLoginPersonalizado) { this.logoLoginPersonalizado = logoLoginPersonalizado; }
        
        // Método para compatibilidad con versiones anteriores
        public boolean isLogoPersonalizado() { return logoAppPersonalizado; }
        public void setLogoPersonalizado(boolean logoPersonalizado) { 
            this.logoAppPersonalizado = logoPersonalizado;
            this.logoLoginPersonalizado = logoPersonalizado;
        }
        
        public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
        public void setUltimaModificacion(LocalDateTime ultimaModificacion) { 
            this.ultimaModificacion = ultimaModificacion; 
        }
    }
}