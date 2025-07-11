package com.example.demo2.config;

// Importaciones estándar de Java para manejo de archivos y propiedades
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * GESTOR DE CONFIGURACIÓN GLOBAL DEL SISTEMA - HUB DE PROPIEDADES
 * ================================================================
 * 
 * Este gestor centraliza toda la configuración del sistema BiblioSystem,
 * especialmente la configuración de conexión a Oracle Cloud Infrastructure.
 * Maneja múltiples modos de operación y optimizaciones de rendimiento.
 * 
 * PATRÓN DE DISEÑO: SINGLETON
 * - Solo existe una instancia en todo el sistema
 * - Garantiza configuración consistente en todos los componentes
 * - Carga única de propiedades al inicio del sistema
 * 
 * RESPONSABILIDADES PRINCIPALES:
 * 1. Cargar configuración desde archivo database.properties
 * 2. Detectar automáticamente modo de operación (Cloud vs Local)
 * 3. Proporcionar URLs de conexión apropiadas para cada modo
 * 4. Gestionar credenciales de base de datos de forma segura
 * 5. Configurar optimizaciones de rendimiento para Oracle Cloud
 * 6. Manejar parámetros de connection pooling
 * 7. Proporcionar configuración específica para Autonomous Database
 * 
 * MODOS DE OPERACIÓN SOPORTADOS:
 * 
 * ORACLE CLOUD MODE (Automático):
 * - Detectado cuando existen propiedades db.cloud.*
 * - Utiliza TNS_ADMIN y wallet para SSL/TLS
 * - Service integradora_high para alto rendimiento
 * - Connection string: jdbc:oracle:thin:@service?TNS_ADMIN=wallet
 * 
 * LOCAL MODE (Fallback):
 * - Base de datos Oracle local o en red
 * - Connection string tradicional: jdbc:oracle:thin:@host:port:sid
 * 
 * CONFIGURACIONES DE RENDIMIENTO:
 * - Fetch Size: Número de registros por fetch (default: 1000)
 * - Batch Size: Operaciones agrupadas (default: 100)
 * - Pool Size: Tamaño de pool de conexiones (5-20)
 * - High Performance Mode: Detectado por service name _high
 * - Cache de statements y optimizaciones Oracle-específicas
 * 
 * ARCHIVOS DE CONFIGURACIÓN BUSCADOS:
 * 1. src/main/resources/config/database.properties (principal)
 * 2. src/main/resources/database.properties (fallback)
 * 3. Múltiples rutas para compatibilidad con diferentes entornos
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Utilizado por DatabaseManager para establecer conexiones
 * - Referenciado por todos los servicios que necesitan configuración
 * - Esencial para la inicialización del sistema
 * - Base para el sistema de logging y debugging de conexiones
 */
public class ConfigManager {
    
    // === PATRÓN SINGLETON ===
    private static ConfigManager instance;      // Única instancia del gestor
    private Properties properties;              // Objeto Properties con toda la configuración
    private boolean isCloudMode = false;        // Flag que indica si opera en modo Oracle Cloud
    
    private ConfigManager() {
        loadConfiguration();
    }
    
    /**
     * Obtiene la instancia única del ConfigManager (Singleton)
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * Carga la configuración desde el archivo properties
     */
    private void loadConfiguration() {
        properties = new Properties();
        
        System.out.println("🔍 Intentando cargar database.properties...");
        
        // Probar múltiples métodos de carga
        String[] paths = {
            "config/database.properties",
            "/config/database.properties",
            "database.properties"
        };
        
        boolean loaded = false;
        
        for (String path : paths) {
            System.out.println("🔍 Probando ruta: " + path);
            
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
                if (input != null) {
                    System.out.println("✅ Archivo encontrado en: " + path);
                    properties.load(input);
                    loaded = true;
                    break;
                }
            } catch (IOException e) {
                System.err.println("❌ Error cargando desde " + path + ": " + e.getMessage());
            }
        }
        
        if (!loaded) {
            // Último intento: usar getResource en lugar de getResourceAsStream
            try {
                var resource = getClass().getClassLoader().getResource("config/database.properties");
                if (resource != null) {
                    System.out.println("✅ Archivo encontrado con getResource: " + resource.toString());
                    properties.load(resource.openStream());
                    loaded = true;
                } else {
                    System.err.println("❌ No se pudo encontrar database.properties con ningún método");
                    
                    // Debugging: listar todos los recursos disponibles
                    try {
                        var configDir = getClass().getClassLoader().getResource("config/");
                        if (configDir != null) {
                            System.out.println("📁 Directorio config encontrado en: " + configDir.toString());
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error verificando directorio config: " + e.getMessage());
                    }
                    return;
                }
            } catch (IOException e) {
                System.err.println("❌ Error final cargando configuración: " + e.getMessage());
                return;
            }
        }
        
        if (loaded) {
            System.out.println("📋 Propiedades cargadas: " + properties.size());
            
            // Detectar si está configurado para modo cloud
            String tnsAdmin = properties.getProperty("db.cloud.tns_admin");
            String cloudUsername = properties.getProperty("db.cloud.username");
            
            System.out.println("🔧 TNS_ADMIN: " + tnsAdmin);
            System.out.println("👤 Usuario: " + cloudUsername);
            
            isCloudMode = (tnsAdmin != null && !tnsAdmin.isEmpty() && 
                          cloudUsername != null && !cloudUsername.isEmpty());
            
            System.out.println("✅ Configuración cargada - Modo: " + 
                             (isCloudMode ? "☁️ Oracle Cloud" : "🏠 Local"));
        }
    }
    
    /**
     * Obtiene una propiedad de configuración
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Obtiene una propiedad con valor por defecto
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Obtiene una propiedad como entero
     */
    public int getPropertyAsInt(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Verifica si está configurado para usar Oracle Cloud
     */
    public boolean isCloudMode() {
        return isCloudMode;
    }
    
    /**
     * Obtiene la URL de conexión según el modo configurado
     */
    public String getDatabaseUrl() {
        if (isCloudMode) {
            String tnsAdmin = getProperty("db.cloud.tns_admin");
            String serviceName = getProperty("db.cloud.service_name");
            return String.format("jdbc:oracle:thin:@%s?TNS_ADMIN=%s", 
                                serviceName, tnsAdmin);
        } else {
            return getProperty("db.url");
        }
    }
    
    /**
     * Obtiene el usuario de base de datos según el modo configurado
     */
    public String getDatabaseUsername() {
        return isCloudMode ? 
            getProperty("db.cloud.username") : 
            getProperty("db.username");
    }
    
    /**
     * Obtiene la contraseña de base de datos según el modo configurado
     */
    public String getDatabasePassword() {
        return isCloudMode ? 
            getProperty("db.cloud.password") : 
            getProperty("db.password");
    }
    
    /**
     * Obtiene el fetch size para optimización de rendimiento
     */
    public int getFetchSize() {
        return getPropertyAsInt("db.performance.fetch_size", 1000);
    }
    
    /**
     * Obtiene el batch size para operaciones en lote
     */
    public int getBatchSize() {
        return getPropertyAsInt("db.performance.batch_size", 100);
    }
    
    /**
     * Verifica si está configurado para alto rendimiento
     */
    public boolean isHighPerformanceMode() {
        String serviceName = getProperty("db.cloud.service_name", "");
        return serviceName.contains("_high") || serviceName.contains("_tpurgent");
    }
    
    /**
     * Obtiene información de configuración de rendimiento
     */
    public String getPerformanceInfo() {
        return String.format(
            "🚀 CONFIGURACIÓN DE RENDIMIENTO:\n" +
            "   Service: %s\n" +
            "   Pool inicial: %d conexiones\n" +
            "   Pool máximo: %d conexiones\n" +
            "   Fetch size: %d registros\n" +
            "   Batch size: %d operaciones\n" +
            "   Modo alto rendimiento: %s",
            getProperty("db.cloud.service_name", "N/A"),
            getPropertyAsInt("db.pool.initial_size", 5),
            getPropertyAsInt("db.pool.max_size", 20),
            getFetchSize(),
            getBatchSize(),
            isHighPerformanceMode() ? "✅ ACTIVADO" : "❌ Desactivado"
        );
    }
} 