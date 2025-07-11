package com.example.demo2;

import com.example.demo2.database.DatabaseManager;
import com.example.demo2.service.DatabaseTestService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controlador principal para la interfaz de pruebas de Oracle Cloud
 * Maneja todas las interacciones del usuario con la base de datos
 */
public class HelloController implements Initializable {

    // Elementos del Header
    @FXML private Label statusLabel;
    
    // Elementos de Connection Status
    @FXML private Label connectionStatusLabel;
    @FXML private Button testConnectionBtn;
    @FXML private Button dbInfoBtn;
    @FXML private Button highPerfBtn;
    
    // Elementos de Database Operations
    @FXML private Button runTestsBtn;
    @FXML private Button createTableBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    
    // Elementos de Results
    @FXML private TextArea resultsArea;
    @FXML private Button clearResultsBtn;
    
    // Servicios
    private DatabaseManager databaseManager;
    private DatabaseTestService testService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        databaseManager = DatabaseManager.getInstance();
        testService = new DatabaseTestService();
        
        // Configurar estado inicial
        setupInitialState();
        
        // Verificar estado de conexión en un hilo separado
        checkConnectionStatus();
    }
    
    /**
     * Configura el estado inicial de la interfaz
     */
    private void setupInitialState() {
        // Configurar área de resultados
        resultsArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");
        
        // Configurar barra de progreso
        progressBar.setProgress(0);
        progressLabel.setText("");
        
        // Mensaje de bienvenida con timestamp
        String welcomeMessage = String.format(
            "🚀 SISTEMA DE PRUEBAS ORACLE CLOUD INICIADO\n" +
            "⏰ %s\n" +
            "=" .repeat(50) + "\n\n" +
            "✨ Bienvenido al sistema de pruebas Oracle Cloud\n" +
            "📋 Usa los botones para probar la conectividad y operaciones\n" +
            "💡 Los resultados aparecerán aquí...\n\n",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
        
        resultsArea.setText(welcomeMessage);
    }
    
    /**
     * Verifica el estado de conexión inicial
     */
    private void checkConnectionStatus() {
        Task<Boolean> connectionTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return testService.isConnectionHealthy();
            }
            
            @Override
            protected void succeeded() {
                boolean isConnected = getValue();
                Platform.runLater(() -> {
                    if (isConnected) {
                        boolean isHighPerf = databaseManager.getPerformanceInfo().contains("✅ ACTIVADO");
                        if (isHighPerf) {
                            statusLabel.setText("🚀 Conectado a Oracle Cloud - ALTO RENDIMIENTO");
                            connectionStatusLabel.setText("🚀 Conexión optimizada para alto rendimiento");
                            connectionStatusLabel.setStyle("-fx-text-fill: #FF6B35;"); // Color naranja
                        } else {
                            statusLabel.setText("✅ Conectado a Oracle Cloud");
                            connectionStatusLabel.setText("✅ Conexión establecida correctamente");
                            connectionStatusLabel.setStyle("-fx-text-fill: #28a745;"); // Color verde
                        }
                    } else {
                        statusLabel.setText("❌ Sin conexión - Verificar configuración");
                        connectionStatusLabel.setText("❌ Sin conexión a la base de datos");
                        connectionStatusLabel.setStyle("-fx-text-fill: #dc3545;"); // Color rojo
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Error verificando conexión");
                    connectionStatusLabel.setText("❌ Error en verificación inicial");
                    connectionStatusLabel.setStyle("-fx-text-fill: #dc3545;");
                });
            }
        };
        
        new Thread(connectionTask).start();
    }
    
    /**
     * Maneja el evento de probar conexión
     */
    @FXML
    private void onTestConnection() {
        appendToResults("🧪 Iniciando prueba de conexión...\n");
        setButtonsEnabled(false);
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return databaseManager.testConnection();
            }
            
            @Override
            protected void succeeded() {
                boolean result = getValue();
                Platform.runLater(() -> {
                    if (result) {
                        appendToResults("✅ Prueba de conexión exitosa!\n");
                        connectionStatusLabel.setText("✅ Conexión verificada");
                        connectionStatusLabel.setStyle("-fx-text-fill: #28a745;");
                        statusLabel.setText("✅ Conectado a Oracle Cloud");
                    } else {
                        appendToResults("❌ Falló la prueba de conexión\n");
                        connectionStatusLabel.setText("❌ Error en conexión");
                        connectionStatusLabel.setStyle("-fx-text-fill: #dc3545;");
                    }
                    setButtonsEnabled(true);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendToResults("❌ Error en prueba de conexión: " + getException().getMessage() + "\n");
                    setButtonsEnabled(true);
                });
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * Muestra información de la base de datos
     */
    @FXML
    private void onShowDatabaseInfo() {
        appendToResults("ℹ️ Obteniendo información de la base de datos...\n");
        setButtonsEnabled(false);
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                String dbInfo = testService.getDatabaseInfo();
                String performanceInfo = databaseManager.getPerformanceInfo();
                return dbInfo + "\n\n" + performanceInfo;
            }
            
            @Override
            protected void succeeded() {
                String info = getValue();
                Platform.runLater(() -> {
                    appendToResults(info + "\n\n");
                    setButtonsEnabled(true);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendToResults("❌ Error obteniendo información: " + getException().getMessage() + "\n\n");
                    setButtonsEnabled(true);
                });
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * Ejecuta prueba específica de alto rendimiento
     */
    @FXML
    private void onHighPerformanceTest() {
        appendToResults("🚀 Ejecutando prueba de alto rendimiento...\n");
        setButtonsEnabled(false);
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return databaseManager.executeHighPerformanceTest();
            }
            
            @Override
            protected void succeeded() {
                String result = getValue();
                Platform.runLater(() -> {
                    appendToResults(result + "\n\n");
                    setButtonsEnabled(true);
                    
                    // Actualizar estado si es alto rendimiento
                    if (result.contains("ALTO RENDIMIENTO COMPLETADA")) {
                        statusLabel.setText("🚀 Conectado - ALTO RENDIMIENTO");
                        connectionStatusLabel.setText("🚀 Modo de alto rendimiento activado");
                        connectionStatusLabel.setStyle("-fx-text-fill: #FF6B35;"); // Color naranja para alto rendimiento
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendToResults("❌ Error en prueba de alto rendimiento: " + getException().getMessage() + "\n\n");
                    setButtonsEnabled(true);
                });
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * Ejecuta las pruebas completas
     */
    @FXML
    private void onRunFullTests() {
        appendToResults("🚀 Iniciando pruebas completas de Oracle Cloud...\n");
        setButtonsEnabled(false);
        showProgress(true);
        
        Task<String> testTask = testService.runFullTest();
        
        // Bindear progreso
        progressBar.progressProperty().bind(testTask.progressProperty());
        progressLabel.textProperty().bind(testTask.messageProperty());
        
        testTask.setOnSucceeded(e -> Platform.runLater(() -> {
            appendToResults(testTask.getValue() + "\n");
            showProgress(false);
            setButtonsEnabled(true);
            
            // Actualizar estado
            statusLabel.setText("✅ Pruebas completadas exitosamente");
            connectionStatusLabel.setText("✅ Todas las operaciones exitosas");
            connectionStatusLabel.setStyle("-fx-text-fill: #28a745;");
        }));
        
        testTask.setOnFailed(e -> Platform.runLater(() -> {
            appendToResults("❌ Error en pruebas: " + testTask.getException().getMessage() + "\n\n");
            showProgress(false);
            setButtonsEnabled(true);
        }));
        
        new Thread(testTask).start();
    }
    
    /**
     * Crea tabla de demostración
     */
    @FXML
    private void onCreateTable() {
        appendToResults("📋 Creando tabla de demostración...\n");
        setButtonsEnabled(false);
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return databaseManager.createExampleTable();
            }
            
            @Override
            protected void succeeded() {
                boolean result = getValue();
                Platform.runLater(() -> {
                    if (result) {
                        appendToResults("✅ Tabla de demostración creada exitosamente!\n\n");
                    } else {
                        appendToResults("❌ Error creando tabla de demostración\n\n");
                    }
                    setButtonsEnabled(true);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendToResults("❌ Error: " + getException().getMessage() + "\n\n");
                    setButtonsEnabled(true);
                });
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * Limpia el área de resultados
     */
    @FXML
    private void onClearResults() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        resultsArea.setText(String.format("🧹 Resultados limpiados - %s\n" +
                                        "=" .repeat(50) + "\n\n", timestamp));
    }
    
    /**
     * Añade texto al área de resultados
     */
    private void appendToResults(String text) {
        Platform.runLater(() -> {
            resultsArea.appendText(text);
            // Scroll automático al final
            resultsArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    /**
     * Habilita/deshabilita botones durante operaciones
     */
    private void setButtonsEnabled(boolean enabled) {
        testConnectionBtn.setDisable(!enabled);
        dbInfoBtn.setDisable(!enabled);
        highPerfBtn.setDisable(!enabled);
        runTestsBtn.setDisable(!enabled);
        createTableBtn.setDisable(!enabled);
    }
    
    /**
     * Muestra/oculta la barra de progreso
     */
    private void showProgress(boolean show) {
        progressBar.setVisible(show);
        progressLabel.setVisible(show);
        
        if (!show) {
            progressBar.progressProperty().unbind();
            progressLabel.textProperty().unbind();
            progressBar.setProgress(0);
            progressLabel.setText("");
        }
    }
}