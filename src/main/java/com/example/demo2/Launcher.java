package com.example.demo2;

/**
 * CLASE LAUNCHER PARA JAR EJECUTABLE
 * ==================================
 * 
 * Esta clase sirve como punto de entrada alternativo para la aplicación
 * cuando se ejecuta desde un JAR fat/uber. Es necesaria porque JavaFX
 * requiere que la clase principal no extienda de Application cuando
 * se ejecuta desde el classpath (como en un fat JAR).
 * 
 * PROPÓSITO:
 * - Actuar como proxy para HelloApplication
 * - Permitir que el JAR ejecutable funcione correctamente
 * - Evitar el error "JavaFX runtime components are missing"
 * 
 * USO:
 * Esta clase debe configurarse como mainClass en el maven-shade-plugin
 * del pom.xml en lugar de HelloApplication.
 */
public class Launcher {
    
    /**
     * MÉTODO PRINCIPAL DE ENTRADA
     * ===========================
     * 
     * Simplemente delega la ejecución a HelloApplication.main()
     * sin extender de Application, lo que permite que JavaFX
     * funcione correctamente en un fat JAR.
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        HelloApplication.main(args);
    }
}