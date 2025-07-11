package com.example.demo2.controller;

import com.example.demo2.models.Usuario;
import com.example.demo2.models.enums.TipoUsuario;
import com.example.demo2.service.UsuarioService;
import com.example.demo2.service.NotificationService;
import com.example.demo2.utils.IconHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de bibliotecarios
 */
public class BibliotecarioManagementController {
    
    // Elementos de la interfaz
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblTotal;
    @FXML private TableView<Usuario> tableBibliotecarios;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colTelefono;
    @FXML private TableColumn<Usuario, String> colFechaCreacion;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private TableColumn<Usuario, String> colUltimaConexion;
    @FXML private TableColumn<Usuario, Void> colAcciones;
    @FXML private Button btnNuevoBibliotecario;
    @FXML private Button btnRefrescar;
    @FXML private Label lblTotalBibliotecarios;
    @FXML private Label lblBibliotecariasActivos;
    @FXML private Label lblBibliotecariasInactivos;
    
    // Servicios
    private UsuarioService usuarioService;
    private NotificationService notificationService;
    
    // Datos
    private ObservableList<Usuario> bibliotecarios;
    private FilteredList<Usuario> bibliotecariosFiltrados;
    
    @FXML
    public void initialize() {
        usuarioService = UsuarioService.getInstance();
        notificationService = NotificationService.getInstance();
        
        configurarTabla();
        configurarFiltros();
        cargarBibliotecarios();
        
        System.out.println("📚 Controlador de gestión de bibliotecarios inicializado");
    }
    
    private void configurarTabla() {
        // Configurar columnas
        colNombre.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                usuario.getNombre() + " " + usuario.getApellido()
            );
        });
        
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        
        colFechaCreacion.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaCreacionAsLocalDate();
            return new javafx.beans.property.SimpleStringProperty(
                fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"
            );
        });
        
        colEstado.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Usuario usuario = getTableRow().getItem();
                    boolean activo = usuario.isActivo();
                    
                    FontIcon icon = activo ? IconHelper.getSuccessIcon() : IconHelper.getErrorIcon();
                    String texto = activo ? " Activo" : " Inactivo";
                    
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(3);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    hbox.getChildren().addAll(icon, new Label(texto));
                    
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
        
        colUltimaConexion.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getUltimaConexion();
            return new javafx.beans.property.SimpleStringProperty(
                fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Nunca"
            );
        });
        
        // Configurar columna de acciones
        configurarColumnaAcciones();
        
        // Ajustar ancho de la columna de acciones para iconos con espacio cómodo
        colAcciones.setPrefWidth(150);
        colAcciones.setMaxWidth(150);
        colAcciones.setMinWidth(150);
        
        // Configurar selección
        tableBibliotecarios.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(new Callback<TableColumn<Usuario, Void>, TableCell<Usuario, Void>>() {
            @Override
            public TableCell<Usuario, Void> call(TableColumn<Usuario, Void> param) {
                return new TableCell<Usuario, Void>() {
                    private final Button btnEditar = new Button();
                    private final Button btnEliminar = new Button();
                    private final Button btnToggleEstado = new Button();
                    
                    {
                        // Configurar iconos simples y visibles - SIN texto, SOLO iconos
                        btnEditar.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EDIT, IconHelper.MEDIUM_SIZE, IconHelper.PRIMARY_COLOR));
                        btnEliminar.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TRASH, IconHelper.MEDIUM_SIZE, IconHelper.ERROR_COLOR));
                        
                        // Estilo minimalista - solo botones transparentes con iconos coloridos
                        btnEditar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                        btnEliminar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                        
                        // Tamaños apropiados
                        btnEditar.setPrefSize(32, 32);
                        btnEliminar.setPrefSize(32, 32);
                        btnToggleEstado.setPrefSize(32, 32);
                        
                        // Tooltips simples y útiles
                        btnEditar.setTooltip(new javafx.scene.control.Tooltip("Editar bibliotecario"));
                        btnEliminar.setTooltip(new javafx.scene.control.Tooltip("Eliminar bibliotecario"));
                        
                        btnEditar.setOnAction(event -> {
                            Usuario usuario = getTableView().getItems().get(getIndex());
                            editarBibliotecario(usuario);
                        });
                        
                        btnEliminar.setOnAction(event -> {
                            Usuario usuario = getTableView().getItems().get(getIndex());
                            eliminarBibliotecario(usuario);
                        });
                        
                        btnToggleEstado.setOnAction(event -> {
                            Usuario usuario = getTableView().getItems().get(getIndex());
                            toggleEstadoBibliotecario(usuario);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Usuario usuario = getTableView().getItems().get(getIndex());
                            
                            // Configurar botón toggle simple y visible
                            if (usuario.isActivo()) {
                                btnToggleEstado.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PAUSE_CIRCLE, IconHelper.MEDIUM_SIZE, IconHelper.WARNING_COLOR));
                                btnToggleEstado.setTooltip(new javafx.scene.control.Tooltip("Desactivar bibliotecario"));
                            } else {
                                btnToggleEstado.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PLAY_CIRCLE, IconHelper.MEDIUM_SIZE, IconHelper.SUCCESS_COLOR));
                                btnToggleEstado.setTooltip(new javafx.scene.control.Tooltip("Activar bibliotecario"));
                            }
                            
                            // Estilo transparente como los otros botones
                            btnToggleEstado.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                            
                            javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(12);
                            hbox.setAlignment(javafx.geometry.Pos.CENTER);
                            hbox.getChildren().addAll(btnEditar, btnToggleEstado, btnEliminar);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }
    
    private void configurarFiltros() {
        // Configurar ComboBox de estado
        cmbEstado.setItems(FXCollections.observableArrayList(
            "Todos", "Activos", "Inactivos"
        ));
        cmbEstado.setValue("Todos");
        
        // Configurar búsqueda en tiempo real
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            aplicarFiltros();
        });
    }
    
    private void cargarBibliotecarios() {
        try {
            List<Usuario> listaBibliotecarios = usuarioService.obtenerUsuariosPorTipo(TipoUsuario.BIBLIOTECARIO);
            bibliotecarios = FXCollections.observableArrayList(listaBibliotecarios);
            bibliotecariosFiltrados = new FilteredList<>(bibliotecarios);
            tableBibliotecarios.setItems(bibliotecariosFiltrados);
            
            actualizarEstadisticas();
            actualizarContadores();
            
            System.out.println("📊 Cargados " + listaBibliotecarios.size() + " bibliotecarios");
            
        } catch (Exception e) {
            System.err.println("❌ Error cargando bibliotecarios: " + e.getMessage());
            mostrarError("Error", "No se pudieron cargar los bibliotecarios");
        }
    }
    
    private void aplicarFiltros() {
        bibliotecariosFiltrados.setPredicate(usuario -> {
            // Filtro de texto
            String busqueda = txtBuscar.getText().toLowerCase();
            boolean coincideTexto = busqueda.isEmpty() || 
                usuario.getNombre().toLowerCase().contains(busqueda) ||
                usuario.getApellido().toLowerCase().contains(busqueda) ||
                usuario.getEmail().toLowerCase().contains(busqueda) ||
                (usuario.getTelefono() != null && usuario.getTelefono().toLowerCase().contains(busqueda));
            
            // Filtro de estado
            String estadoFiltro = cmbEstado.getValue();
            boolean coincideEstado = "Todos".equals(estadoFiltro) ||
                ("Activos".equals(estadoFiltro) && usuario.isActivo()) ||
                ("Inactivos".equals(estadoFiltro) && !usuario.isActivo());
            
            return coincideTexto && coincideEstado;
        });
        
        actualizarContadores();
    }
    
    private void actualizarEstadisticas() {
        if (bibliotecarios != null) {
            int total = bibliotecarios.size();
            int activos = (int) bibliotecarios.stream().filter(Usuario::isActivo).count();
            int inactivos = total - activos;
            
            lblTotalBibliotecarios.setText(String.valueOf(total));
            lblBibliotecariasActivos.setText(String.valueOf(activos));
            lblBibliotecariasInactivos.setText(String.valueOf(inactivos));
        }
    }
    
    private void actualizarContadores() {
        if (bibliotecariosFiltrados != null) {
            lblTotal.setText("Total: " + bibliotecariosFiltrados.size() + " bibliotecarios");
        }
    }
    
    @FXML
    private void handleNuevoBibliotecario() {
        System.out.println("➕ Abriendo formulario para nuevo bibliotecario");
        abrirFormularioBibliotecario(null);
    }
    
    @FXML
    private void handleBuscar() {
        aplicarFiltros();
    }
    
    @FXML
    private void handleFiltroEstado() {
        aplicarFiltros();
    }
    
    @FXML
    private void handleRefrescar() {
        System.out.println("🔄 Refrescando lista de bibliotecarios");
        cargarBibliotecarios();
        
        // ELIMINADO: Notificación innecesaria de lista actualizada
    }
    
    // Métodos de exportar eliminados - funcionalidad no requerida
    
    private void abrirFormularioBibliotecario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/bibliotecario-form-view.fxml"));
            Parent root = loader.load();
            
            BibliotecarioFormController controller = loader.getController();
            controller.setUsuario(usuario);
            
            Stage stage = new Stage();
            stage.setTitle(usuario == null ? "Nuevo Bibliotecario" : "Editar Bibliotecario");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(org.kordamp.bootstrapfx.BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/example/demo2/styles/main.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            
            // Tamaño de ventana ampliado para mostrar todo el contenido sin scroll
            stage.setWidth(900);
            stage.setHeight(950);
            stage.setMinWidth(850);
            stage.setMinHeight(900);
            stage.setMaxWidth(1000);
            stage.setMaxHeight(1000);
            
            // Callback para actualizar la lista cuando se cierre el formulario
            stage.setOnHidden(e -> {
                cargarBibliotecarios();
                System.out.println("🔄 Lista de bibliotecarios actualizada");
            });
            
            stage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("❌ Error abriendo formulario de bibliotecario: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir el formulario");
        }
    }
    
    private void editarBibliotecario(Usuario usuario) {
        System.out.println("✏️ Editando bibliotecario: " + usuario.getEmail());
        abrirFormularioBibliotecario(usuario);
    }
    
    private void eliminarBibliotecario(Usuario usuario) {
        System.out.println("🗑️ Solicitando eliminación de bibliotecario: " + usuario.getEmail());
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Eliminación");
        confirmAlert.setHeaderText("¿Eliminar bibliotecario?");
        confirmAlert.setContentText("¿Está seguro de que desea eliminar al bibliotecario?\n\n" +
            "Nombre: " + usuario.getNombre() + " " + usuario.getApellido() + "\n" +
            "Email: " + usuario.getEmail() + "\n\n" +
            "Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean eliminado = usuarioService.eliminarUsuario(usuario.getId());
                
                if (eliminado) {
                    bibliotecarios.remove(usuario);
                    actualizarEstadisticas();
                    actualizarContadores();
                    
                    notificationService.notifyWarning(
                        "Bibliotecario Eliminado",
                        "El bibliotecario " + usuario.getNombreCompleto() + " ha sido eliminado"
                    );
                    
                    System.out.println("✅ Bibliotecario eliminado exitosamente");
                } else {
                    mostrarError("Error", "No se pudo eliminar el bibliotecario");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error eliminando bibliotecario: " + e.getMessage());
                mostrarError("Error", "Error al eliminar el bibliotecario: " + e.getMessage());
            }
        }
    }
    
    private void toggleEstadoBibliotecario(Usuario usuario) {
        try {
            boolean nuevoEstado = !usuario.isActivo();
            usuario.setActivo(nuevoEstado);
            
            boolean actualizado = usuarioService.actualizarUsuario(usuario);
            
            if (actualizado) {
                tableBibliotecarios.refresh();
                actualizarEstadisticas();
                
                String mensaje = nuevoEstado ? "activado" : "desactivado";
                // ELIMINADO: Notificación innecesaria de cambio de estado
                
                System.out.println("🔄 Estado del bibliotecario actualizado: " + mensaje);
            } else {
                // Revertir cambio si falló
                usuario.setActivo(!nuevoEstado);
                mostrarError("Error", "No se pudo actualizar el estado del bibliotecario");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error actualizando estado: " + e.getMessage());
            mostrarError("Error", "Error al actualizar el estado: " + e.getMessage());
        }
    }
    
    /**
     * Método llamado desde el formulario para actualizar la lista
     */
    public void actualizarLista() {
        cargarBibliotecarios();
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 