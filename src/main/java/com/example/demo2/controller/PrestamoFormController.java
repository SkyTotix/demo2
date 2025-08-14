package com.example.demo2.controller;

import com.example.demo2.models.Prestamo;
import com.example.demo2.models.Libro;
import com.example.demo2.models.Lector;
import com.example.demo2.service.PrestamoService;
import com.example.demo2.service.LibroService;
import com.example.demo2.service.LectorService;
import com.example.demo2.service.NotificationService;
import com.example.demo2.service.AuthService;
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
            
            // Mostrar texto indicativo en el campo de código
            if (prestamo == null) {
                codigoField.setText("[Se generará automáticamente al guardar]");
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
                    // Mostrar advertencia en tiempo real, pero no bloquear
                    List<Prestamo> prestamosActivos = prestamoService.obtenerPorLector(lectorId);
                    int cantidadActivos = (int) prestamosActivos.stream()
                        .filter(p -> "ACTIVO".equals(p.getEstado()))
                        .count();
                        
                    String mensaje = String.format(
                        "⚠️ Este lector tiene %d préstamo(s) activo(s).",
                        cantidadActivos
                    );
                    mostrarAdvertencia(mensaje);
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
        System.out.println("🔄 Iniciando proceso de creación de préstamo...");
        
        if (!validarFormulario()) {
            System.out.println("❌ Validación de formulario falló");
            return;
        }
        
        System.out.println("✅ Validación de formulario exitosa");
        
        try {
            // Generar código de préstamo justo antes de guardar
            String nuevoCodigo = prestamoService.generarCodigoPrestamo();
            System.out.println("🔑 Código de préstamo generado: " + nuevoCodigo);
            
            // Verificar si el código ya existe antes de continuar
            while (prestamoService.existeCodigo(nuevoCodigo)) {
                System.out.println("⚠️ Código " + nuevoCodigo + " ya existe, generando uno nuevo...");
                nuevoCodigo = prestamoService.generarCodigoPrestamo();
                System.out.println("🔄 Nuevo código generado: " + nuevoCodigo);
            }
            
            Prestamo nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setCodigoPrestamo(nuevoCodigo);
            nuevoPrestamo.setLibroId(libroCombo.getValue().getId());  // Libro.getId() ya retorna Long
            nuevoPrestamo.setLectorId((long) lectorCombo.getValue().getId());  // Lector.getId() retorna int, necesita cast
            
            // Usar el usuario autenticado actual
            AuthService authService = AuthService.getInstance();
            if (authService.getUsuarioActual() != null) {
                nuevoPrestamo.setBibliotecarioPrestamoId(authService.getUsuarioActual().getId());
                System.out.println("📋 Bibliotecario asignado: " + authService.getUsuarioActual().getNombreCompleto() + " (ID: " + authService.getUsuarioActual().getId() + ")");
            } else {
                System.out.println("❌ No hay usuario autenticado");
                mostrarError("Error: No hay usuario autenticado");
                return;
            }
            
            nuevoPrestamo.setFechaDevolucionEsperada(fechaDevolucionPicker.getValue());
            nuevoPrestamo.setEstado("ACTIVO");
            nuevoPrestamo.setCondicionPrestamo(condicionCombo.getValue());
            nuevoPrestamo.setObservacionesPrestamo(observacionesArea.getText());
            
            System.out.println("📝 Datos del préstamo:");
            System.out.println("  - Código: " + nuevoPrestamo.getCodigoPrestamo());
            System.out.println("  - Libro ID: " + nuevoPrestamo.getLibroId());
            System.out.println("  - Lector ID: " + nuevoPrestamo.getLectorId());
            System.out.println("  - Bibliotecario ID: " + nuevoPrestamo.getBibliotecarioPrestamoId());
            System.out.println("  - Fecha devolución: " + nuevoPrestamo.getFechaDevolucionEsperada());
            System.out.println("  - Estado: " + nuevoPrestamo.getEstado());
            
            System.out.println("💾 Intentando guardar préstamo en base de datos...");
            
            // Implementar mecanismo de reintentos en caso de colisión de códigos
            boolean guardadoExitoso = false;
            int intentos = 0;
            int maxIntentos = 3;
            Prestamo prestamoCreado = null;
            
            while (!guardadoExitoso && intentos < maxIntentos) {
                try {
                    prestamoCreado = prestamoService.crear(nuevoPrestamo);
                    guardadoExitoso = true;
                } catch (SQLException e) {
                    if (e.getMessage().contains("ORA-00001") && e.getMessage().contains("unique constraint")) {
                        // Restricción única violada, generar nuevo código
                        intentos++;
                        System.out.println("⚠️ Colisión de código detectada, reintento " + intentos + "/" + maxIntentos);
                        
                        // Generar un nuevo código y verificar que no exista
                        do {
                            nuevoCodigo = prestamoService.generarCodigoPrestamo();
                            System.out.println("🔄 Nuevo código generado: " + nuevoCodigo);
                        } while (prestamoService.existeCodigo(nuevoCodigo));
                        
                        nuevoPrestamo.setCodigoPrestamo(nuevoCodigo);
                        System.out.println("✅ Usando código verificado: " + nuevoCodigo);
                    } else {
                        // Otro error SQL, relanzar
                        throw e;
                    }
                }
            }
            
            if (!guardadoExitoso) {
                System.out.println("❌ No se pudo crear el préstamo después de " + maxIntentos + " intentos");
                mostrarError("No se pudo crear el préstamo después de varios intentos. Por favor, inténtelo de nuevo.");
                return;
            }
            
            if (prestamoCreado != null && prestamoCreado.getId() != null) {
                System.out.println("✅ Préstamo creado exitosamente con ID: " + prestamoCreado.getId());
                
                notificationService.notifySuccess("Préstamo creado", 
                    "El préstamo " + prestamoCreado.getCodigoPrestamo() + " ha sido creado exitosamente.");
                
                // Actualizar la tabla padre y cerrar ventana
                if (parentController != null) {
                    parentController.refrescarTabla();
                    System.out.println("🔄 Tabla de préstamos actualizada");
                }
                cerrarVentana();
            } else {
                System.out.println("❌ El servicio retornó null o sin ID");
                mostrarError("No se pudo crear el préstamo - el servicio no retornó un préstamo válido");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error SQL al guardar préstamo: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al guardar préstamo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al guardar préstamo: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Valida el formulario antes de guardar
     */
    private boolean validarFormulario() {
        System.out.println("🔍 Iniciando validación del formulario...");
        
        // Ya no validamos el código porque se genera automáticamente al guardar
        
        System.out.println("  📚 Libro seleccionado: " + (libroCombo.getValue() != null ? libroCombo.getValue().getTitulo() : "null"));
        if (libroCombo.getValue() == null) {
            System.out.println("  ❌ No hay libro seleccionado");
            mostrarError("Debe seleccionar un libro");
            return false;
        }
        
        System.out.println("  👤 Lector seleccionado: " + (lectorCombo.getValue() != null ? lectorCombo.getValue().getNombreCompleto() : "null"));
        if (lectorCombo.getValue() == null) {
            System.out.println("  ❌ No hay lector seleccionado");
            mostrarError("Debe seleccionar un lector");
            return false;
        }
        
        System.out.println("  📅 Fecha devolución: " + fechaDevolucionPicker.getValue());
        if (fechaDevolucionPicker.getValue() == null) {
            System.out.println("  ❌ No hay fecha de devolución");
            mostrarError("Debe seleccionar una fecha de devolución");
            return false;
        }
        
        System.out.println("  📋 Condición: " + condicionCombo.getValue());
        if (condicionCombo.getValue() == null) {
            System.out.println("  ❌ No hay condición seleccionada");
            mostrarError("Debe seleccionar la condición del libro");
            return false;
        }
        
        // Validar fecha de devolución
        System.out.println("  🗓️ Validando fecha: " + fechaDevolucionPicker.getValue() + " vs hoy: " + LocalDate.now());
        if (fechaDevolucionPicker.getValue().isBefore(LocalDate.now())) {
            System.out.println("  ❌ Fecha anterior a hoy");
            mostrarError("La fecha de devolución no puede ser anterior a hoy");
            return false;
        }
        
        // Validar disponibilidad del libro
        try {
            Long libroId = libroCombo.getValue().getId();
            System.out.println("  📖 Validando disponibilidad del libro ID: " + libroId);
            if (!prestamoService.libroDisponible(libroId)) {
                System.out.println("  ❌ Libro no disponible");
                mostrarError("El libro seleccionado no está disponible para préstamo");
                return false;
            }
            System.out.println("  ✅ Libro disponible");
        } catch (SQLException e) {
            System.out.println("  ❌ Error SQL validando libro: " + e.getMessage());
            mostrarError("Error al validar disponibilidad del libro");
            return false;
        }
        
        // Validar estado del lector - CONFIGURACIÓN TEMPORAL RELAJADA
        try {
            Long lectorId = (long) lectorCombo.getValue().getId();
            System.out.println("  👥 Validando lector ID: " + lectorId);
            
            // OPCIÓN TEMPORAL: Permitir múltiples préstamos pero mostrar advertencia
            if (prestamoService.lectorTienePrestamosActivos(lectorId)) {
                System.out.println("  ⚠️ Lector tiene préstamos activos - mostrando advertencia");
                
                // Obtener información de los préstamos activos
                List<Prestamo> prestamosActivos = prestamoService.obtenerPorLector(lectorId);
                int cantidadActivos = (int) prestamosActivos.stream()
                    .filter(p -> "ACTIVO".equals(p.getEstado()))
                    .count();
                
                // Mostrar advertencia pero permitir continuar
                String mensaje = String.format(
                    "⚠️ ADVERTENCIA: El lector %s tiene %d préstamo(s) activo(s). " +
                    "Considere si debe permitirse otro préstamo antes de continuar.",
                    lectorCombo.getValue().getNombreCompleto(),
                    cantidadActivos
                );
                
                // Cambiar el estilo para mostrar como advertencia, no error
                errorLabel.setText(mensaje);
                errorLabel.setStyle("-fx-text-fill: #F59E0B; -fx-background-color: #FEF3C7; -fx-padding: 8px; -fx-background-radius: 4px;");
                errorLabel.setVisible(true);
                
                System.out.println("  ⚠️ Advertencia mostrada - préstamo permitido");
                // NO retornar false - permitir que continúe
            } else {
                System.out.println("  ✅ Lector sin préstamos activos");
            }
        } catch (SQLException e) {
            System.out.println("  ❌ Error SQL validando lector: " + e.getMessage());
            mostrarError("Error al validar el lector");
            return false;
        }
        
        System.out.println("✅ Validación completada exitosamente");
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
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-background-color: #FEE2E2; -fx-padding: 8px; -fx-background-radius: 4px;");
        errorLabel.setVisible(true);
    }
    
    /**
     * Muestra un mensaje de advertencia
     */
    private void mostrarAdvertencia(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setStyle("-fx-text-fill: #F59E0B; -fx-background-color: #FEF3C7; -fx-padding: 8px; -fx-background-radius: 4px;");
        errorLabel.setVisible(true);
    }
    
    /**
     * Limpia el mensaje de error/advertencia
     */
    private void limpiarError() {
        errorLabel.setText("");
        errorLabel.setStyle("");
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