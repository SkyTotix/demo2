package com.example.demo2.controller;

import com.example.demo2.models.Libro;
import com.example.demo2.service.LibroService;
import com.example.demo2.service.NotificationService;
import com.example.demo2.utils.IconHelper;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.Optional;

/**
 * Controlador para la gestión de libros
 */
public class LibroManagementController {
    
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCriterioBusqueda;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnActualizar;
    @FXML private Button btnNuevoLibro;
    @FXML private Button btnExportarExcel;
    @FXML private Button btnImportarLibros;
    
    @FXML private Label lblTotalLibros;
    @FXML private Label lblLibrosActivos;
    @FXML private Label lblLibrosDisponibles;
    @FXML private Label lblLibrosPrestados;
    @FXML private Label lblResultados;
    @FXML private Label lblUltimaActualizacion;
    
    @FXML private TableView<Libro> tableLibros;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colEditorial;
    @FXML private TableColumn<Libro, String> colCategoria;
    @FXML private TableColumn<Libro, Integer> colAnio;
    @FXML private TableColumn<Libro, Integer> colCantidadTotal;
    @FXML private TableColumn<Libro, Integer> colCantidadDisponible;
    @FXML private TableColumn<Libro, String> colEstado;
    @FXML private TableColumn<Libro, Void> colAcciones;
    
    private LibroService libroService;
    private NotificationService notificationService;
    private FilteredList<Libro> librosFiltrados;
    
    @FXML
    public void initialize() {
        libroService = LibroService.getInstance();
        notificationService = NotificationService.getInstance();
        
        // Configurar tabla
        configurarTabla();
        
        // Configurar filtros
        configurarFiltros();
        
        // Configurar eventos
        configurarEventos();
        
        // Cargar datos iniciales
        cargarLibros();
        
        // Actualizar estadísticas
        actualizarEstadisticas();
        
        // Crear libros de ejemplo si no hay ninguno
        libroService.crearLibrosDePrueba();
        
        // Actualizar timestamp
        actualizarTimestamp();
        
        System.out.println("📚 Controlador de gestión de libros inicializado");
    }
    
    private void configurarTabla() {
        // Configurar columnas básicas
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colEditorial.setCellValueFactory(new PropertyValueFactory<>("editorial"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioPublicacion"));
        colCantidadTotal.setCellValueFactory(new PropertyValueFactory<>("cantidadTotal"));
        colCantidadDisponible.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));
        
        // Columna de estado con iconos
        colEstado.setCellFactory(column -> new TableCell<Libro, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Libro libro = getTableRow().getItem();
                    HBox hbox = new HBox(5);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    FontIcon estadoIcon;
                    String textoEstado;
                    
                    if (!libro.isActivo()) {
                        estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.ERROR_COLOR);
                        textoEstado = "Inactivo";
                    } else if (libro.getCantidadDisponible() == 0) {
                        estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_TRIANGLE, IconHelper.SMALL_SIZE, IconHelper.WARNING_COLOR);
                        textoEstado = "Agotado";
                    } else if (libro.getCantidadDisponible() < libro.getCantidadTotal() / 2) {
                        estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.WARNING_COLOR);
                        textoEstado = "Pocos";
                    } else {
                        estadoIcon = IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE, IconHelper.SMALL_SIZE, IconHelper.SUCCESS_COLOR);
                        textoEstado = "Disponible";
                    }
                    
                    hbox.getChildren().addAll(estadoIcon, new Label(textoEstado));
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
        
        // Configurar columna de acciones
        configurarColumnaAcciones();
        
        // Ajustar anchos de columnas y política de redimensionamiento
        ajustarAnchoColumnas();
        
        // Configurar política de redimensionamiento para que use todo el espacio
        tableLibros.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }
    
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<Libro, Void>() {
            private final Button btnEditar = new Button();
            private final Button btnToggleEstado = new Button();
            private final Button btnEliminar = new Button();
            private final HBox hbox = new HBox(5);
            
            {
                // Configurar iconos de botones
                btnEditar.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EDIT, IconHelper.MEDIUM_SIZE, IconHelper.PRIMARY_COLOR));
                btnEliminar.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TRASH, IconHelper.MEDIUM_SIZE, IconHelper.ERROR_COLOR));
                
                // Estilos de botones
                btnEditar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                btnToggleEstado.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                
                // Tamaños de botones más compactos
                btnEditar.setPrefSize(28, 28);
                btnToggleEstado.setPrefSize(28, 28);
                btnEliminar.setPrefSize(28, 28);
                
                // Tooltips
                btnEditar.setTooltip(new Tooltip("Editar libro"));
                btnEliminar.setTooltip(new Tooltip("Eliminar libro"));
                
                // Eventos
                btnEditar.setOnAction(e -> {
                    Libro libro = getTableView().getItems().get(getIndex());
                    editarLibro(libro);
                });
                
                btnToggleEstado.setOnAction(e -> {
                    Libro libro = getTableView().getItems().get(getIndex());
                    toggleEstadoLibro(libro);
                });
                
                btnEliminar.setOnAction(e -> {
                    Libro libro = getTableView().getItems().get(getIndex());
                    eliminarLibro(libro);
                });
                
                hbox.setAlignment(javafx.geometry.Pos.CENTER);
                hbox.getChildren().addAll(btnEditar, btnToggleEstado, btnEliminar);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Libro libro = getTableRow().getItem();
                    
                    // Configurar botón de estado dinámicamente
                    if (libro.isActivo()) {
                        btnToggleEstado.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PAUSE_CIRCLE, IconHelper.MEDIUM_SIZE, IconHelper.WARNING_COLOR));
                        btnToggleEstado.setTooltip(new Tooltip("Desactivar libro"));
                    } else {
                        btnToggleEstado.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PLAY_CIRCLE, IconHelper.MEDIUM_SIZE, IconHelper.SUCCESS_COLOR));
                        btnToggleEstado.setTooltip(new Tooltip("Activar libro"));
                    }
                    
                    setGraphic(hbox);
                }
            }
        });
    }
    
    private void ajustarAnchoColumnas() {
        // Configurar anchos específicos para cada columna
        // ISBN - corta, números fijos
        colIsbn.setPrefWidth(115);
        colIsbn.setMinWidth(100);
        colIsbn.setMaxWidth(130);
        
        // Título - la más ancha, contenido variable
        colTitulo.setPrefWidth(270);
        colTitulo.setMinWidth(200);
        
        // Autor - mediana
        colAutor.setPrefWidth(170);
        colAutor.setMinWidth(140);
        colAutor.setMaxWidth(210);
        
        // Editorial - mediana
        colEditorial.setPrefWidth(130);
        colEditorial.setMinWidth(100);
        colEditorial.setMaxWidth(170);
        
        // Categoría - pequeña
        colCategoria.setPrefWidth(95);
        colCategoria.setMinWidth(80);
        colCategoria.setMaxWidth(115);
        
        // Año - muy pequeña, números
        colAnio.setPrefWidth(55);
        colAnio.setMinWidth(50);
        colAnio.setMaxWidth(65);
        
        // Cantidad Total - pequeña, números
        colCantidadTotal.setPrefWidth(55);
        colCantidadTotal.setMinWidth(50);
        colCantidadTotal.setMaxWidth(70);
        
        // Cantidad Disponible - pequeña, números  
        colCantidadDisponible.setPrefWidth(65);
        colCantidadDisponible.setMinWidth(60);
        colCantidadDisponible.setMaxWidth(80);
        
        // Estado - mediana para iconos y texto
        colEstado.setPrefWidth(95);
        colEstado.setMinWidth(90);
        colEstado.setMaxWidth(110);
        
        // Acciones - más ancha para que quepan bien los 3 botones
        colAcciones.setPrefWidth(140);
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setResizable(false);
        
        // Hacer que el título sea la columna que más se expanda
        colTitulo.setMaxWidth(Double.MAX_VALUE);
    }
    
    private void configurarFiltros() {
        // Configurar criterio de búsqueda
        cmbCriterioBusqueda.getItems().addAll("titulo", "autor", "isbn", "editorial", "categoria");
        cmbCriterioBusqueda.setValue("titulo");
        
        // Configurar filtro de estado
        cmbFiltroEstado.getItems().addAll("Todos", "Activos", "Inactivos", "Disponibles", "Agotados");
        cmbFiltroEstado.setValue("Todos");
        
        // Cargar categorías dinámicamente
        cargarCategorias();
    }
    
    private void cargarCategorias() {
        cmbFiltroCategoria.getItems().clear();
        cmbFiltroCategoria.getItems().add("Todas");
        cmbFiltroCategoria.getItems().addAll(libroService.obtenerCategorias());
        cmbFiltroCategoria.setValue("Todas");
    }
    
    private void configurarEventos() {
        // Botones principales
        btnNuevoLibro.setOnAction(e -> nuevoLibro());
        btnBuscar.setOnAction(e -> aplicarFiltros());
        btnLimpiarBusqueda.setOnAction(e -> limpiarBusqueda());
        btnActualizar.setOnAction(e -> {
            cargarLibros();
            actualizarTimestamp();
        });
        
        // Eventos de filtros
        cmbFiltroCategoria.setOnAction(e -> aplicarFiltros());
        cmbFiltroEstado.setOnAction(e -> aplicarFiltros());
        txtBuscar.setOnKeyReleased(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                aplicarFiltros();
            }
        });
        
        // Botones de footer (placeholder)
        btnExportarExcel.setOnAction(e -> mostrarMensajeDesarrollo("Exportar a Excel"));
        btnImportarLibros.setOnAction(e -> mostrarMensajeDesarrollo("Importar Libros"));
        
        // Doble click en tabla para editar
        tableLibros.setRowFactory(tv -> {
            TableRow<Libro> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    editarLibro(row.getItem());
                }
            });
            return row;
        });
    }
    
    private void cargarLibros() {
        // Crear lista filtrada
        librosFiltrados = new FilteredList<>(libroService.obtenerTodosLosLibros());
        tableLibros.setItems(librosFiltrados);
        
        // Aplicar filtros actuales
        aplicarFiltros();
        
        // Actualizar estadísticas
        actualizarEstadisticas();
        
        System.out.println("📚 Libros cargados: " + librosFiltrados.size());
    }
    
    private void aplicarFiltros() {
        if (librosFiltrados == null) return;
        
        String textoBusqueda = txtBuscar.getText().toLowerCase().trim();
        String criterio = cmbCriterioBusqueda.getValue();
        String filtroCategoria = cmbFiltroCategoria.getValue();
        String filtroEstado = cmbFiltroEstado.getValue();
        
        librosFiltrados.setPredicate(libro -> {
            // Filtro de búsqueda
            if (!textoBusqueda.isEmpty()) {
                String valorCampo = "";
                switch (criterio) {
                    case "titulo":
                        valorCampo = libro.getTitulo();
                        break;
                    case "autor":
                        valorCampo = libro.getAutor();
                        break;
                    case "isbn":
                        valorCampo = libro.getIsbn();
                        break;
                    case "editorial":
                        valorCampo = libro.getEditorial();
                        break;
                    case "categoria":
                        valorCampo = libro.getCategoria();
                        break;
                }
                
                if (valorCampo == null || !valorCampo.toLowerCase().contains(textoBusqueda)) {
                    return false;
                }
            }
            
            // Filtro de categoría
            if (filtroCategoria != null && !filtroCategoria.equals("Todas")) {
                if (!filtroCategoria.equals(libro.getCategoria())) {
                    return false;
                }
            }
            
            // Filtro de estado
            if (filtroEstado != null && !filtroEstado.equals("Todos")) {
                switch (filtroEstado) {
                    case "Activos":
                        if (!libro.isActivo()) return false;
                        break;
                    case "Inactivos":
                        if (libro.isActivo()) return false;
                        break;
                    case "Disponibles":
                        if (libro.getCantidadDisponible() == 0) return false;
                        break;
                    case "Agotados":
                        if (libro.getCantidadDisponible() > 0) return false;
                        break;
                }
            }
            
            return true;
        });
        
        // Actualizar etiqueta de resultados
        lblResultados.setText("Mostrando " + librosFiltrados.size() + " libros");
    }
    
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cmbCriterioBusqueda.setValue("titulo");
        cmbFiltroCategoria.setValue("Todas");
        cmbFiltroEstado.setValue("Todos");
        aplicarFiltros();
    }
    
    private void actualizarEstadisticas() {
        if (librosFiltrados == null) return;
        
        int total = librosFiltrados.getSource().size();
        int activos = (int) librosFiltrados.getSource().stream().filter(Libro::isActivo).count();
        int disponibles = (int) librosFiltrados.getSource().stream().filter(libro -> libro.getCantidadDisponible() > 0).count();
        int prestados = librosFiltrados.getSource().stream().mapToInt(Libro::getCantidadPrestada).sum();
        
        lblTotalLibros.setText(String.valueOf(total));
        lblLibrosActivos.setText(String.valueOf(activos));
        lblLibrosDisponibles.setText(String.valueOf(disponibles));
        lblLibrosPrestados.setText(String.valueOf(prestados));
    }
    
    private void actualizarTimestamp() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        lblUltimaActualizacion.setText("Última actualización: " + timestamp);
    }
    
    private void nuevoLibro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/libro-form-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Nuevo Libro");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnNuevoLibro.getScene().getWindow());
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/example/demo2/styles/main.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setResizable(true);
            
            // Configurar tamaño del modal
            stage.setWidth(700);
            stage.setHeight(650);
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.setMaxWidth(800);
            stage.setMaxHeight(700);
            
            // Configurar controlador
            LibroFormController controller = loader.getController();
            controller.setParentController(this);
            
            stage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("❌ Error abriendo formulario de libro: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir el formulario de nuevo libro");
        }
    }
    
    private void editarLibro(Libro libro) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo2/libro-form-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Editar Libro - " + libro.getTitulo());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableLibros.getScene().getWindow());
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/example/demo2/styles/main.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setResizable(true);
            
            // Configurar tamaño del modal
            stage.setWidth(700);
            stage.setHeight(650);
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.setMaxWidth(800);
            stage.setMaxHeight(700);
            
            // Configurar controlador con datos del libro
            LibroFormController controller = loader.getController();
            controller.setParentController(this);
            controller.setLibro(libro);
            
            stage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("❌ Error abriendo formulario de edición: " + e.getMessage());
            mostrarError("Error", "No se pudo abrir el formulario de edición");
        }
    }
    
    private void toggleEstadoLibro(Libro libro) {
        boolean nuevoEstado = !libro.isActivo();
        libro.setActivo(nuevoEstado);
        
        if (libroService.actualizarLibro(libro)) {
            tableLibros.refresh();
            actualizarEstadisticas();
            
            String mensaje = nuevoEstado ? "activado" : "desactivado";
            String accion = nuevoEstado ? "Activado" : "Desactivado";
            
            notificationService.addNotification(
                "Libro " + accion,
                "El libro \"" + libro.getTitulo() + "\" ha sido " + mensaje,
                NotificationService.NotificationType.SUCCESS
            );
            
            System.out.println("✅ Estado del libro actualizado: " + mensaje);
        } else {
            // Revertir el cambio si falló
            libro.setActivo(!nuevoEstado);
            mostrarError("Error", "No se pudo actualizar el estado del libro");
        }
    }
    
    private void eliminarLibro(Libro libro) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Eliminación");
        confirmAlert.setHeaderText("¿Eliminar libro?");
        confirmAlert.setContentText("¿Está seguro de que desea eliminar el libro \"" + libro.getTitulo() + "\"?\n\nEsta acción no se puede deshacer.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (libroService.eliminarLibro(libro.getId())) {
                cargarLibros(); // Recargar para mostrar cambios
                actualizarEstadisticas();
                
                notificationService.addNotification(
                    "Libro Eliminado",
                    "El libro \"" + libro.getTitulo() + "\" ha sido eliminado",
                    NotificationService.NotificationType.SUCCESS
                );
                
                System.out.println("✅ Libro eliminado: " + libro.getTitulo());
            } else {
                mostrarError("Error", "No se pudo eliminar el libro");
            }
        }
    }
    
    /**
     * Método llamado desde el formulario para actualizar la tabla
     */
    public void actualizarTabla() {
        cargarLibros();
        actualizarTimestamp();
    }
    
    private void mostrarMensajeDesarrollo(String funcionalidad) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("En Desarrollo");
        alert.setHeaderText(funcionalidad);
        alert.setContentText("Esta funcionalidad está en desarrollo y estará disponible próximamente.");
        alert.showAndWait();
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 