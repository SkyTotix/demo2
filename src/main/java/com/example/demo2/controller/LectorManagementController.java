package com.example.demo2.controller;

import com.example.demo2.models.Lector;
import com.example.demo2.service.AuthService;
import com.example.demo2.service.LectorService;
import com.example.demo2.service.NotificationService;
import com.example.demo2.utils.IconHelper;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para la gestión de lectores
 */
public class LectorManagementController {
    
    @FXML private TableView<Lector> tableLectores;
    @FXML private TableColumn<Lector, String> colCodigo;
    @FXML private TableColumn<Lector, String> colNombre;
    @FXML private TableColumn<Lector, String> colDocumento;
    @FXML private TableColumn<Lector, String> colEmail;
    @FXML private TableColumn<Lector, String> colTelefono;
    @FXML private TableColumn<Lector, String> colEstado;
    @FXML private TableColumn<Lector, String> colVencimiento;
    @FXML private TableColumn<Lector, Void> colAcciones;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterEstado;
    @FXML private Button btnNuevoLector;
    @FXML private Button btnRefrescar;
    @FXML private Button btnActualizarVencidos;
    @FXML private Label lblTotalLectores;
    @FXML private Label lblActivos;
    @FXML private Label lblSuspendidos;
    @FXML private Label lblVencidos;
    
    private ObservableList<Lector> lectoresList = FXCollections.observableArrayList();
    private LectorService lectorService;
    private AuthService authService;
    private NotificationService notificationService;
    
    @FXML
    public void initialize() {
        lectorService = LectorService.getInstance();
        authService = AuthService.getInstance();
        notificationService = NotificationService.getInstance();
        
        configurarTabla();
        configurarFiltros();
        configurarBotones();
        cargarLectores();
        actualizarEstadisticas();
    }
    
    private void configurarTabla() {
        // Configurar columnas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoLector"));
        colCodigo.setPrefWidth(100);
        
        colNombre.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNombreCompleto()));
        colNombre.setPrefWidth(200);
        
        colDocumento.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTipoDocumento() + ": " + 
                                    cellData.getValue().getNumeroDocumento()));
        colDocumento.setPrefWidth(150);
        
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(180);
        
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(100);
        
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(100);
        colEstado.setCellFactory(column -> new TableCell<Lector, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    FontIcon estadoIcon;
                    String textoEstado = estado;
                    
                    switch (estado) {
                        case "ACTIVO":
                            estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.SUCCESS_COLOR);
                            break;
                        case "SUSPENDIDO":
                            estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PAUSE_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.WARNING_COLOR);
                            break;
                        case "VENCIDO":
                            estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_TRIANGLE, IconHelper.SMALL_SIZE, IconHelper.ERROR_COLOR);
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
        
        colVencimiento.setCellValueFactory(cellData -> {
            LocalDate vencimiento = cellData.getValue().getFechaVencimiento();
            if (vencimiento != null) {
                return new SimpleStringProperty(vencimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("");
        });
        colVencimiento.setPrefWidth(100);
        
        // Columna de acciones con iconos
        colAcciones.setPrefWidth(180);
        colAcciones.setMaxWidth(180);
        colAcciones.setMinWidth(180);
        colAcciones.setResizable(false);
        
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final HBox container = new HBox(12);
            private final Button btnEditar = new Button();
            private final Button btnActivar = new Button();
            private final Button btnSuspender = new Button();
            private final Button btnEliminar = new Button();
            
            {
                // Configurar botones con iconos
                btnEditar.setGraphic(IconHelper.createIcon(
                    org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EDIT, 
                    IconHelper.MEDIUM_SIZE, IconHelper.PRIMARY_COLOR));
                btnEditar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                btnEditar.setPrefSize(28, 28);
                btnEditar.setTooltip(new Tooltip("Editar"));
                
                btnActivar.setGraphic(IconHelper.createIcon(
                    org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE, 
                    IconHelper.MEDIUM_SIZE, IconHelper.SUCCESS_COLOR));
                btnActivar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                btnActivar.setPrefSize(28, 28);
                btnActivar.setTooltip(new Tooltip("Activar"));
                
                btnSuspender.setGraphic(IconHelper.createIcon(
                    org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PAUSE_CIRCLE, 
                    IconHelper.MEDIUM_SIZE, IconHelper.WARNING_COLOR));
                btnSuspender.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                btnSuspender.setPrefSize(28, 28);
                btnSuspender.setTooltip(new Tooltip("Suspender"));
                
                btnEliminar.setGraphic(IconHelper.createIcon(
                    org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TRASH, 
                    IconHelper.MEDIUM_SIZE, IconHelper.ERROR_COLOR));
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                btnEliminar.setPrefSize(28, 28);
                btnEliminar.setTooltip(new Tooltip("Eliminar"));
                
                // Configurar acciones
                btnEditar.setOnAction(event -> {
                    Lector lector = getTableView().getItems().get(getIndex());
                    editarLector(lector);
                });
                
                btnActivar.setOnAction(event -> {
                    Lector lector = getTableView().getItems().get(getIndex());
                    cambiarEstadoLector(lector, "ACTIVO");
                });
                
                btnSuspender.setOnAction(event -> {
                    Lector lector = getTableView().getItems().get(getIndex());
                    cambiarEstadoLector(lector, "SUSPENDIDO");
                });
                
                btnEliminar.setOnAction(event -> {
                    Lector lector = getTableView().getItems().get(getIndex());
                    eliminarLector(lector);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Lector lector = getTableView().getItems().get(getIndex());
                    container.getChildren().clear();
                    
                    // Siempre mostrar editar
                    container.getChildren().add(btnEditar);
                    
                    // Mostrar botones según el estado
                    switch (lector.getEstado()) {
                        case "ACTIVO":
                            container.getChildren().add(btnSuspender);
                            break;
                        case "SUSPENDIDO":
                        case "VENCIDO":
                            container.getChildren().add(btnActivar);
                            break;
                    }
                    
                    // Siempre mostrar eliminar
                    container.getChildren().add(btnEliminar);
                    
                    setGraphic(container);
                }
            }
        });
        
        // Configurar tabla
        tableLectores.setItems(lectoresList);
        tableLectores.setPlaceholder(new Label("No hay lectores registrados"));
    }
    
    private void configurarFiltros() {
        // Configurar ComboBox de estados
        filterEstado.setItems(FXCollections.observableArrayList(
            "TODOS", "ACTIVO", "SUSPENDIDO", "VENCIDO", "INACTIVO"
        ));
        filterEstado.setValue("TODOS");
        
        // Listener para búsqueda
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarLectores();
        });
        
        // Listener para filtro de estado
        filterEstado.valueProperty().addListener((observable, oldValue, newValue) -> {
            filtrarLectores();
        });
    }
    
    private void configurarBotones() {
        // Botón nuevo lector
        btnNuevoLector.setGraphic(IconHelper.createIcon(
            org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.USER_PLUS,
            IconHelper.SMALL_SIZE, "#FFFFFF"));
        btnNuevoLector.setOnAction(e -> nuevoLector());
        
        // Botón refrescar
        btnRefrescar.setGraphic(IconHelper.createIcon(
            org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SYNC,
            IconHelper.SMALL_SIZE, IconHelper.PRIMARY_COLOR));
        btnRefrescar.setOnAction(e -> {
            cargarLectores();
            actualizarEstadisticas();
        });
        
        // Botón actualizar vencidos
        btnActualizarVencidos.setOnAction(e -> actualizarLectoresVencidos());
    }
    
    private void cargarLectores() {
        try {
            lectoresList.clear();
            lectoresList.addAll(lectorService.obtenerTodos());
        } catch (SQLException e) {
            mostrarError("Error al cargar lectores", e.getMessage());
        }
    }
    
    private void filtrarLectores() {
        try {
            String termino = searchField.getText().toLowerCase();
            String estadoFiltro = filterEstado.getValue();
            
            lectoresList.clear();
            
            for (Lector lector : lectorService.obtenerTodos()) {
                boolean coincideTermino = termino.isEmpty() ||
                    lector.getNombreCompleto().toLowerCase().contains(termino) ||
                    lector.getCodigoLector().toLowerCase().contains(termino) ||
                    lector.getNumeroDocumento().toLowerCase().contains(termino) ||
                    lector.getEmail().toLowerCase().contains(termino);
                
                boolean coincideEstado = "TODOS".equals(estadoFiltro) ||
                    lector.getEstado().equals(estadoFiltro);
                
                if (coincideTermino && coincideEstado) {
                    lectoresList.add(lector);
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al filtrar lectores", e.getMessage());
        }
    }
    
    private void actualizarEstadisticas() {
        try {
            int total = 0, activos = 0, suspendidos = 0, vencidos = 0;
            
            for (Lector lector : lectorService.obtenerTodos()) {
                total++;
                switch (lector.getEstado()) {
                    case "ACTIVO":
                        activos++;
                        break;
                    case "SUSPENDIDO":
                        suspendidos++;
                        break;
                    case "VENCIDO":
                        vencidos++;
                        break;
                }
            }
            
            lblTotalLectores.setText(String.valueOf(total));
            lblActivos.setText(String.valueOf(activos));
            lblSuspendidos.setText(String.valueOf(suspendidos));
            lblVencidos.setText(String.valueOf(vencidos));
            
        } catch (SQLException e) {
            System.err.println("Error actualizando estadísticas: " + e.getMessage());
        }
    }
    
    @FXML
    private void nuevoLector() {
        abrirFormularioLector(null);
    }
    
    private void editarLector(Lector lector) {
        abrirFormularioLector(lector);
    }
    
    private void abrirFormularioLector(Lector lector) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/lector-form-view.fxml"));
            Parent root = loader.load();
            
            LectorFormController controller = loader.getController();
            if (lector != null) {
                controller.setLector(lector);
            }
            
            Stage stage = new Stage();
            stage.setTitle(lector == null ? "Nuevo Lector" : "Editar Lector");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/example/demo2/styles/main.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            
            // Configurar tamaño estandarizado del modal
            stage.setWidth(700);
            stage.setHeight(650);
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.setMaxWidth(800);
            stage.setMaxHeight(700);
            
            // Callback cuando se cierra el formulario
            stage.setOnHidden(e -> {
                cargarLectores();
                actualizarEstadisticas();
            });
            
            stage.show();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }
    
    private void cambiarEstadoLector(Lector lector, String nuevoEstado) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cambio de estado");
        confirmacion.setHeaderText("¿Cambiar estado del lector?");
        confirmacion.setContentText("¿Desea cambiar el estado de " + lector.getNombreCompleto() + 
                                   " a " + nuevoEstado + "?");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    lector.setEstado(nuevoEstado);
                    lector.setActualizadoPor(authService.getUsuarioActual().getId().intValue());
                    
                    if (lectorService.actualizar(lector)) {
                        notificationService.notifySuccess(
                            "Estado actualizado",
                            "El estado del lector se cambió a " + nuevoEstado
                        );
                        cargarLectores();
                        actualizarEstadisticas();
                    }
                } catch (SQLException e) {
                    mostrarError("Error al cambiar estado", e.getMessage());
                }
            }
        });
    }
    
    private void eliminarLector(Lector lector) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar lector?");
        confirmacion.setContentText("¿Está seguro de eliminar al lector " + 
                                   lector.getNombreCompleto() + "? Esta acción no se puede deshacer.");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (lectorService.eliminar(lector.getId())) {
                        notificationService.notifySuccess(
                            "Lector eliminado",
                            "El lector ha sido eliminado exitosamente"
                        );
                        cargarLectores();
                        actualizarEstadisticas();
                    }
                } catch (SQLException e) {
                    if (e.getMessage().contains("constraint")) {
                        mostrarError("No se puede eliminar", 
                                   "El lector tiene préstamos asociados y no puede ser eliminado");
                    } else {
                        mostrarError("Error al eliminar", e.getMessage());
                    }
                }
            }
        });
    }
    
    @FXML
    private void actualizarLectoresVencidos() {
        try {
            int actualizados = lectorService.actualizarLectoresVencidos();
            if (actualizados > 0) {
                notificationService.notifyInfo(
                    "Lectores actualizados",
                    "Se actualizaron " + actualizados + " lectores vencidos"
                );
                cargarLectores();
                actualizarEstadisticas();
            } else {
                notificationService.notifyInfo(
                    "Sin cambios",
                    "No hay lectores vencidos para actualizar"
                );
            }
        } catch (SQLException e) {
            mostrarError("Error al actualizar vencidos", e.getMessage());
        }
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 