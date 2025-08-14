package com.example.demo2.controller;

import com.example.demo2.models.Prestamo;
import com.example.demo2.service.PrestamoService;
import com.example.demo2.service.NotificationService;
import com.example.demo2.utils.IconHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;

/**
 * Controlador para la gestión de préstamos
 */
public class PrestamoManagementController implements Initializable {
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> estadoFilter;
    @FXML private TableView<Prestamo> prestamosTable;
    @FXML private TableColumn<Prestamo, String> codigoColumn;
    @FXML private TableColumn<Prestamo, String> libroColumn;
    @FXML private TableColumn<Prestamo, String> lectorColumn;
    @FXML private TableColumn<Prestamo, String> fechaPrestamoColumn;
    @FXML private TableColumn<Prestamo, String> fechaDevolucionColumn;
    @FXML private TableColumn<Prestamo, String> estadoColumn;
    @FXML private TableColumn<Prestamo, Void> accionesColumn;
    @FXML private Label totalPrestamosLabel;
    @FXML private Label prestamosActivosLabel;
    @FXML private Label prestamosVencidosLabel;
    @FXML private Label prestamosDevueltosLabel;
    @FXML private Label lblResultados;
    @FXML private Label lblUltimaActualizacion;
    @FXML private Button nuevoPrestamoBtn;
    @FXML private Button renovarListaBtn;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    
    private PrestamoService prestamoService;
    private NotificationService notificationService;
    private List<Prestamo> prestamosCache; // Cache para evitar consultas innecesarias
    private long ultimaActualizacionCache = 0;
    private static final long CACHE_DURATION_MS = 30000; // 30 segundos
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prestamoService = PrestamoService.getInstance();
        notificationService = NotificationService.getInstance();
        
        configurarTabla();
        configurarFiltros();
        configurarBotones();
        cargarPrestamos();
        actualizarEstadisticas();
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("codigoPrestamo"));
        libroColumn.setCellValueFactory(new PropertyValueFactory<>("libroTitulo"));
        lectorColumn.setCellValueFactory(new PropertyValueFactory<>("lectorNombre"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Configurar columna de fecha de préstamo
        fechaPrestamoColumn.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            if (prestamo.getFechaPrestamo() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    prestamo.getFechaPrestamo().toLocalDateTime().toLocalDate()
                           .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Configurar columna de fecha de devolución esperada
        fechaDevolucionColumn.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            if (prestamo.getFechaDevolucionEsperada() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    prestamo.getFechaDevolucionEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Configurar columna de estado con iconos
        estadoColumn.setCellFactory(column -> new TableCell<Prestamo, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    org.kordamp.ikonli.javafx.FontIcon estadoIcon;
                    String textoEstado = estado;
                    
                    switch (estado) {
                        case "ACTIVO":
                            estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.SUCCESS_COLOR);
                            break;
                        case "VENCIDO":
                            estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_TRIANGLE, IconHelper.SMALL_SIZE, IconHelper.ERROR_COLOR);
                            break;
                        case "DEVUELTO":
                            estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.UNDO, IconHelper.SMALL_SIZE, IconHelper.PRIMARY_COLOR);
                            break;
                        case "PERDIDO":
                            estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.QUESTION_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.WARNING_COLOR);
                            break;
                        default:
                            estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.ERROR_COLOR);
                    }
                    
                    hbox.getChildren().addAll(estadoIcon, new Label(textoEstado));
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
        
        // Configurar columna de acciones
        accionesColumn.setCellFactory(column -> new TableCell<Prestamo, Void>() {
            private final Button detallesBtn = new Button();
            private final Button devolucionBtn = new Button();
            private final Button eliminarBtn = new Button();
            private final HBox actionBox = new HBox(12, detallesBtn, devolucionBtn, eliminarBtn);
            
            {
                actionBox.setAlignment(javafx.geometry.Pos.CENTER);
            }
            
            {
                // Configurar botón de detalles
                detallesBtn.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EYE, IconHelper.MEDIUM_SIZE, IconHelper.PRIMARY_COLOR));
                detallesBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                detallesBtn.setPrefSize(28, 28);
                detallesBtn.setTooltip(new Tooltip("Ver detalles"));
                detallesBtn.setOnAction(e -> {
                    Prestamo prestamo = getTableRow().getItem();
                    if (prestamo != null) {
                        mostrarDetallesPrestamo(prestamo);
                    }
                });
                
                // Configurar botón de devolución
                devolucionBtn.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.UNDO, IconHelper.MEDIUM_SIZE, IconHelper.SUCCESS_COLOR));
                devolucionBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                devolucionBtn.setPrefSize(28, 28);
                devolucionBtn.setTooltip(new Tooltip("Registrar devolución"));
                devolucionBtn.setOnAction(e -> {
                    Prestamo prestamo = getTableRow().getItem();
                    if (prestamo != null) {
                        registrarDevolucion(prestamo);
                    }
                });
                
                // Configurar botón de eliminar
                eliminarBtn.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TRASH, IconHelper.MEDIUM_SIZE, IconHelper.ERROR_COLOR));
                eliminarBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                eliminarBtn.setPrefSize(28, 28);
                eliminarBtn.setTooltip(new Tooltip("Eliminar préstamo"));
                eliminarBtn.setOnAction(e -> {
                    Prestamo prestamo = getTableRow().getItem();
                    if (prestamo != null) {
                        eliminarPrestamo(prestamo);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Prestamo prestamo = getTableRow().getItem();
                    if (prestamo != null) {
                        // Mostrar/ocultar botones según el estado
                        devolucionBtn.setVisible(prestamo.isActivo() || "VENCIDO".equals(prestamo.getEstado()));
                        eliminarBtn.setVisible(!"ACTIVO".equals(prestamo.getEstado()) && !"VENCIDO".equals(prestamo.getEstado()));
                    }
                    setGraphic(actionBox);
                }
            }
        });
    }
    
    /**
     * Configura los filtros de búsqueda
     */
    private void configurarFiltros() {
        // Configurar filtro por estado
        estadoFilter.getItems().addAll("Todos", "ACTIVO", "VENCIDO", "DEVUELTO", "PERDIDO");
        estadoFilter.setValue("Todos");
        
        // Listener para búsqueda en tiempo real
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPrestamos();
        });
        
        estadoFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPrestamos();
        });
    }
    
    /**
     * Configura los botones de acción
     */
    private void configurarBotones() {
        // Configurar botón nuevo préstamo
        nuevoPrestamoBtn.setOnAction(e -> abrirFormularioPrestamo(null));
        
        // Configurar botón renovar lista
        renovarListaBtn.setOnAction(e -> {
            System.out.println("🔄 Renovando lista manualmente - invalidando caché...");
            cargarPrestamos(true); // Forzar actualización
            actualizarEstadisticas();
        });
        
        // Configurar botones de búsqueda (si existen)
        if (btnBuscar != null) {
            btnBuscar.setOnAction(e -> filtrarPrestamos());
        }
        
        if (btnLimpiarBusqueda != null) {
            btnLimpiarBusqueda.setOnAction(e -> {
                searchField.clear();
                estadoFilter.setValue("Todos");
                filtrarPrestamos();
            });
        }
        
        // Botones de footer eliminados - funcionalidades no requeridas
    }
    
    private void mostrarMensajeDesarrollo(String funcionalidad) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("En Desarrollo");
        alert.setHeaderText(funcionalidad);
        alert.setContentText("Esta funcionalidad estará disponible en próximas versiones.");
        alert.showAndWait();
    }
    
    private void actualizarTimestamp() {
        if (lblUltimaActualizacion != null) {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            lblUltimaActualizacion.setText("Última actualización: " + timestamp);
        }
    }
    
    /**
     * Carga todos los préstamos en la tabla con sistema de caché optimizado
     */
    private void cargarPrestamos() {
        cargarPrestamos(false);
    }
    
    /**
     * Carga todos los préstamos en la tabla con opción de forzar actualización
     */
    private void cargarPrestamos(boolean forzarActualizacion) {
        try {
            long tiempoActual = System.currentTimeMillis();
            boolean cacheValido = prestamosCache != null && 
                                 (tiempoActual - ultimaActualizacionCache) < CACHE_DURATION_MS;
            
            List<Prestamo> prestamos;
            
            if (!forzarActualizacion && cacheValido) {
                System.out.println("📋 Usando caché de préstamos (válido por " + 
                    ((CACHE_DURATION_MS - (tiempoActual - ultimaActualizacionCache)) / 1000) + " segundos más)");
                prestamos = prestamosCache;
            } else {
                System.out.println("🔄 Aplicando optimizaciones de alto rendimiento para carga de préstamos...");
                long startTime = System.currentTimeMillis();
                
                prestamos = prestamoService.obtenerTodos();
                prestamosCache = prestamos;
                ultimaActualizacionCache = tiempoActual;
                
                long endTime = System.currentTimeMillis();
                System.out.println("✅ Conexión obtenida exitosamente - " + prestamos.size() + 
                    " préstamos cargados en " + (endTime - startTime) + "ms");
            }
            
            prestamosTable.getItems().clear();
            prestamosTable.getItems().addAll(prestamos);
            
            // Actualizar contador de resultados
            if (lblResultados != null) {
                lblResultados.setText("Mostrando " + prestamos.size() + " préstamos");
            }
            
            // Actualizar timestamp
            actualizarTimestamp();
        } catch (SQLException e) {
            notificationService.notifyError("Error al cargar préstamos", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Filtra los préstamos según los criterios seleccionados usando caché optimizado
     */
    private void filtrarPrestamos() {
        try {
            String textoBusqueda = searchField.getText().toLowerCase();
            String estadoFiltro = estadoFilter.getValue();
            
            // Usar caché si está disponible, sino cargar datos
            List<Prestamo> prestamos;
            long tiempoActual = System.currentTimeMillis();
            boolean cacheValido = prestamosCache != null && 
                                 (tiempoActual - ultimaActualizacionCache) < CACHE_DURATION_MS;
            
            if (cacheValido) {
                System.out.println("🔍 Filtrando usando caché de préstamos");
                prestamos = prestamosCache;
            } else {
                System.out.println("🔄 Actualizando caché para filtrado...");
                prestamos = prestamoService.obtenerTodos();
                prestamosCache = prestamos;
                ultimaActualizacionCache = tiempoActual;
            }
            
            prestamosTable.getItems().clear();
            
            for (Prestamo prestamo : prestamos) {
                boolean coincideTexto = textoBusqueda.isEmpty() || 
                    prestamo.getCodigoPrestamo().toLowerCase().contains(textoBusqueda) ||
                    prestamo.getLibroTitulo().toLowerCase().contains(textoBusqueda) ||
                    prestamo.getLectorNombre().toLowerCase().contains(textoBusqueda) ||
                    prestamo.getLibroIsbn().toLowerCase().contains(textoBusqueda);
                
                boolean coincideEstado = estadoFiltro.equals("Todos") || 
                    prestamo.getEstado().equals(estadoFiltro);
                
                if (coincideTexto && coincideEstado) {
                    prestamosTable.getItems().add(prestamo);
                }
            }
            
            // Actualizar contador de resultados
            if (lblResultados != null) {
                lblResultados.setText("Mostrando " + prestamosTable.getItems().size() + " préstamos");
            }
        } catch (SQLException e) {
            notificationService.notifyError("Error al filtrar préstamos", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Actualiza las estadísticas de préstamos usando consulta optimizada
     */
    private void actualizarEstadisticas() {
        try {
            System.out.println("🔄 Aplicando optimizaciones de alto rendimiento para estadísticas...");
            long startTime = System.currentTimeMillis();
            
            // Usar consulta optimizada que obtiene todas las estadísticas en una sola query
            PrestamoService.EstadisticasCompletas stats = prestamoService.obtenerEstadisticasCompletas();
            
            totalPrestamosLabel.setText(String.valueOf(stats.totalPrestamos));
            prestamosActivosLabel.setText(String.valueOf(stats.prestamosActivos));
            prestamosVencidosLabel.setText(String.valueOf(stats.prestamosVencidos));
            prestamosDevueltosLabel.setText(String.valueOf(stats.prestamosDevueltos));
            
            long endTime = System.currentTimeMillis();
            System.out.println("✅ Estadísticas actualizadas en " + (endTime - startTime) + "ms");
            
        } catch (SQLException e) {
            notificationService.notifyError("Error al actualizar estadísticas", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Abre el formulario para crear/editar préstamo
     */
    private void abrirFormularioPrestamo(Prestamo prestamo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/prestamo-form-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            
            // Agregar hojas de estilo necesarias
            scene.getStylesheets().add(org.kordamp.bootstrapfx.BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/example/demo2/styles/main.css").toExternalForm());
            
            PrestamoFormController controller = loader.getController();
            controller.setPrestamo(prestamo);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle(prestamo == null ? "Nuevo Préstamo" : "Editar Préstamo");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(nuevoPrestamoBtn.getScene().getWindow());
            stage.setResizable(true);
            
            // Configurar tamaño ampliado del modal para mostrar todo el contenido
            stage.setWidth(800);
            stage.setHeight(750);
            stage.setMinWidth(750);
            stage.setMinHeight(700);
            stage.setMaxWidth(900);
            stage.setMaxHeight(800);
            
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("❌ Error abriendo formulario de préstamo: " + e.getMessage());
            e.printStackTrace();
            notificationService.notifyError("Error al abrir formulario", "No se pudo abrir el formulario de préstamo: " + e.getMessage());
        }
    }
    
    /**
     * Muestra los detalles completos de un préstamo
     */
    private void mostrarDetallesPrestamo(Prestamo prestamo) {
        try {
            // Refrescar datos del préstamo
            Prestamo prestamoCompleto = prestamoService.buscarPorId(prestamo.getId());
            
            if (prestamoCompleto != null) {
                StringBuilder detalles = new StringBuilder();
                detalles.append("📚 DETALLES DEL PRÉSTAMO\n\n");
                detalles.append("Código: ").append(prestamoCompleto.getCodigoPrestamo()).append("\n");
                detalles.append("Libro: ").append(prestamoCompleto.getLibroTitulo()).append("\n");
                detalles.append("ISBN: ").append(prestamoCompleto.getLibroIsbn()).append("\n");
                detalles.append("Lector: ").append(prestamoCompleto.getLectorNombre()).append("\n");
                detalles.append("Estado: ").append(prestamoCompleto.getEstado()).append("\n");
                detalles.append("Fecha Préstamo: ").append(
                    prestamoCompleto.getFechaPrestamo().toLocalDateTime().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ).append("\n");
                detalles.append("Fecha Devolución Esperada: ").append(
                    prestamoCompleto.getFechaDevolucionEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ).append("\n");
                
                if (prestamoCompleto.getFechaDevolucionReal() != null) {
                    detalles.append("Fecha Devolución Real: ").append(
                        prestamoCompleto.getFechaDevolucionReal().toLocalDateTime().toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ).append("\n");
                }
                
                detalles.append("Bibliotecario Préstamo: ").append(prestamoCompleto.getBibliotecarioPrestamoNombre()).append("\n");
                
                if (prestamoCompleto.getBibliotecarioDevolucionNombre() != null) {
                    detalles.append("Bibliotecario Devolución: ").append(prestamoCompleto.getBibliotecarioDevolucionNombre()).append("\n");
                }
                
                detalles.append("Condición Préstamo: ").append(prestamoCompleto.getCondicionPrestamo()).append("\n");
                
                if (prestamoCompleto.getCondicionDevolucion() != null) {
                    detalles.append("Condición Devolución: ").append(prestamoCompleto.getCondicionDevolucion()).append("\n");
                }
                
                if (prestamoCompleto.getMulta() > 0) {
                    detalles.append("Multa: S/. ").append(String.format("%.2f", prestamoCompleto.getMulta()));
                    detalles.append(" - ").append(prestamoCompleto.isMultaPagada() ? "Pagada" : "Pendiente").append("\n");
                }
                
                if (prestamoCompleto.getObservacionesPrestamo() != null && !prestamoCompleto.getObservacionesPrestamo().isEmpty()) {
                    detalles.append("Observaciones Préstamo: ").append(prestamoCompleto.getObservacionesPrestamo()).append("\n");
                }
                
                if (prestamoCompleto.getObservacionesDevolucion() != null && !prestamoCompleto.getObservacionesDevolucion().isEmpty()) {
                    detalles.append("Observaciones Devolución: ").append(prestamoCompleto.getObservacionesDevolucion()).append("\n");
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Detalles del Préstamo");
                alert.setHeaderText(null);
                alert.setContentText(detalles.toString());
                alert.getDialogPane().setPrefSize(500, 400);
                alert.showAndWait();
            }
        } catch (SQLException e) {
            notificationService.notifyError("Error al obtener detalles", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registra la devolución de un préstamo
     */
    private void registrarDevolucion(Prestamo prestamo) {
        // Crear un diálogo simple para registrar devolución
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Registrar Devolución");
        dialog.setHeaderText("Préstamo: " + prestamo.getCodigoPrestamo());
        
        // Crear campos del formulario
        ComboBox<String> condicionCombo = new ComboBox<>();
        condicionCombo.getItems().addAll("Excelente", "Buena", "Regular", "Malo", "Dañado");
        condicionCombo.setValue("Buena");
        
        TextArea observacionesArea = new TextArea();
        observacionesArea.setPromptText("Observaciones de devolución (opcional)");
        observacionesArea.setPrefRowCount(3);
        
        // Layout del diálogo
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
            new Label("Condición del libro:"),
            condicionCombo,
            new Label("Observaciones:"),
            observacionesArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Registrar devolución (usuario bibliotecario ID = 1 por ahora)
                    boolean exito = prestamoService.registrarDevolucion(
                        prestamo.getId(), 
                        1L, // ID del bibliotecario actual
                        condicionCombo.getValue(),
                        observacionesArea.getText()
                    );
                    
                    if (exito) {
                        notificationService.notifySuccess("Devolución registrada", 
                            "La devolución del préstamo " + prestamo.getCodigoPrestamo() + " ha sido registrada exitosamente.");
                        cargarPrestamos();
                        actualizarEstadisticas();
                    } else {
                        notificationService.notifyError("Error", "No se pudo registrar la devolución.");
                    }
                } catch (SQLException e) {
                    notificationService.notifyError("Error al registrar devolución", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Elimina un préstamo
     */
    private void eliminarPrestamo(Prestamo prestamo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar préstamo?");
        alert.setContentText("¿Está seguro que desea eliminar el préstamo " + prestamo.getCodigoPrestamo() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean exito = prestamoService.eliminar(prestamo.getId());
                    if (exito) {
                        notificationService.notifySuccess("Préstamo eliminado", 
                            "El préstamo " + prestamo.getCodigoPrestamo() + " ha sido eliminado exitosamente.");
                        cargarPrestamos();
                        actualizarEstadisticas();
                    } else {
                        notificationService.notifyError("Error", "No se pudo eliminar el préstamo.");
                    }
                } catch (SQLException e) {
                    notificationService.notifyError("Error al eliminar préstamo", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Refresca la tabla de préstamos invalidando el caché
     */
    public void refrescarTabla() {
        System.out.println("🔄 Refrescando tabla de préstamos - invalidando caché...");
        cargarPrestamos(true); // Forzar actualización
        actualizarEstadisticas();
    }
}