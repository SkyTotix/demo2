package com.example.demo2.controller;

import com.example.demo2.models.Lector;
import com.example.demo2.service.AuthService;
import com.example.demo2.service.LectorService;
import com.example.demo2.service.NotificationService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.Period;

public class LectorFormController {
    
    // Campos principales
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtDireccion;  // Corregido: es TextArea no TextField
    @FXML private TextField txtNumeroDocumento;
    @FXML private TextArea txtObservaciones;  // Agregado
    @FXML private ComboBox<String> cmbTipoDocumento;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private DatePicker dpFechaVencimiento;  // Agregado
    @FXML private Label lblTitulo;
    @FXML private Label lblEdad;  // Agregado
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    
    // Labels de error
    @FXML private Label lblErrorNombre;
    @FXML private Label lblErrorApellido;
    @FXML private Label lblErrorEmail;
    @FXML private Label lblErrorTelefono;
    @FXML private Label lblErrorTipoDocumento;
    @FXML private Label lblErrorNumeroDocumento;
    @FXML private Label lblErrorFechaNacimiento;
    @FXML private Label lblErrorDireccion;
    @FXML private Label lblErrorFechaVencimiento;
    @FXML private Label lblErrorEstado;
    
    // Servicios
    private LectorService lectorService;
    private AuthService authService;
    private NotificationService notificationService;
    private Lector lectorActual;
    private LectorManagementController parentController;
    
    @FXML
    public void initialize() {
        lectorService = LectorService.getInstance();
        authService = AuthService.getInstance();
        notificationService = NotificationService.getInstance();
        
        configurarCampos();
        configurarEventos();
        
        System.out.println("📝 Controlador de formulario de lector inicializado");
    }
    
    private void configurarCampos() {
        // Configurar tipos de documento
        cmbTipoDocumento.setItems(FXCollections.observableArrayList(
            "DNI", "PASAPORTE", "CARNET_EXTRANJERIA"
        ));
        
        // Configurar estados
        cmbEstado.setItems(FXCollections.observableArrayList(
            "ACTIVO", "SUSPENDIDO", "VENCIDO", "INACTIVO"
        ));
        cmbEstado.setValue("ACTIVO");
        
        // Configurar fecha de vencimiento por defecto (1 año desde hoy)
        dpFechaVencimiento.setValue(LocalDate.now().plusYears(1));
        
        // Generar código automático para nuevos lectores
        if (lectorActual == null) {
            generarCodigoAutomatico();
        }
    }
    
    private void configurarEventos() {
        // Calcular edad cuando cambia la fecha de nacimiento
        dpFechaNacimiento.setOnAction(e -> calcularEdad());
        
        // Validación en tiempo real
        txtNombre.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validarCampo(txtNombre, lblErrorNombre, "El nombre es obligatorio");
        });
        
        txtApellido.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validarCampo(txtApellido, lblErrorApellido, "El apellido es obligatorio");
        });
        
        txtEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validarEmail();
        });
        
        txtTelefono.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validarCampo(txtTelefono, lblErrorTelefono, "El teléfono es obligatorio");
        });
        
        txtNumeroDocumento.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validarCampo(txtNumeroDocumento, lblErrorNumeroDocumento, "El número de documento es obligatorio");
        });
        
        txtDireccion.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validarCampo(txtDireccion, lblErrorDireccion, "La dirección es obligatoria");
        });
        
        cmbTipoDocumento.setOnAction(e -> 
            validarComboBox(cmbTipoDocumento, lblErrorTipoDocumento, "Seleccione un tipo de documento"));
        
        cmbEstado.setOnAction(e -> 
            validarComboBox(cmbEstado, lblErrorEstado, "Seleccione un estado"));
        
        dpFechaNacimiento.setOnAction(e -> validarFechaNacimiento());
        dpFechaVencimiento.setOnAction(e -> validarFechaVencimiento());
    }
    
    private void generarCodigoAutomatico() {
        try {
            // Generar código automático usando el servicio
            String codigo = lectorService.generarCodigoLector();
            txtCodigo.setText(codigo);
        } catch (Exception e) {
            System.err.println("❌ Error generando código automático: " + e.getMessage());
            // Fallback en caso de error
            txtCodigo.setText("LEC-000001");
        }
    }
    
    private void calcularEdad() {
        if (dpFechaNacimiento.getValue() != null) {
            LocalDate fechaNacimiento = dpFechaNacimiento.getValue();
            LocalDate ahora = LocalDate.now();
            
            if (fechaNacimiento.isAfter(ahora)) {
                lblEdad.setText("Fecha inválida");
                lblEdad.setStyle("-fx-text-fill: #EF4444;");
            } else {
                Period periodo = Period.between(fechaNacimiento, ahora);
                lblEdad.setText(periodo.getYears() + " años");
                lblEdad.setStyle("-fx-text-fill: #64748B;");
            }
        } else {
            lblEdad.setText("");
        }
    }
    
    private boolean validarCampo(Control campo, Label lblError, String mensaje) {
        String valor = "";
        if (campo instanceof TextField) {
            valor = ((TextField) campo).getText();
        } else if (campo instanceof TextArea) {
            valor = ((TextArea) campo).getText();
        }
        
        if (valor == null || valor.trim().isEmpty()) {
            mostrarError(lblError, mensaje);
            return false;
        } else {
            ocultarError(lblError);
            return true;
        }
    }
    
    private boolean validarComboBox(ComboBox<?> combo, Label lblError, String mensaje) {
        if (combo.getValue() == null) {
            mostrarError(lblError, mensaje);
            return false;
        } else {
            ocultarError(lblError);
            return true;
        }
    }
    
    private boolean validarEmail() {
        String email = txtEmail.getText();
        if (email == null || email.trim().isEmpty()) {
            mostrarError(lblErrorEmail, "El email es obligatorio");
            return false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarError(lblErrorEmail, "Formato de email inválido");
            return false;
        } else {
            ocultarError(lblErrorEmail);
            return true;
        }
    }
    
    private boolean validarFechaNacimiento() {
        if (dpFechaNacimiento.getValue() == null) {
            mostrarError(lblErrorFechaNacimiento, "La fecha de nacimiento es obligatoria");
            return false;
        } else if (dpFechaNacimiento.getValue().isAfter(LocalDate.now())) {
            mostrarError(lblErrorFechaNacimiento, "La fecha de nacimiento no puede ser futura");
            return false;
        } else {
            ocultarError(lblErrorFechaNacimiento);
            return true;
        }
    }
    
    private boolean validarFechaVencimiento() {
        if (dpFechaVencimiento.getValue() == null) {
            mostrarError(lblErrorFechaVencimiento, "La fecha de vencimiento es obligatoria");
            return false;
        } else if (dpFechaVencimiento.getValue().isBefore(LocalDate.now())) {
            mostrarError(lblErrorFechaVencimiento, "La fecha de vencimiento no puede ser pasada");
            return false;
        } else {
            ocultarError(lblErrorFechaVencimiento);
            return true;
        }
    }
    
    private void mostrarError(Label lblError, String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
    
    private void ocultarError(Label lblError) {
        lblError.setVisible(false);
    }
    
    public void setParentController(LectorManagementController parentController) {
        this.parentController = parentController;
    }
    
    public void setLector(Lector lector) {
        this.lectorActual = lector;
        if (lector != null) {
            lblTitulo.setText("Editar Lector");
            txtCodigo.setText(lector.getCodigoLector());
            txtCodigo.setDisable(true);
            txtNombre.setText(lector.getNombre());
            txtApellido.setText(lector.getApellido());
            txtEmail.setText(lector.getEmail());
            txtTelefono.setText(lector.getTelefono());
            txtDireccion.setText(lector.getDireccion());
            txtNumeroDocumento.setText(lector.getNumeroDocumento());
            cmbTipoDocumento.setValue(lector.getTipoDocumento());
            cmbEstado.setValue(lector.getEstado());
            
            if (lector.getFechaNacimiento() != null) {
                dpFechaNacimiento.setValue(lector.getFechaNacimiento());
                calcularEdad();
            }
            
            if (lector.getFechaVencimiento() != null) {
                dpFechaVencimiento.setValue(lector.getFechaVencimiento());
            }
            
            if (lector.getObservaciones() != null) {
                txtObservaciones.setText(lector.getObservaciones());
            }
        } else {
            lblTitulo.setText("Nuevo Lector");
            generarCodigoAutomatico();
        }
    }
    
    @FXML
    private void guardar() {
        if (validarFormulario()) {
            try {
                Lector lector = lectorActual != null ? lectorActual : new Lector();
                
                // Actualizar datos
                if (lectorActual == null) {
                    lector.setCodigoLector(txtCodigo.getText().trim().toUpperCase());
                    // Establecer creado por el usuario actual
                    if (authService.getUsuarioActual() != null) {
                        lector.setCreadoPor(authService.getUsuarioActual().getId().intValue());
                    } else {
                        lector.setCreadoPor(1); // Fallback al admin por defecto
                    }
                } else {
                    // Para edición, establecer actualizado por el usuario actual
                    if (authService.getUsuarioActual() != null) {
                        lector.setActualizadoPor(authService.getUsuarioActual().getId().intValue());
                    }
                }
                
                lector.setNombre(txtNombre.getText().trim());
                lector.setApellido(txtApellido.getText().trim());
                lector.setEmail(txtEmail.getText().trim());
                lector.setTelefono(txtTelefono.getText().trim());
                lector.setDireccion(txtDireccion.getText().trim());
                lector.setNumeroDocumento(txtNumeroDocumento.getText().trim());
                lector.setTipoDocumento(cmbTipoDocumento.getValue());
                lector.setEstado(cmbEstado.getValue());
                lector.setFechaNacimiento(dpFechaNacimiento.getValue());
                lector.setFechaVencimiento(dpFechaVencimiento.getValue());
                lector.setObservaciones(txtObservaciones.getText().trim());
                
                // Guardar
                boolean exito = false;
                try {
                    if (lectorActual == null) {
                        lectorService.crear(lector);
                        notificationService.addNotification(
                            "Lector Creado",
                            "El lector " + lector.getNombre() + " " + lector.getApellido() + " ha sido creado exitosamente",
                            NotificationService.NotificationType.SUCCESS
                        );
                        exito = true;
                    } else {
                        lectorService.actualizar(lector);
                        notificationService.addNotification(
                            "Lector Actualizado",
                            "El lector " + lector.getNombre() + " " + lector.getApellido() + " ha sido actualizado exitosamente",
                            NotificationService.NotificationType.SUCCESS
                        );
                        exito = true;
                    }
                } catch (Exception ex) {
                    System.err.println("❌ Error al guardar lector: " + ex.getMessage());
                    exito = false;
                }
                
                if (exito) {
                    // Cerrar formulario - la actualización se maneja automáticamente
                    cerrarFormulario();
                } else {
                    mostrarAlerta("Error", "No se pudo guardar el lector", Alert.AlertType.ERROR);
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error inesperado al guardar: " + e.getMessage());
                mostrarAlerta("Error", "Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    private boolean validarFormulario() {
        boolean valido = true;
        
        // Validar todos los campos
        valido &= validarCampo(txtNombre, lblErrorNombre, "El nombre es obligatorio");
        valido &= validarCampo(txtApellido, lblErrorApellido, "El apellido es obligatorio");
        valido &= validarEmail();
        valido &= validarCampo(txtTelefono, lblErrorTelefono, "El teléfono es obligatorio");
        valido &= validarCampo(txtNumeroDocumento, lblErrorNumeroDocumento, "El número de documento es obligatorio");
        valido &= validarCampo(txtDireccion, lblErrorDireccion, "La dirección es obligatoria");
        valido &= validarComboBox(cmbTipoDocumento, lblErrorTipoDocumento, "Seleccione un tipo de documento");
        valido &= validarComboBox(cmbEstado, lblErrorEstado, "Seleccione un estado");
        valido &= validarFechaNacimiento();
        valido &= validarFechaVencimiento();
        
        // Validar código único solo para nuevos lectores
        if (lectorActual == null) {
            valido &= validarCodigoUnico();
        }
        
        // Validar documento único
        valido &= validarDocumentoUnico();
        
        // Validar email único
        valido &= validarEmailUnico();
        
        return valido;
    }
    
    private boolean validarCodigoUnico() {
        try {
            String codigo = txtCodigo.getText().trim();
            if (lectorService.existeCodigo(codigo)) {
                mostrarAlerta("Código Duplicado", "Ya existe un lector con este código. Se generará uno nuevo.", Alert.AlertType.WARNING);
                generarCodigoAutomatico();
                return true; // Continuar con el nuevo código
            }
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error validando código único: " + e.getMessage());
            return true; // Continuar en caso de error
        }
    }
    
    private boolean validarDocumentoUnico() {
        try {
            String documento = txtNumeroDocumento.getText().trim();
            Integer excludeId = lectorActual != null ? lectorActual.getId() : null;
            
            if (lectorService.existeDocumento(documento, excludeId)) {
                mostrarError(lblErrorNumeroDocumento, "Ya existe un lector con este número de documento");
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error validando documento único: " + e.getMessage());
            return true; // Continuar en caso de error
        }
    }
    
    private boolean validarEmailUnico() {
        try {
            String email = txtEmail.getText().trim();
            Integer excludeId = lectorActual != null ? lectorActual.getId() : null;
            
            if (lectorService.existeEmail(email, excludeId)) {
                mostrarError(lblErrorEmail, "Ya existe un lector con este email");
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error validando email único: " + e.getMessage());
            return true; // Continuar en caso de error
        }
    }
    
    @FXML
    private void cancelar() {
        cerrarFormulario();
    }
    
    private void cerrarFormulario() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 