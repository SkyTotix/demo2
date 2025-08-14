package com.example.demo2.controller;

import com.example.demo2.models.Usuario;
import com.example.demo2.models.enums.TipoUsuario;
import com.example.demo2.service.UsuarioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Controlador para el formulario de administradores
 */
public class AdminFormController {
    
    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblMensaje;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    
    // Secciones de contraseña (para ocultar en modo edición)
    @FXML private VBox passwordSection;
    @FXML private VBox confirmPasswordSection;
    
    private UsuarioService usuarioService;
    private AdminManagementController parentController;
    private Usuario administradorParaEditar;
    private boolean esEdicion = false;
    
    @FXML
    public void initialize() {
        usuarioService = UsuarioService.getInstance();
        configurarValidaciones();
    }
    
    private void configurarValidaciones() {
        // Validación en tiempo real del email
        txtEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtEmail.getText().isEmpty()) {
                if (!esEmailValido(txtEmail.getText())) {
                    mostrarMensaje("Email no válido", "error");
                } else {
                    limpiarMensaje();
                }
            }
        });
        
        // Validación de username único
        txtUsername.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtUsername.getText().isEmpty()) {
                verificarUsernameUnico();
            }
        });
        
        // Validación de confirmación de contraseña
        txtConfirmPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !txtPassword.getText().equals(newVal)) {
                mostrarMensaje("Las contraseñas no coinciden", "error");
            } else {
                limpiarMensaje();
            }
        });
    }
    
    public void setAdminManagementController(AdminManagementController controller) {
        this.parentController = controller;
    }
    
    public void setAdministradorParaEditar(Usuario admin) {
        this.administradorParaEditar = admin;
        this.esEdicion = true;
        
        // Cambiar título
        lblTitulo.setText("Editar Administrador");
        btnGuardar.setText("Actualizar Administrador");
        
        // Llenar campos
        txtNombre.setText(admin.getNombre());
        txtApellido.setText(admin.getApellido());
        txtEmail.setText(admin.getEmail());
        txtUsername.setText(admin.getUsername());
        chkActivo.setSelected(admin.isActivo());
        
        // Ocultar campos de contraseña en modo edición
        passwordSection.setVisible(false);
        passwordSection.setManaged(false);
        confirmPasswordSection.setVisible(false);
        confirmPasswordSection.setManaged(false);
        
        // Deshabilitar username en edición
        txtUsername.setDisable(true);
    }
    
    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) {
            return;
        }
        
        try {
            if (esEdicion) {
                actualizarAdministrador();
            } else {
                crearAdministrador();
            }
            
            // Actualizar lista en el controlador padre después de un pequeño delay
            // para asegurar que la transacción se complete
            if (parentController != null) {
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(100); // Pequeño delay para asegurar commit de BD
                        parentController.actualizarListaAdministradores();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            // Cerrar ventana
            cerrarVentana();
            
        } catch (Exception e) {
            System.err.println("❌ Error guardando administrador: " + e.getMessage());
            mostrarMensaje("Error al guardar: " + e.getMessage(), "error");
        }
    }
    
    private void crearAdministrador() throws Exception {
        Usuario nuevoAdmin = new Usuario();
        nuevoAdmin.setNombre(txtNombre.getText().trim());
        nuevoAdmin.setApellido(txtApellido.getText().trim());
        nuevoAdmin.setEmail(txtEmail.getText().trim().toLowerCase());
        nuevoAdmin.setUsername(txtUsername.getText().trim().toLowerCase());
        nuevoAdmin.setTipoUsuario(TipoUsuario.ADMIN);
        nuevoAdmin.setActivo(chkActivo.isSelected());
        
        // Encriptar contraseña
        String passwordHash = BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt());
        nuevoAdmin.setPasswordHash(passwordHash);
        
        usuarioService.crear(nuevoAdmin);
        
        mostrarMensaje("Administrador creado exitosamente", "success");
        System.out.println("✅ Administrador creado: " + nuevoAdmin.getUsername());
    }
    
    private void actualizarAdministrador() throws Exception {
        administradorParaEditar.setNombre(txtNombre.getText().trim());
        administradorParaEditar.setApellido(txtApellido.getText().trim());
        administradorParaEditar.setEmail(txtEmail.getText().trim().toLowerCase());
        administradorParaEditar.setActivo(chkActivo.isSelected());
        
        usuarioService.actualizar(administradorParaEditar);
        
        mostrarMensaje("Administrador actualizado exitosamente", "success");
        System.out.println("✅ Administrador actualizado: " + administradorParaEditar.getUsername());
    }
    
    private boolean validarFormulario() {
        // Validar campos obligatorios
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarMensaje("El nombre es obligatorio", "error");
            txtNombre.requestFocus();
            return false;
        }
        
        if (txtApellido.getText().trim().isEmpty()) {
            mostrarMensaje("El apellido es obligatorio", "error");
            txtApellido.requestFocus();
            return false;
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            mostrarMensaje("El email es obligatorio", "error");
            txtEmail.requestFocus();
            return false;
        }
        
        if (!esEmailValido(txtEmail.getText())) {
            mostrarMensaje("El email no es válido", "error");
            txtEmail.requestFocus();
            return false;
        }
        
        if (txtUsername.getText().trim().isEmpty()) {
            mostrarMensaje("El username es obligatorio", "error");
            txtUsername.requestFocus();
            return false;
        }
        
        // Validaciones para nuevo usuario
        if (!esEdicion) {
            if (txtPassword.getText().isEmpty()) {
                mostrarMensaje("La contraseña es obligatoria", "error");
                txtPassword.requestFocus();
                return false;
            }
            
            if (txtPassword.getText().length() < 6) {
                mostrarMensaje("La contraseña debe tener al menos 6 caracteres", "error");
                txtPassword.requestFocus();
                return false;
            }
            
            if (!txtPassword.getText().equals(txtConfirmPassword.getText())) {
                mostrarMensaje("Las contraseñas no coinciden", "error");
                txtConfirmPassword.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void verificarUsernameUnico() {
        try {
            String username = txtUsername.getText().trim().toLowerCase();
            
            // Si es edición y el username no cambió, no verificar
            if (esEdicion && administradorParaEditar.getUsername().equals(username)) {
                return;
            }
            
            Usuario existente = usuarioService.buscarPorUsername(username);
            if (existente != null) {
                mostrarMensaje("El username ya está en uso", "error");
            } else {
                limpiarMensaje();
            }
        } catch (Exception e) {
            System.err.println("Error verificando username: " + e.getMessage());
        }
    }
    
    private boolean esEmailValido(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }
    
    @FXML
    private void handleVolver() {
        cerrarVentana();
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarMensaje(String mensaje, String tipo) {
        lblMensaje.setText(mensaje);
        lblMensaje.getStyleClass().clear();
        
        switch (tipo.toLowerCase()) {
            case "success":
                lblMensaje.getStyleClass().add("success-message");
                break;
            case "error":
                lblMensaje.getStyleClass().add("error-message");
                break;
            default:
                lblMensaje.getStyleClass().add("info-message");
                break;
        }
        
        lblMensaje.setVisible(true);
    }
    
    private void limpiarMensaje() {
        lblMensaje.setVisible(false);
    }
}