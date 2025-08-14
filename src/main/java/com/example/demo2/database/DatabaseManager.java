package com.example.demo2.database;

// Importación del gestor de configuración que maneja la conexión a Oracle Cloud
import com.example.demo2.config.ConfigManager;

// Importaciones estándar de Java para manejo de base de datos
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.demo2.database.PooledConnectionWrapper;

/**
 * GESTOR DE BASE DE DATOS - CONEXIÓN A ORACLE CLOUD INFRASTRUCTURE
 * ================================================================
 * 
 * Esta clase es el punto central para todas las operaciones de base de datos del sistema.
 * Maneja conexiones a Oracle Autonomous Database en Oracle Cloud Infrastructure (OCI).
 * 
 * PATRÓN DE DISEÑO: SINGLETON
 * - Solo existe una instancia de este manager en todo el sistema
 * - Garantiza gestión centralizada y eficiente de conexiones
 * 
 * RESPONSABILIDADES PRINCIPALES:
 * 1. Establecer y gestionar conexiones seguras a Oracle Cloud
 * 2. Configurar optimizaciones de rendimiento para alto throughput
 * 3. Manejar el wallet de seguridad para SSL/TLS
 * 4. Proporcionar connection pooling implícito
 * 5. Ejecutar pruebas de conectividad y diagnósticos
 * 
 * CONFIGURACIÓN DE ORACLE CLOUD:
 * - Utiliza Autonomous Database con service integradora_high
 * - Conexión SSL/TLS obligatoria con wallet
 * - Optimizado para alto rendimiento y concurrencia
 * - Connection pooling para eficiencia de recursos
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Utilizado por todos los servicios (UsuarioService, LibroService, etc.)
 * - Configurado a través de ConfigManager (lee database.properties)
 * - Punto de entrada para todas las operaciones CRUD del sistema
 * - Esencial para AuthService y NotificationService
 */
public class DatabaseManager {
    
    // === PATRÓN SINGLETON ===
    private static DatabaseManager instance;    // Única instancia del manager
    private ConfigManager config;               // Gestor de configuración (wallet, URLs, etc.)
    private boolean isInitialized = false;     // Estado de inicialización exitosa
    
    // Pool de conexiones simple
    private final ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final int MAX_POOL_SIZE = 10;
    private final int MIN_POOL_SIZE = 3;
    private boolean poolInitialized = false;
    
    private DatabaseManager() {
        config = ConfigManager.getInstance();
        initializeDatabase();
    }
    
    /**
     * Obtiene la instancia única del DatabaseManager (Singleton)
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Inicializa la configuración de la base de datos
     */
    private void initializeDatabase() {
        try {
            System.out.println("🔧 Inicializando gestor de base de datos...");
            
            // Cargar el driver Oracle JDBC
            System.out.println("📦 Cargando driver Oracle JDBC...");
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("✅ Driver Oracle JDBC cargado exitosamente");
            
            // Verificar configuración
            System.out.println("🔍 Verificando configuración...");
            String url = config.getDatabaseUrl();
            String username = config.getDatabaseUsername();
            String password = config.getDatabasePassword();
            
            System.out.println("🌐 URL: " + url);
            System.out.println("👤 Usuario: " + username);
            System.out.println("🔒 Password: " + (password != null ? "***configurada***" : "NO CONFIGURADA"));
            
            if (url == null || username == null || password == null) {
                System.err.println("❌ Error: Configuración de base de datos incompleta");
                System.err.println("   URL: " + url);
                System.err.println("   Usuario: " + username);
                System.err.println("   Password: " + (password != null ? "OK" : "NULL"));
                isInitialized = false;
                return;
            }
            
            // Probar la conexión
            System.out.println("🧪 Probando conexión inicial...");
            if (testConnection()) {
                isInitialized = true;
                initializeConnectionPool();
                System.out.println("✅ Gestor de base de datos inicializado correctamente");
                System.out.println("   - Modo: " + (config.isCloudMode() ? "☁️ Oracle Cloud" : "🏠 Local"));
                System.out.println("   - Pool de conexiones: " + (poolInitialized ? "✅ Activo" : "❌ Inactivo"));
            } else {
                System.err.println("❌ Error: Falló la prueba de conexión inicial");
                isInitialized = false;
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: Driver Oracle JDBC no encontrado: " + e.getMessage());
            System.err.println("💡 Verificar que las dependencias de Oracle estén correctamente configuradas");
            e.printStackTrace();
            isInitialized = false;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado inicializando base de datos: " + e.getMessage());
            e.printStackTrace();
            isInitialized = false;
        }
    }
    
    /**
     * Inicializa el pool de conexiones
     */
    private void initializeConnectionPool() {
        try {
            System.out.println("🏊 Inicializando pool de conexiones...");
            
            // Crear conexiones iniciales
            for (int i = 0; i < MIN_POOL_SIZE; i++) {
                Connection conn = createNewConnection();
                if (conn != null) {
                    connectionPool.offer(conn);
                    activeConnections.incrementAndGet();
                }
            }
            
            poolInitialized = true;
            System.out.println("✅ Pool de conexiones inicializado con " + activeConnections.get() + " conexiones");
            
        } catch (Exception e) {
            System.err.println("❌ Error inicializando pool de conexiones: " + e.getMessage());
            poolInitialized = false;
        }
    }
    
    /**
     * Crea una nueva conexión física a la base de datos
     */
    private Connection createNewConnection() throws SQLException {
        String url = config.getDatabaseUrl();
        String username = config.getDatabaseUsername();
        String password = config.getDatabasePassword();
        
        if (url == null || username == null || password == null) {
            throw new SQLException("Configuración de base de datos incompleta");
        }
        
        // Configurar propiedades de conexión para Oracle Cloud
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        
        // Configuraciones específicas para Oracle Cloud
        if (config.isCloudMode()) {
            props.setProperty("oracle.jdbc.fanEnabled", "false");
            props.setProperty("oracle.net.ssl_server_dn_match", "true");
        }
        
        // Optimizaciones de rendimiento (solo mostrar mensaje una vez)
        if (config.isHighPerformanceMode()) {
            // Configuraciones para alto rendimiento
            props.setProperty("oracle.jdbc.defaultRowPrefetch", String.valueOf(config.getFetchSize()));
            props.setProperty("oracle.jdbc.defaultBatchValue", String.valueOf(config.getBatchSize()));
            props.setProperty("oracle.jdbc.implicitStatementCacheSize", "50");
            props.setProperty("oracle.jdbc.maxCachedBufferSize", "20971520"); // 20MB
            props.setProperty("oracle.jdbc.ReadTimeout", "0"); // Sin timeout para consultas largas
        }
        
        Connection connection = DriverManager.getConnection(url, props);
        
        // Configurar la conexión para rendimiento
        connection.setAutoCommit(false); // Control manual de transacciones
        
        return connection;
    }
    
    /**
     * Obtiene una conexión del pool
     */
    public Connection getConnection() throws SQLException {
        if (!isInitialized) {
            throw new SQLException("Gestor de base de datos no inicializado");
        }
        
        if (!poolInitialized) {
            initializeConnectionPool();
        }
        
        try {
            // Intentar obtener una conexión del pool
            Connection connection = connectionPool.poll();
            
            if (connection != null && !connection.isClosed()) {
                // Conexión válida del pool
                return new PooledConnectionWrapper(connection, this);
            } else {
                // Crear nueva conexión si el pool está vacío o la conexión está cerrada
                if (activeConnections.get() < MAX_POOL_SIZE) {
                    connection = createNewConnection();
                    activeConnections.incrementAndGet();
                    return new PooledConnectionWrapper(connection, this);
                } else {
                    throw new SQLException("Pool de conexiones agotado. Máximo: " + MAX_POOL_SIZE);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo conexión del pool: " + e.getMessage());
            throw e;
        }
    }
    
    /**
      * Devuelve una conexión al pool
      */
     public void returnConnection(Connection connection) {
         if (connection != null && poolInitialized) {
             try {
                 if (!connection.isClosed()) {
                     // Resetear el estado de la conexión
                     if (!connection.getAutoCommit()) {
                         connection.rollback(); // Limpiar cualquier transacción pendiente
                     }
                     connectionPool.offer(connection);
                 } else {
                     // Conexión cerrada, decrementar contador
                     activeConnections.decrementAndGet();
                 }
             } catch (SQLException e) {
                 System.err.println("⚠️ Error devolviendo conexión al pool: " + e.getMessage());
                 activeConnections.decrementAndGet();
             }
         }
     }
     
     /**
      * Cierra todas las conexiones del pool
      */
     public void closePool() {
         System.out.println("🔒 Cerrando pool de conexiones...");
         
         Connection conn;
         while ((conn = connectionPool.poll()) != null) {
             try {
                 if (!conn.isClosed()) {
                     conn.close();
                 }
             } catch (SQLException e) {
                 System.err.println("⚠️ Error cerrando conexión del pool: " + e.getMessage());
             }
         }
         
         activeConnections.set(0);
         poolInitialized = false;
         System.out.println("✅ Pool de conexiones cerrado");
     }
    
    /**
     * Prueba la conexión a la base de datos
     */
    public boolean testConnection() {
        try {
            System.out.println("🧪 Iniciando prueba de conexión...");
            
            String url = config.getDatabaseUrl();
            String username = config.getDatabaseUsername();
            String password = config.getDatabasePassword();
            
            System.out.println("🔗 Intentando conectar con:");
            System.out.println("   URL: " + url);
            System.out.println("   Usuario: " + username);
            
            // Configurar propiedades de conexión para Oracle Cloud
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            
            // Configuraciones específicas para Oracle Cloud
            if (config.isCloudMode()) {
                props.setProperty("oracle.jdbc.fanEnabled", "false");
                props.setProperty("oracle.net.ssl_server_dn_match", "true");
                System.out.println("🌐 Configuraciones Oracle Cloud aplicadas");
            }
            
            System.out.println("📡 Estableciendo conexión...");
            Connection connection = DriverManager.getConnection(url, props);
            
            System.out.println("✅ Conexión establecida, probando validez...");
            
            if (connection != null && !connection.isClosed()) {
                String dbInfo = String.format("Base de datos: %s v%s - Usuario: %s", 
                    connection.getMetaData().getDatabaseProductName(),
                    connection.getMetaData().getDatabaseProductVersion(),
                    connection.getMetaData().getUserName());
                System.out.println("✅ Conexión exitosa: " + dbInfo);
                
                connection.close();
                System.out.println("🔒 Conexión cerrada correctamente");
                return true;
            } else {
                System.err.println("❌ Conexión nula o cerrada");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error SQL en prueba de conexión:");
            System.err.println("   Código de error: " + e.getErrorCode());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Mensaje: " + e.getMessage());
            
            // Proporcionar ayuda específica según el error
            if (e.getMessage().contains("TNS")) {
                System.err.println("💡 Problema TNS:");
                System.err.println("   - Verifica que el wallet esté en la carpeta correcta");
                System.err.println("   - Verifica que el service_name sea correcto");
                System.err.println("   - Verifica que los archivos del wallet no estén corruptos");
            } else if (e.getMessage().contains("authentication") || e.getMessage().contains("ORA-01017")) {
                System.err.println("💡 Problema de autenticación:");
                System.err.println("   - Verifica el usuario y contraseña");
                System.err.println("   - Verifica que el usuario ADMIN esté habilitado");
            } else if (e.getMessage().contains("network adapter") || e.getMessage().contains("Connection refused")) {
                System.err.println("💡 Problema de red:");
                System.err.println("   - Verifica la conectividad de red");
                System.err.println("   - Verifica firewall/proxy");
                System.err.println("   - Verifica que la base de datos esté activa");
            }
            
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado en prueba de conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ejecuta una consulta simple para verificar la conectividad
     */
    public String executeTestQuery() {
        String query = "SELECT 'Conexión exitosa a Oracle Cloud' as mensaje, " +
                      "SYSDATE as fecha_actual, " +
                      "USER as usuario_actual " +
                      "FROM DUAL";
        
        try (Connection connection = getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(query)) {
            
            if (resultSet.next()) {
                return String.format("✅ %s\n📅 Fecha: %s\n👤 Usuario: %s",
                    resultSet.getString("mensaje"),
                    resultSet.getTimestamp("fecha_actual"),
                    resultSet.getString("usuario_actual"));
            }
            
        } catch (SQLException e) {
            return "❌ Error ejecutando consulta de prueba: " + e.getMessage();
        }
        
        return "❌ No se pudo ejecutar la consulta de prueba";
    }
    
    /**
     * Crea una tabla de ejemplo si no existe
     */
    public boolean createExampleTable() {
        // Primero verificar si la tabla existe
        String checkTableSQL = """
            SELECT COUNT(*) as table_count FROM user_tables WHERE table_name = 'DEMO_TABLE'
            """;
            
        String createTableSQL = """
            CREATE TABLE demo_table (
                id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                nombre VARCHAR2(100) NOT NULL,
                descripcion VARCHAR2(500),
                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Connection connection = getConnection()) {
            // Verificar si la tabla ya existe
            try (var checkStatement = connection.createStatement();
                 var checkResult = checkStatement.executeQuery(checkTableSQL)) {
                
                if (checkResult.next() && checkResult.getInt("table_count") > 0) {
                    System.out.println("✅ Tabla demo_table ya existe");
                    return true;
                }
            }
            
            // Crear la tabla si no existe
            try (var statement = connection.createStatement()) {
                statement.execute(createTableSQL);
                connection.commit();
                System.out.println("✅ Tabla de demostración creada exitosamente");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creando tabla de ejemplo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el manager está inicializado correctamente
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
      * Obtiene información detallada de configuración de rendimiento
      */
    public String getPerformanceInfo() {
        if (!isInitialized) {
            return "❌ Manager no inicializado";
        }
        
        String poolInfo = String.format(
            "\n🏊 POOL DE CONEXIONES:\n" +
            "   Estado: %s\n" +
            "   Conexiones activas: %d/%d\n" +
            "   Conexiones disponibles: %d",
            poolInitialized ? "✅ Activo" : "❌ Inactivo",
            activeConnections.get(),
            MAX_POOL_SIZE,
            connectionPool.size()
        );
        
        return config.getPerformanceInfo() + poolInfo;
    }
    
    /**
     * Ejecuta una consulta optimizada de alto rendimiento
     */
    public String executeHighPerformanceTest() {
        if (!config.isHighPerformanceMode()) {
            return "⚠️ Modo de alto rendimiento no está activado";
        }
        
        String testQuery = """
            SELECT 
                'PRUEBA DE ALTO RENDIMIENTO' as tipo_prueba,
                SYSTIMESTAMP as timestamp_inicio,
                SYS_CONTEXT('USERENV', 'SERVICE_NAME') as service_name,
                SYS_CONTEXT('USERENV', 'SERVER_HOST') as server_host,
                SYS_CONTEXT('USERENV', 'INSTANCE_NAME') as instance_name,
                (SELECT value FROM v$parameter WHERE name = 'cpu_count') as cpu_count,
                ROUND((SELECT value FROM v$parameter WHERE name = 'memory_target')/1024/1024/1024, 2) as memory_gb
            FROM DUAL
            """;
        
        try (Connection connection = getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(testQuery)) {
            
            if (resultSet.next()) {
                return String.format(
                    "🚀 PRUEBA DE ALTO RENDIMIENTO COMPLETADA:\n" +
                    "   Tipo: %s\n" +
                    "   Timestamp: %s\n" +
                    "   Service: %s\n" +
                    "   Server: %s\n" +
                    "   Instance: %s\n" +
                    "   CPUs: %s\n" +
                    "   Memoria: %s GB\n" +
                    "   ⚡ Configuración optimizada para máximo rendimiento",
                    resultSet.getString("tipo_prueba"),
                    resultSet.getTimestamp("timestamp_inicio"),
                    resultSet.getString("service_name"),
                    resultSet.getString("server_host"),
                    resultSet.getString("instance_name"),
                    resultSet.getString("cpu_count"),
                    resultSet.getString("memory_gb")
                );
            }
            
        } catch (SQLException e) {
            return "❌ Error en prueba de alto rendimiento: " + e.getMessage();
        }
        
        return "❌ No se pudo completar la prueba de alto rendimiento";
    }
}