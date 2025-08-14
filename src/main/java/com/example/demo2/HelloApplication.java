package com.example.demo2;

// Importaciones necesarias para JavaFX y estilos
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

/**
 * CLASE PRINCIPAL DE LA APLICACIÓN BIBLIOSYSTEM
 * =============================================
 * 
 * Esta es la clase de entrada principal del sistema de gestión bibliotecaria.
 * Extiende de Application (JavaFX) para crear una aplicación de escritorio.
 * 
 * RESPONSABILIDADES:
 * - Inicializar la aplicación JavaFX
 * - Cargar la pantalla de login como punto de entrada
 * - Configurar estilos globales (BootstrapFX + CSS personalizado)
 * - Establecer propiedades básicas de la ventana principal
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Se conecta con LoginController através del archivo login-view.fxml
 * - Aplica estilos definidos en styles/login.css
 * - Es el punto de partida para todo el flujo de autenticación
 */
public class HelloApplication extends Application {
    
    /**
     * MÉTODO PRINCIPAL DE INICIALIZACIÓN DE JAVAFX
     * ============================================
     * 
     * Este método se ejecuta automáticamente cuando se inicia la aplicación.
     * Configura la ventana principal y carga la primera pantalla (login).
     * 
     * @param stage La ventana principal proporcionada por JavaFX
     * @throws IOException Si hay problemas cargando el archivo FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        // PASO 1: Cargar el archivo FXML de la pantalla de login
        // El FXMLLoader conecta el archivo login-view.fxml con su controlador LoginController
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        
        // PASO 2: Crear la escena con dimensiones específicas para login (500x720)
        Scene scene = new Scene(fxmlLoader.load(), 500, 720);
        
        // PASO 3: Aplicar hojas de estilo para mejorar la apariencia
        // BootstrapFX: Proporciona estilos base similares a Bootstrap web
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        // CSS personalizado: Estilos específicos para la pantalla de login
        scene.getStylesheets().add(getClass().getResource("styles/login.css").toExternalForm());
        
        // PASO 4: Configurar propiedades de la ventana principal
        stage.setTitle("BiblioSystem - Iniciar Sesión");    // Título en la barra superior
        stage.setScene(scene);                               // Asignar la escena a la ventana
        stage.setResizable(false);                          // Evitar redimensionamiento (login fijo)
        stage.centerOnScreen();                             // Centrar en pantalla
        stage.show();                                       // Mostrar la ventana
        
        // NOTA: Desde aquí el control pasa al LoginController cuando el usuario interactúa
    }

    /**
     * PUNTO DE ENTRADA DEL PROGRAMA
     * =============================
     * 
     * Método main estándar de Java que inicia toda la aplicación.
     * El método launch() es proporcionado por JavaFX y internamente llama a start().
     * 
     * FLUJO DE EJECUCIÓN:
     * 1. main() -> launch() -> start() -> LoginController -> MainController (si login exitoso)
     * 
     * @param args Argumentos de línea de comandos (no utilizados en esta aplicación)
     */
    public static void main(String[] args) {
        // Iniciar la aplicación JavaFX
        // Este método maneja toda la inicialización del framework y llama a start()
        launch();
    }
    

}