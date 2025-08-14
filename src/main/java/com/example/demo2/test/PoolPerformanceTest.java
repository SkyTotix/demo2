package com.example.demo2.test;

import com.example.demo2.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Prueba de rendimiento del pool de conexiones
 * Compara el rendimiento antes y después de la implementación del pool
 */
public class PoolPerformanceTest {
    
    private static final int NUM_OPERATIONS = 50;
    private static final int NUM_THREADS = 5;
    
    public static void main(String[] args) {
        System.out.println("🧪 INICIANDO PRUEBAS DE RENDIMIENTO DEL POOL DE CONEXIONES");
        System.out.println("=".repeat(60));
        
        PoolPerformanceTest test = new PoolPerformanceTest();
        
        // Prueba secuencial
        test.testSequentialOperations();
        
        // Prueba concurrente
        test.testConcurrentOperations();
        
        // Información del pool
        test.showPoolInfo();
        
        System.out.println("\n✅ PRUEBAS COMPLETADAS");
    }
    
    /**
     * Prueba operaciones secuenciales
     */
    public void testSequentialOperations() {
        System.out.println("\n📊 PRUEBA SECUENCIAL - " + NUM_OPERATIONS + " operaciones");
        System.out.println("-".repeat(50));
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < NUM_OPERATIONS; i++) {
            performDatabaseOperation(i + 1);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        System.out.printf("⏱️  Tiempo total: %d ms\n", totalTime);
        System.out.printf("⚡ Tiempo promedio por operación: %.2f ms\n", 
                         (double) totalTime / NUM_OPERATIONS);
        System.out.printf("🚀 Operaciones por segundo: %.2f\n", 
                         (double) NUM_OPERATIONS * 1000 / totalTime);
    }
    
    /**
     * Prueba operaciones concurrentes
     */
    public void testConcurrentOperations() {
        System.out.println("\n🔄 PRUEBA CONCURRENTE - " + NUM_THREADS + " hilos, " + 
                          NUM_OPERATIONS + " operaciones");
        System.out.println("-".repeat(50));
        
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future<Long>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Enviar tareas concurrentes
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i + 1;
            Future<Long> future = executor.submit(() -> {
                long threadStart = System.currentTimeMillis();
                
                for (int j = 0; j < NUM_OPERATIONS / NUM_THREADS; j++) {
                    performDatabaseOperation(threadId * 1000 + j + 1);
                }
                
                return System.currentTimeMillis() - threadStart;
            });
            futures.add(future);
        }
        
        // Esperar resultados
        try {
            long maxThreadTime = 0;
            for (Future<Long> future : futures) {
                long threadTime = future.get();
                maxThreadTime = Math.max(maxThreadTime, threadTime);
            }
            
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("⏱️  Tiempo total: %d ms\n", totalTime);
            System.out.printf("🔥 Tiempo máximo por hilo: %d ms\n", maxThreadTime);
            System.out.printf("⚡ Tiempo promedio por operación: %.2f ms\n", 
                             (double) totalTime / NUM_OPERATIONS);
            System.out.printf("🚀 Operaciones por segundo: %.2f\n", 
                             (double) NUM_OPERATIONS * 1000 / totalTime);
            
        } catch (Exception e) {
            System.err.println("❌ Error en prueba concurrente: " + e.getMessage());
        }
    }
    
    /**
     * Realiza una operación de base de datos típica
     */
    private void performDatabaseOperation(int operationId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Simular consulta típica de préstamos
            String sql = "SELECT COUNT(*) as total FROM prestamos WHERE estado = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "ACTIVO");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt("total");
                        // Operación completada
                        if (operationId % 10 == 0) {
                            System.out.printf("✓ Operación %d completada (count: %d)\n", 
                                             operationId, count);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.printf("❌ Error en operación %d: %s\n", operationId, e.getMessage());
        }
    }
    
    /**
     * Muestra información del pool de conexiones
     */
    public void showPoolInfo() {
        System.out.println("\n📋 INFORMACIÓN DEL POOL DE CONEXIONES");
        System.out.println("-".repeat(50));
        
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            String performanceInfo = dbManager.getPerformanceInfo();
            System.out.println(performanceInfo);
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo información del pool: " + e.getMessage());
        }
    }
    
    /**
     * Prueba de estrés del pool
     */
    public void stressTest() {
        System.out.println("\n🔥 PRUEBA DE ESTRÉS DEL POOL");
        System.out.println("-".repeat(50));
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        for (int i = 0; i < 100; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try (Connection conn = DatabaseManager.getInstance().getConnection()) {
                    Thread.sleep(100); // Simular trabajo
                    System.out.printf("Task %d completed\n", taskId);
                } catch (Exception e) {
                    System.err.printf("Task %d failed: %s\n", taskId, e.getMessage());
                }
            });
        }
        
        try {
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
            System.out.println("✅ Prueba de estrés completada");
        } catch (InterruptedException e) {
            System.err.println("❌ Prueba de estrés interrumpida");
        }
    }
}