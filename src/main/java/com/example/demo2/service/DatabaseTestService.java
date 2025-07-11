package com.example.demo2.service;

import com.example.demo2.database.DatabaseManager;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de prueba para demostrar operaciones con Oracle Cloud
 * Incluye operaciones CRUD básicas y pruebas de conectividad
 */
public class DatabaseTestService {
    
    private final DatabaseManager databaseManager;
    
    public DatabaseTestService() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    /**
     * Prueba completa de conectividad y operaciones básicas
     */
    public Task<String> runFullTest() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                StringBuilder resultBuilder = new StringBuilder();
                
                try {
                    resultBuilder.append("🚀 INICIANDO PRUEBAS DE ORACLE CLOUD\n");
                    resultBuilder.append("=" .repeat(50)).append("\n\n");
                    
                    // 1. Verificar inicialización
                    updateMessage("Verificando inicialización...");
                    if (!databaseManager.isInitialized()) {
                        throw new Exception("DatabaseManager no está inicializado");
                    }
                    resultBuilder.append("✅ DatabaseManager inicializado correctamente\n\n");
                    
                    // 2. Probar conexión básica
                    updateMessage("Probando conexión básica...");
                    if (!databaseManager.testConnection()) {
                        throw new Exception("Falló la prueba de conexión básica");
                    }
                    resultBuilder.append("✅ Prueba de conexión básica exitosa\n\n");
                    
                    // 3. Ejecutar consulta de prueba
                    updateMessage("Ejecutando consulta de prueba...");
                    String testQueryResult = databaseManager.executeTestQuery();
                    resultBuilder.append("📋 CONSULTA DE PRUEBA:\n");
                    resultBuilder.append(testQueryResult).append("\n\n");
                    
                    // 4. Crear tabla de demostración
                    updateMessage("Creando tabla de demostración...");
                    if (databaseManager.createExampleTable()) {
                        resultBuilder.append("✅ Tabla de demostración creada/verificada\n\n");
                    }
                    
                    // 5. Insertar datos de prueba
                    updateMessage("Insertando datos de prueba...");
                    int insertedRecords = insertTestData();
                    resultBuilder.append("📝 Registros insertados: ").append(insertedRecords).append("\n\n");
                    
                    // 6. Consultar datos
                    updateMessage("Consultando datos...");
                    List<String> records = queryTestData();
                    resultBuilder.append("📊 DATOS EN LA TABLA:\n");
                    records.forEach(record -> resultBuilder.append("   ").append(record).append("\n"));
                    resultBuilder.append("\n");
                    
                    // 7. Estadísticas finales
                    resultBuilder.append("🎉 TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE\n");
                    resultBuilder.append("📈 Total de registros en tabla: ").append(records.size()).append("\n");
                    
                    updateMessage("Pruebas completadas exitosamente");
                    
                } catch (Exception e) {
                    String errorMessage = "❌ ERROR EN LAS PRUEBAS: " + e.getMessage();
                    resultBuilder.append(errorMessage).append("\n");
                    updateMessage(errorMessage);
                    throw e;
                }
                
                return resultBuilder.toString();
            }
        };
    }
    
    /**
     * Inserta datos de prueba en la tabla demo
     */
    private int insertTestData() throws SQLException {
        String insertSQL = "INSERT INTO demo_table (nombre, descripcion) VALUES (?, ?)";
        
        String[][] testData = {
            {"Conexión Oracle Cloud", "Prueba de conectividad a Autonomous Database"},
            {"JavaFX + BootstrapFX", "Interfaz gráfica moderna con estilos Bootstrap"},
            {"Aplicación Integradora", "Proyecto de demostración con Oracle Cloud"},
            {"Base de Datos", "Almacenamiento persistente en la nube"}
        };
        
        int totalInserted = 0;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {
            
            for (String[] data : testData) {
                statement.setString(1, data[0]);
                statement.setString(2, data[1]);
                
                try {
                    int rowsAffected = statement.executeUpdate();
                    totalInserted += rowsAffected;
                } catch (SQLException e) {
                    // Ignorar duplicados si existen
                    if (!e.getMessage().contains("unique constraint")) {
                        throw e;
                    }
                }
            }
            
            connection.commit();
        }
        
        return totalInserted;
    }
    
    /**
     * Consulta los datos de la tabla demo
     */
    private List<String> queryTestData() throws SQLException {
        String selectSQL = "SELECT id, nombre, descripcion, fecha_creacion FROM demo_table ORDER BY id";
        List<String> records = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                String record = String.format("ID: %d | %s | %s | %s",
                    resultSet.getLong("id"),
                    resultSet.getString("nombre"),
                    resultSet.getString("descripcion"),
                    resultSet.getTimestamp("fecha_creacion"));
                
                records.add(record);
            }
        }
        
        return records;
    }
    
    /**
     * Verifica el estado de la conexión
     */
    public boolean isConnectionHealthy() {
        return databaseManager.isInitialized() && databaseManager.testConnection();
    }
    
    /**
     * Obtiene información de la base de datos
     */
    public String getDatabaseInfo() {
        try (Connection connection = databaseManager.getConnection()) {
            var metadata = connection.getMetaData();
            
            return String.format(
                "🏛️ INFORMACIÓN DE LA BASE DE DATOS:\n" +
                "   Database: %s\n" +
                "   Version: %s\n" +
                "   Driver: %s v%s\n" +
                "   Usuario: %s\n" +
                "   Schema: %s",
                metadata.getDatabaseProductName(),
                metadata.getDatabaseProductVersion(),
                metadata.getDriverName(),
                metadata.getDriverVersion(),
                metadata.getUserName(),
                connection.getSchema()
            );
            
        } catch (SQLException e) {
            return "❌ Error obteniendo información: " + e.getMessage();
        }
    }
} 