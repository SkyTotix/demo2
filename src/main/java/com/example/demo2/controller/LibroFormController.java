package com.example.demo2.controller;

import com.example.demo2.models.Libro;
import com.example.demo2.service.LibroService;
import com.example.demo2.service.NotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.Year;
import java.util.regex.Pattern;

/**
 * Controlador para el formulario de libros (crear/editar)
 */
public class LibroFormController {
    
    @FXML private Label lblTitulo;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtEditorial;
    @FXML private TextField txtAnioPublicacion;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtCantidadTotal;
    @FXML private TextField txtCantidadDisponible;
    @FXML private TextArea txtDescripcion;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblEstadoGuardado;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Label lblHelp;
    @FXML private Label lblErrorCodigo;
    @FXML private Label lblErrorTitulo;
    @FXML private Label lblErrorAutor;
    @FXML private Label lblErrorEditorial;
    @FXML private Label lblErrorAnio;
    @FXML private Label lblErrorPaginas;
    @FXML private Label lblErrorGenero;
    @FXML private Label lblErrorUbicacion;
    
    private LibroService libroService;
    private NotificationService notificationService;
    private Libro libroActual;
    private LibroManagementController parentController;
    
    @FXML
    public void initialize() {
        libroService = LibroService.getInstance();
        notificationService = NotificationService.getInstance();
        
        // Configurar géneros
        cmbCategoria.getItems().addAll(
            "Ficción", "No Ficción", "Ciencia", "Historia", 
            "Biografía", "Tecnología", "Arte", "Literatura",
            "Filosofía", "Psicología", "Educación", "Infantil"
        );
        
        // Configurar valor inicial para cantidad
        txtCantidadTotal.setText("1");
        txtCantidadDisponible.setText("1");
        
        System.out.println("📝 Controlador de formulario de libro inicializado");
    }
    
    public void setLibro(Libro libro) {
        this.libroActual = libro;
        if (libro != null) {
            lblTitulo.setText("Editar Libro");
            txtIsbn.setText(libro.getIsbn());  // ISBN es el código
            txtIsbn.setDisable(true);
            txtTitulo.setText(libro.getTitulo());
            txtAutor.setText(libro.getAutor());
            txtEditorial.setText(libro.getEditorial());
            txtAnioPublicacion.setText(String.valueOf(libro.getAnioPublicacion()));
            cmbCategoria.setValue(libro.getCategoria());  // Categoría es el género
            txtCantidadTotal.setText(String.valueOf(libro.getCantidadTotal()));
            txtCantidadDisponible.setText(String.valueOf(libro.getCantidadDisponible()));
            txtDescripcion.setText(libro.getDescripcion());
            chkActivo.setSelected(libro.isActivo());
        } else {
            lblTitulo.setText("Nuevo Libro");
        }
    }
    
    public void setParentController(LibroManagementController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void guardar() {
        if (validarFormulario()) {
            try {
                Libro libro = libroActual != null ? libroActual : new Libro();
                
                // Actualizar datos del libro con métodos reales
                if (libroActual == null) {
                    libro.setIsbn(txtIsbn.getText().trim().toUpperCase());
                }
                libro.setTitulo(txtTitulo.getText().trim());
                libro.setAutor(txtAutor.getText().trim());
                libro.setEditorial(txtEditorial.getText().trim());
                libro.setAnioPublicacion(Integer.parseInt(txtAnioPublicacion.getText().trim()));
                libro.setCategoria(cmbCategoria.getValue());
                libro.setCantidadTotal(Integer.parseInt(txtCantidadTotal.getText().trim()));
                libro.setCantidadDisponible(Integer.parseInt(txtCantidadDisponible.getText().trim()));
                libro.setActivo(chkActivo.isSelected());
                libro.setDescripcion(txtDescripcion.getText().trim());
                
                // Guardar usando el método real del servicio
                boolean exito = libroService.crearLibro(libro);
                
                if (exito) {
                    lblEstadoGuardado.setText("✓ Guardado exitosamente");
                    lblEstadoGuardado.setStyle("-fx-text-fill: #10B981;");
                    lblEstadoGuardado.setVisible(true);
                    
                    // Cerrar ventana después de un breve delay
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                    pause.setOnFinished(e -> cerrarVentana());
                    pause.play();
                    
                } else {
                    mostrarErrorGeneral("Error al guardar el libro");
                }
                
            } catch (Exception e) {
                mostrarErrorGeneral("Error inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private boolean validarFormulario() {
        // Validación básica - solo campos obligatorios que existen
        if (txtIsbn.getText() == null || txtIsbn.getText().trim().isEmpty()) {
            mostrarErrorGeneral("El código ISBN es obligatorio");
            return false;
        }
        
        if (txtTitulo.getText() == null || txtTitulo.getText().trim().isEmpty()) {
            mostrarErrorGeneral("El título es obligatorio");
            return false;
        }
        
        if (txtAutor.getText() == null || txtAutor.getText().trim().isEmpty()) {
            mostrarErrorGeneral("El autor es obligatorio");
            return false;
        }
        
        return true;
    }
    
    private void mostrarErrorGeneral(String mensaje) {
        lblEstadoGuardado.setText("✗ " + mensaje);
        lblEstadoGuardado.setStyle("-fx-text-fill: #EF4444;");
        lblEstadoGuardado.setVisible(true);
    }
    
    @FXML
    private void cancelar() {
        cerrarVentana();
    }
    
    @FXML
    private void validarIsbn() {
        String isbn = txtIsbn.getText();
        if (isbn == null || isbn.trim().isEmpty()) {
            mostrarErrorGeneral("Ingrese un ISBN para validar");
            return;
        }
        
        // Validación básica de formato ISBN
        isbn = isbn.trim().replaceAll("-", "").replaceAll(" ", "");
        
        if (isbn.length() == 10 || isbn.length() == 13) {
            // Validación básica pasada
            lblEstadoGuardado.setText("✓ ISBN válido");
            lblEstadoGuardado.setStyle("-fx-text-fill: #10B981;");
            lblEstadoGuardado.setVisible(true);
            
            // Ocultar mensaje después de 3 segundos
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            pause.setOnFinished(e -> lblEstadoGuardado.setVisible(false));
            pause.play();
        } else {
            mostrarErrorGeneral("El ISBN debe tener 10 o 13 dígitos");
        }
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
} 