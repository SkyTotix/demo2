package com.example.demo2.controller;

import com.example.demo2.models.Usuario;
import com.example.demo2.models.enums.TipoUsuario;
import com.example.demo2.service.UsuarioService;
import com.example.demo2.service.AuthService;
import com.example.demo2.service.NotificationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Timestamp;

/**
 * Controlador para el formulario de bibliotecarios
 */
public class BibliotecarioFormController {
    
    // Elementos básicos del formulario
    @FXML private Label lblTitulo;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbTurno;
    @FXML private CheckBox chkGestionUsuarios;
    @FXML private CheckBox chkConfiguracionSistema;
    @FXML private TextArea txtNotas;
    @FXML private Label lblEstadoGuardado;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnLimpiar;
    @FXML private VBox vboxPassword;
    
    private UsuarioService usuarioService;
    private AuthService authService;
    private NotificationService notificationService;
    private Usuario usuarioActual;
    
    @FXML
    public void initialize() {
        usuarioService = UsuarioService.getInstance();
        authService = AuthService.getInstance();
        notificationService = NotificationService.getInstance();
        
        // Configurar estado
        cmbEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cmbEstado.setValue("Activo");
        
        // Configurar turno (si existe)
        if (cmbTurno != null) {
            cmbTurno.setItems(FXCollections.observableArrayList("Matutino", "Vespertino", "Nocturno", "Mixto"));
            cmbTurno.setValue("Matutino");
        }
        
        // Configurar checkboxes por defecto
        if (chkGestionUsuarios != null) {
            chkGestionUsuarios.setSelected(false);
        }
        if (chkConfiguracionSistema != null) {
            chkConfiguracionSistema.setSelected(false);
        }
        
        System.out.println("📝 Controlador de formulario de bibliotecario inicializado");
    }
    
    /**
     * Configura el formulario para un usuario existente (edición)
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            lblTitulo.setText("Editar Bibliotecario");
            cargarDatosUsuario(usuario);
            
            // En modo edición, ocultar campos de contraseña
            if (vboxPassword != null) {
                vboxPassword.setVisible(false);
                vboxPassword.setManaged(false);
            }
        } else {
            lblTitulo.setText("Nuevo Bibliotecario");
        }
    }
    
    /**
     * Carga los datos del usuario en el formulario
     */
    private void cargarDatosUsuario(Usuario usuario) {
        txtUsername.setText(usuario.getUsername());
        txtUsername.setDisable(true); // No permitir cambiar el nombre de usuario
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtEmail.setText(usuario.getEmail());
        txtTelefono.setText(usuario.getTelefono() != null ? usuario.getTelefono() : "");
        
        // Cargar campos opcionales
        if (txtDireccion != null) {
            txtDireccion.setText(usuario.getDireccion() != null ? usuario.getDireccion() : "");
        }
        if (dpFechaNacimiento != null) {
            dpFechaNacimiento.setValue(usuario.getFechaNacimiento());
        }
        
        cmbEstado.setValue(usuario.isActivo() ? "Activo" : "Inactivo");
    }
    
    /**
     * Maneja el evento de guardar
     */
    @FXML
    private void handleGuardar() {
        System.out.println("🔄 Iniciando proceso de guardado de bibliotecario...");
        
        if (validarFormulario()) {
            System.out.println("✅ Validación del formulario exitosa");
            
            try {
                Usuario usuario = usuarioActual != null ? usuarioActual : new Usuario();
                System.out.println("📋 Usuario objeto creado: " + (usuarioActual != null ? "editando existente" : "creando nuevo"));
                
                // Actualizar datos básicos
                String username = txtUsername.getText().trim();
                String nombre = txtNombre.getText().trim();
                String apellido = txtApellido.getText().trim();
                String email = txtEmail.getText().trim();
                String telefono = txtTelefono.getText().trim();
                boolean activo = "Activo".equals(cmbEstado.getValue());
                
                System.out.println("📝 Datos del formulario:");
                System.out.println("  - Username: " + username);
                System.out.println("  - Nombre: " + nombre);
                System.out.println("  - Apellido: " + apellido);
                System.out.println("  - Email: " + email);
                System.out.println("  - Teléfono: " + telefono);
                System.out.println("  - Activo: " + activo);
                
                usuario.setUsername(username);
                usuario.setNombre(nombre);
                usuario.setApellido(apellido);
                usuario.setEmail(email);
                usuario.setTelefono(telefono);
                usuario.setActivo(activo);
                usuario.setTipoUsuario(TipoUsuario.BIBLIOTECARIO);
                
                // Configurar campos opcionales
                if (txtDireccion != null) {
                    usuario.setDireccion(txtDireccion.getText().trim());
                }
                if (dpFechaNacimiento != null) {
                    usuario.setFechaNacimiento(dpFechaNacimiento.getValue());
                }
                
                // Si es nuevo usuario, establecer contraseña
                if (usuarioActual == null) {
                    String password = txtPassword.getText();
                    System.out.println("🔐 Procesando contraseña para nuevo usuario...");
                    
                    if (password == null || password.isEmpty()) {
                        throw new IllegalArgumentException("La contraseña no puede estar vacía");
                    }
                    
                    String hashedPassword = authService.hashPassword(password);
                    usuario.setPasswordHash(hashedPassword);
                    usuario.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
                    System.out.println("✅ Contraseña hasheada y fecha de creación establecida");
                } 
                
                System.out.println("💾 Intentando guardar usuario en base de datos...");
                
                // Guardar usuario
                boolean exito = false;
                String errorMessage = "";
                try {
                    if (usuarioActual == null) {
                        System.out.println("➕ Creando nuevo usuario...");
                        usuarioService.crear(usuario);
                        System.out.println("✅ Usuario creado exitosamente en BD");
                        exito = true;
                    } else {
                        System.out.println("📝 Actualizando usuario existente...");
                        usuarioService.actualizar(usuario);
                        System.out.println("✅ Usuario actualizado exitosamente en BD");
                        exito = true;
                    }
                } catch (Exception ex) {
                    System.err.println("❌ Error en operación de BD: " + ex.getMessage());
                    ex.printStackTrace();
                    errorMessage = ex.getMessage();
                    exito = false;
                }
                
                if (exito) {
                    System.out.println("🎉 Proceso completado exitosamente");
                    mostrarExito("Usuario guardado correctamente");
                    
                    // Cerrar ventana después de un breve delay
                    javafx.animation.PauseTransition pause = 
                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                    pause.setOnFinished(e -> cerrarVentana());
                    pause.play();
                    
                } else {
                    System.err.println("❌ Fallo en el guardado");
                    mostrarError("Error al guardar el usuario: " + errorMessage);
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error inesperado en handleGuardar: " + e.getMessage());
                e.printStackTrace();
                mostrarError("Error inesperado: " + e.getMessage());
            }
        } else {
            System.err.println("❌ Validación del formulario falló");
        }
    }
    
    /**
     * Valida todos los campos del formulario
     */
    private boolean validarFormulario() {
        System.out.println("🔍 Iniciando validación del formulario...");
        
        // Validar nombre de usuario
        String nombreUsuario = txtUsername.getText();
        System.out.println("🔍 Validando username: '" + nombreUsuario + "'");
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            System.err.println("❌ Username vacío o null");
            mostrarError("El nombre de usuario es obligatorio");
            return false;
        }
        
        // Validar contraseña (solo para nuevos usuarios)
        if (usuarioActual == null) {
            System.out.println("🔍 Validando contraseñas para nuevo usuario...");
            String password = txtPassword.getText();
            String confirmarPassword = txtConfirmPassword.getText();
            
            System.out.println("🔍 Password: " + (password != null ? "'***' (length: " + password.length() + ")" : "null"));
            System.out.println("🔍 Confirm Password: " + (confirmarPassword != null ? "'***' (length: " + confirmarPassword.length() + ")" : "null"));
            
            if (password == null || password.isEmpty()) {
                System.err.println("❌ Password vacío o null");
                mostrarError("La contraseña es obligatoria");
                return false;
            } else if (!password.equals(confirmarPassword)) {
                System.err.println("❌ Contraseñas no coinciden");
                mostrarError("Las contraseñas no coinciden");
                return false;
            }
        } else {
            System.out.println("🔍 Editando usuario existente - saltando validación de contraseña");
        }
        
        // Validar nombre
        String nombre = txtNombre.getText();
        System.out.println("🔍 Validando nombre: '" + nombre + "'");
        if (nombre == null || nombre.trim().isEmpty()) {
            System.err.println("❌ Nombre vacío o null");
            mostrarError("El nombre es obligatorio");
            return false;
        }
        
        // Validar apellido
        String apellido = txtApellido.getText();
        System.out.println("🔍 Validando apellido: '" + apellido + "'");
        if (apellido == null || apellido.trim().isEmpty()) {
            System.err.println("❌ Apellido vacío o null");
            mostrarError("El apellido es obligatorio");
            return false;
        }
        
        // Validar email
        String email = txtEmail.getText();
        System.out.println("🔍 Validando email: '" + email + "'");
        if (email == null || email.trim().isEmpty()) {
            System.err.println("❌ Email vacío o null");
            mostrarError("El email es obligatorio");
            return false;
        }
        
        System.out.println("✅ Validación completada exitosamente");
        return true;
    }
    
    /**
     * Muestra un mensaje de éxito
     */
    private void mostrarExito(String mensaje) {
        lblEstadoGuardado.setText("✓ " + mensaje);
        lblEstadoGuardado.setStyle("-fx-text-fill: #10B981;");
        lblEstadoGuardado.setVisible(true);
    }
    
    /**
     * Muestra un mensaje de error general
     */
    private void mostrarError(String mensaje) {
        lblEstadoGuardado.setText("✗ " + mensaje);
        lblEstadoGuardado.setStyle("-fx-text-fill: #EF4444;");
        lblEstadoGuardado.setVisible(true);
    }
    
    /**
     * Maneja el evento de cancelar
     */
    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }
    
    @FXML
    private void handleLimpiar() {
        // Limpiar campos de información personal
        txtUsername.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        txtTelefono.clear();
        
        if (txtDireccion != null) {
            txtDireccion.clear();
        }
        if (dpFechaNacimiento != null) {
            dpFechaNacimiento.setValue(null);
        }
        if (txtNotas != null) {
            txtNotas.clear();
        }
        
        // Limpiar campos de contraseña
        txtPassword.clear();
        txtConfirmPassword.clear();
        
        // Resetear estado por defecto
        cmbEstado.setValue("Activo");
        if (cmbTurno != null) {
            cmbTurno.setValue("Matutino");
        }
        
        // Resetear checkboxes
        if (chkGestionUsuarios != null) {
            chkGestionUsuarios.setSelected(false);
        }
        if (chkConfiguracionSistema != null) {
            chkConfiguracionSistema.setSelected(false);
        }
        
        // Ocultar mensaje de estado
        if (lblEstadoGuardado != null) {
            lblEstadoGuardado.setVisible(false);
        }
        
        // Enfocar en el primer campo
        txtUsername.requestFocus();
        
        System.out.println("🧹 Formulario de bibliotecario limpiado");
    }
    

    
    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
} 