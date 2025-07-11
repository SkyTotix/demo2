package com.example.demo2.controller;

import com.example.demo2.models.Usuario;
import com.example.demo2.models.enums.TipoUsuario;
import com.example.demo2.service.NotificationService;
import com.example.demo2.service.UsuarioService;
import com.example.demo2.utils.IconHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de administradores
 */
public class AdminManagementController {
    
    @FXML private TableView<Usuario> tableAdmins;
    @FXML private TableColumn<Usuario, Long> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private TableColumn<Usuario, String> colFechaCreacion;
    @FXML private TableColumn<Usuario, String> colUltimoAcceso;
    @FXML private TableColumn<Usuario, Void> colAcciones;
    
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnNuevoAdmin;
    @FXML private Button btnLimpiar;
    
    @FXML private Label lblTotalAdmins;
    @FXML private Label lblAdminsActivos;
    @FXML private Label lblAdminsInactivos;
    
    private UsuarioService usuarioService;
    private NotificationService notificationService;
    private ObservableList<Usuario> administradores;
    private FilteredList<Usuario> administradoresFiltrados;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @FXML
    public void initialize() {
        usuarioService = UsuarioService.getInstance();
        notificationService = NotificationService.getInstance();
        administradores = FXCollections.observableArrayList();
        
        configurarTabla();
        configurarFiltros();
        cargarAdministradores();
        actualizarEstadisticas();
    }
    
    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        // Columna de estado con iconos
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
                    hbox.getChildren().addAll(icon, new javafx.scene.control.Label(texto));
                    
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
        
        // Columna de fecha de creación
        colFechaCreacion.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaCreacion() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getFechaCreacion().toLocalDateTime().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Columna de último acceso
        colUltimoAcceso.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUltimoAcceso() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getUltimoAcceso().toLocalDateTime().format(dateFormatter));
            }
            return new SimpleStringProperty("Nunca");
        });
        
        // Columna de acciones con iconos de Ikonli
        colAcciones.setCellFactory(param -> new TableCell<Usuario, Void>() {
            private final Button btnEditar = new Button();
            private final Button btnToggleEstado = new Button();
            private final Button btnEliminar = new Button();
            private final HBox hbox = new HBox(12, btnEditar, btnToggleEstado, btnEliminar);
            
            {
                // Configurar iconos visibles sin texto
                btnEditar.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EDIT, IconHelper.MEDIUM_SIZE, IconHelper.PRIMARY_COLOR));
                btnEliminar.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TRASH, IconHelper.MEDIUM_SIZE, IconHelper.ERROR_COLOR));
                
                // Estilo transparente para que solo se vean los iconos
                btnEditar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                btnToggleEstado.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                
                // Tamaños apropiados
                btnEditar.setPrefSize(32, 32);
                btnEliminar.setPrefSize(32, 32);
                btnToggleEstado.setPrefSize(32, 32);
                
                // Tooltips útiles
                btnEditar.setTooltip(new Tooltip("Editar administrador"));
                btnEliminar.setTooltip(new Tooltip("Eliminar administrador"));
                
                // Centrar los botones
                hbox.setAlignment(javafx.geometry.Pos.CENTER);
                
                btnEditar.setOnAction(e -> {
                    Usuario admin = getTableView().getItems().get(getIndex());
                    editarAdministrador(admin);
                });
                
                btnEliminar.setOnAction(e -> {
                    Usuario admin = getTableView().getItems().get(getIndex());
                    eliminarAdministrador(admin);
                });
                
                btnToggleEstado.setOnAction(e -> {
                    Usuario admin = getTableView().getItems().get(getIndex());
                    toggleEstadoAdministrador(admin);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Usuario admin = getTableView().getItems().get(getIndex());
                    
                    // Configurar botón toggle con iconos dinámicos
                    if (admin.isActivo()) {
                        btnToggleEstado.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PAUSE_CIRCLE, IconHelper.MEDIUM_SIZE, IconHelper.WARNING_COLOR));
                        btnToggleEstado.setTooltip(new Tooltip("Desactivar administrador"));
                    } else {
                        btnToggleEstado.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PLAY_CIRCLE, IconHelper.MEDIUM_SIZE, IconHelper.SUCCESS_COLOR));
                        btnToggleEstado.setTooltip(new Tooltip("Activar administrador"));
                    }
                    
                    setGraphic(hbox);
                }
            }
        });
        
        // Ajustar ancho de la columna de acciones para 3 iconos
        colAcciones.setPrefWidth(160);
        colAcciones.setMaxWidth(160);
        colAcciones.setMinWidth(160);
        
        // Configurar lista filtrada
        administradoresFiltrados = new FilteredList<>(administradores);
        tableAdmins.setItems(administradoresFiltrados);
    }
    
    private void configurarFiltros() {
        // Configurar ComboBox de estado
        cmbEstado.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        cmbEstado.setValue("Todos");
        
        // Configurar filtro de búsqueda
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cmbEstado.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
    }
    
    private void aplicarFiltros() {
        administradoresFiltrados.setPredicate(admin -> {
            // Filtro de texto
            String filtroTexto = txtBuscar.getText();
            if (filtroTexto != null && !filtroTexto.isEmpty()) {
                String textoLower = filtroTexto.toLowerCase();
                if (!admin.getNombre().toLowerCase().contains(textoLower) &&
                    !admin.getApellido().toLowerCase().contains(textoLower) &&
                    !admin.getEmail().toLowerCase().contains(textoLower) &&
                    !admin.getUsername().toLowerCase().contains(textoLower)) {
                    return false;
                }
            }
            
            // Filtro de estado
            String filtroEstado = cmbEstado.getValue();
            if (filtroEstado != null && !filtroEstado.equals("Todos")) {
                boolean esActivo = filtroEstado.equals("Activo");
                if (admin.isActivo() != esActivo) {
                    return false;
                }
            }
            
            return true;
        });
        
        actualizarEstadisticas();
    }
    
    private void cargarAdministradores() {
        try {
            // Cargar solo usuarios de tipo ADMIN
            List<Usuario> admins = usuarioService.buscarPorTipo(TipoUsuario.ADMIN);
            administradores.setAll(admins);
            System.out.println("✅ Cargados " + admins.size() + " administradores");
        } catch (Exception e) {
            System.err.println("❌ Error cargando administradores: " + e.getMessage());
            mostrarError("Error al cargar administradores", e.getMessage());
        }
    }
    
    private void actualizarEstadisticas() {
        int total = administradoresFiltrados.size();
        int activos = (int) administradoresFiltrados.stream().filter(Usuario::isActivo).count();
        int inactivos = total - activos;
        
        lblTotalAdmins.setText("Total: " + total + " administradores");
        lblAdminsActivos.setText("Activos: " + activos);
        lblAdminsInactivos.setText("Inactivos: " + inactivos);
    }
    
    @FXML
    private void handleNuevoAdmin() {
        abrirFormularioAdmin(null);
    }
    
    @FXML
    private void handleBuscar() {
        // El filtro se aplica automáticamente por el listener
    }
    
    @FXML
    private void handleFiltrar() {
        // El filtro se aplica automáticamente por el listener
    }
    
    @FXML
    private void handleLimpiarFiltros() {
        txtBuscar.clear();
        cmbEstado.setValue("Todos");
    }
    
    private void editarAdministrador(Usuario admin) {
        abrirFormularioAdmin(admin);
    }
    
    private void eliminarAdministrador(Usuario admin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar Administrador?");
        alert.setContentText("¿Está seguro de que desea eliminar al administrador " + 
                           admin.getNombreCompleto() + "?\n\nEsta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                usuarioService.eliminar(admin.getId());
                administradores.remove(admin);
                actualizarEstadisticas();
                mostrarExito("Administrador eliminado exitosamente");
                
                // Notificación
                notificationService.notifyWarning(
                    "Administrador Eliminado",
                    "Se eliminó al administrador " + admin.getNombreCompleto()
                );
                
                System.out.println("✅ Administrador eliminado: " + admin.getUsername());
            } catch (Exception e) {
                System.err.println("❌ Error eliminando administrador: " + e.getMessage());
                mostrarError("Error al eliminar administrador", e.getMessage());
                
                // Notificación de error
                notificationService.notifyError(
                    "Error al Eliminar",
                    "No se pudo eliminar al administrador " + admin.getNombreCompleto()
                );
            }
        }
    }
    
    private void abrirFormularioAdmin(Usuario adminParaEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/admin-form-view.fxml"));
            Parent root = loader.load();
            
            AdminFormController controller = loader.getController();
            controller.setAdminManagementController(this);
            
            if (adminParaEditar != null) {
                controller.setAdministradorParaEditar(adminParaEditar);
            }
            
            Stage stage = new Stage();
            stage.setTitle(adminParaEditar == null ? "Nuevo Administrador" : "Editar Administrador");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnNuevoAdmin.getScene().getWindow());
            
            Scene scene = new Scene(root, 600, 700);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/example/demo2/styles/main.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("❌ Error abriendo formulario: " + e.getMessage());
            mostrarError("Error al abrir formulario", e.getMessage());
        }
    }
    
    public void actualizarListaAdministradores() {
        cargarAdministradores();
        actualizarEstadisticas();
    }
    
    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void toggleEstadoAdministrador(Usuario admin) {
        try {
            boolean nuevoEstado = !admin.isActivo();
            admin.setActivo(nuevoEstado);
            
            boolean actualizado = usuarioService.actualizarUsuario(admin);
            
            if (actualizado) {
                tableAdmins.refresh();
                actualizarEstadisticas();
                
                String mensaje = nuevoEstado ? "activado" : "desactivado";
                String accion = nuevoEstado ? "Activado" : "Desactivado";
                
                notificationService.notifyInfo(
                    "Administrador " + accion,
                    "El administrador " + admin.getNombreCompleto() + " ha sido " + mensaje
                );
                
                System.out.println("🔄 Estado del administrador actualizado: " + mensaje);
            } else {
                // Revertir cambio si falló
                admin.setActivo(!nuevoEstado);
                mostrarError("Error", "No se pudo actualizar el estado del administrador");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error actualizando estado del administrador: " + e.getMessage());
            mostrarError("Error", "Error al actualizar el estado: " + e.getMessage());
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