package com.example.demo2.controller;

import com.example.demo2.models.Prestamo;
import com.example.demo2.models.Libro;
import com.example.demo2.models.Lector;
import com.example.demo2.service.PrestamoService;
import com.example.demo2.service.LibroService;
import com.example.demo2.service.LectorService;
import com.example.demo2.service.NotificationService;
import com.example.demo2.utils.IconHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para el formulario de préstamos
 */
public class PrestamoFormController implements Initializable {
    
    @FXML private TextField codigoField;
    @FXML private ComboBox<Libro> libroCombo;
    @FXML private ComboBox<Lector> lectorCombo;
    @FXML private DatePicker fechaDevolucionPicker;
    @FXML private ComboBox<String> condicionCombo;
    @FXML private TextArea observacionesArea;
    @FXML private Button guardarBtn;
    @FXML private Button cancelarBtn;
    @FXML private Label tituloLabel;
    @FXML private Label errorLabel;
    
    private PrestamoService prestamoService;
    private LibroService libroService;
    private LectorService lectorService;
    private NotificationService notificationService;
    private Prestamo prestamo;
    private PrestamoManagementController parentController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prestamoService = PrestamoService.getInstance();
        libroService = LibroService.getInstance();
        lectorService = LectorService.getInstance();
        notificationService = NotificationService.getInstance();
        
        configurarCampos();
        cargarDatos();
        configurarBotones();
    }
    
    /**
     * Configura los campos del formulario
     */
    private void configurarCampos() {
        // Configurar campo código como solo lectura
        codigoField.setEditable(false);
        codigoField.setStyle("-fx-background-color: #f8f9fa;");
        
        // Configurar combo de condición
        condicionCombo.getItems().addAll("Excelente", "Buena", "Regular", "Malo");
        condicionCombo.setValue("Excelente");
        
        // Configurar fecha de devolución (por defecto 15 días)
        fechaDevolucionPicker.setValue(LocalDate.now().plusDays(15));
        
        // Configurar combo de libros
        libroCombo.setConverter(new StringConverter<Libro>() {
            @Override
            public String toString(Libro libro) {
                return libro != null ? libro.getTitulo() + " - " + libro.getIsbn() : "";
            }
            
            @Override
            public Libro fromString(String string) {
                return null;
            }
        });
        
        // Configurar combo de lectores
        lectorCombo.setConverter(new StringConverter<Lector>() {
            @Override
            public String toString(Lector lector) {
                return lector != null ? lector.getNombre() + " " + lector.getApellido() + " (" + lector.getCodigoLector() + ")" : "";
            }
            
            @Override
            public Lector fromString(String string) {
                return null;
            }
        });
        
        // Configurar área de observaciones
        observacionesArea.setPromptText("Observaciones sobre el préstamo (opcional)");
        observacionesArea.setPrefRowCount(3);
        observacionesArea.setWrapText(true);
        
        // Listener para validar en tiempo real
        libroCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            validarDisponibilidad();
        });
        
        lectorCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            validarLector();
        });
    }
    
    /**
     * Carga los datos necesarios para el formulario
     */
    private void cargarDatos() {
        try {
            // Cargar libros disponibles
            List<Libro> libros = libroService.obtenerTodosLosLibros();
            libroCombo.getItems().clear();
            for (Libro libro : libros) {
                if (libro.isActivo() && libro.getCantidadDisponible() > 0) {
                    libroCombo.getItems().add(libro);
                }
            }
            
            // Cargar lectores activos
            List<Lector> lectores = lectorService.obtenerPorEstado("ACTIVO");
            lectorCombo.getItems().clear();
            lectorCombo.getItems().addAll(lectores);
            
            // Generar código de préstamo si es nuevo
            if (prestamo == null) {
                String codigo = prestamoService.generarCodigoPrestamo();
                codigoField.setText(codigo);
            }
            
        } catch (SQLException e) {
            notificationService.notifyError("Error al cargar datos", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Configura los botones del formulario
     */
    private void configurarBotones() {
        // Configurar botón guardar
        guardarBtn.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SAVE, 14));
        guardarBtn.setOnAction(e -> guardar());
        
        // Configurar botón cancelar
        cancelarBtn.setGraphic(IconHelper.createIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES, 14));
        cancelarBtn.setOnAction(e -> cancelar());
    }
    
    /**
     * Valida la disponibilidad del libro seleccionado
     */
    private void validarDisponibilidad() {
        if (libroCombo.getValue() != null) {
            try {
                Long libroId = (long) libroCombo.getValue().getId();
                if (!prestamoService.libroDisponible(libroId)) {
                    mostrarError("El libro seleccionado no está disponible para préstamo");
                    return;
                }
            } catch (SQLException e) {
                mostrarError("Error al validar disponibilidad del libro");
                return;
            }
        }
        limpiarError();
    }
    
    /**
     * Valida el lector seleccionado
     */
    private void validarLector() {
        if (lectorCombo.getValue() != null) {
            try {
                Long lectorId = (long) lectorCombo.getValue().getId();
                if (prestamoService.lectorTienePrestamosActivos(lectorId)) {
                    mostrarError("El lector seleccionado tiene préstamos activos pendientes");
                    return;
                }
            } catch (SQLException e) {
                mostrarError("Error al validar el lector");
                return;
            }
        }
        limpiarError();
    }
    
    /**
     * Guarda el préstamo
     */
    private void guardar() {
        if (!validarFormulario()) {
            return;
        }
        
        try {
            Prestamo nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setCodigoPrestamo(codigoField.getText());
            nuevoPrestamo.setLibroId((long) libroCombo.getValue().getId());
            nuevoPrestamo.setLectorId((long) lectorCombo.getValue().getId());
            nuevoPrestamo.setBibliotecarioPrestamoId(1L); // ID del bibliotecario actual
            nuevoPrestamo.setFechaDevolucionEsperada(fechaDevolucionPicker.getValue());
            nuevoPrestamo.setEstado("ACTIVO");
            nuevoPrestamo.setCondicionPrestamo(condicionCombo.getValue());
            nuevoPrestamo.setObservacionesPrestamo(observacionesArea.getText());
            
            Prestamo prestamoCreado = prestamoService.crear(nuevoPrestamo);
            
            if (prestamoCreado != null) {
                notificationService.notifySuccess("Préstamo creado", 
                    "El préstamo " + prestamoCreado.getCodigoPrestamo() + " ha sido creado exitosamente.");
                
                // Actualizar la tabla padre y cerrar ventana
                if (parentController != null) {
                    parentController.refrescarTabla();
                }
                cerrarVentana();
            } else {
                mostrarError("No se pudo crear el préstamo");
            }
        } catch (SQLException e) {
            mostrarError("Error al guardar préstamo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Valida el formulario antes de guardar
     */
    private boolean validarFormulario() {
        // Validar campos obligatorios
        if (codigoField.getText().trim().isEmpty()) {
            mostrarError("El código del préstamo es obligatorio");
            return false;
        }
        
        if (libroCombo.getValue() == null) {
            mostrarError("Debe seleccionar un libro");
            return false;
        }
        
        if (lectorCombo.getValue() == null) {
            mostrarError("Debe seleccionar un lector");
            return false;
        }
        
        if (fechaDevolucionPicker.getValue() == null) {
            mostrarError("Debe seleccionar una fecha de devolución");
            return false;
        }
        
        if (condicionCombo.getValue() == null) {
            mostrarError("Debe seleccionar la condición del libro");
            return false;
        }
        
        // Validar fecha de devolución
        if (fechaDevolucionPicker.getValue().isBefore(LocalDate.now())) {
            mostrarError("La fecha de devolución no puede ser anterior a hoy");
            return false;
        }
        
        // Validar disponibilidad del libro
        try {
            Long libroId = (long) libroCombo.getValue().getId();
            if (!prestamoService.libroDisponible(libroId)) {
                mostrarError("El libro seleccionado no está disponible para préstamo");
                return false;
            }
        } catch (SQLException e) {
            mostrarError("Error al validar disponibilidad del libro");
            return false;
        }
        
        // Validar estado del lector
        try {
            Long lectorId = (long) lectorCombo.getValue().getId();
            if (prestamoService.lectorTienePrestamosActivos(lectorId)) {
                mostrarError("El lector seleccionado tiene préstamos activos pendientes");
                return false;
            }
        } catch (SQLException e) {
            mostrarError("Error al validar el lector");
            return false;
        }
        
        return true;
    }
    
    /**
     * Cancela la operación y cierra la ventana
     */
    private void cancelar() {
        cerrarVentana();
    }
    
    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) cancelarBtn.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }
    
    /**
     * Limpia el mensaje de error
     */
    private void limpiarError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
    
    /**
     * Establece el préstamo a editar (null para nuevo préstamo)
     */
    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
        
        if (prestamo != null) {
            tituloLabel.setText("Editar Préstamo");
            // Cargar datos del préstamo para edición
            // Por ahora solo se permite crear préstamos nuevos
        } else {
            tituloLabel.setText("Nuevo Préstamo");
        }
    }
    
    /**
     * Establece el controlador padre para actualizar la tabla
     */
    public void setParentController(PrestamoManagementController parentController) {
        this.parentController = parentController;
    }
} 