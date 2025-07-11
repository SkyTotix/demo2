package com.example.demo2.models;

// Importación del enum que define los tipos de usuario del sistema
import com.example.demo2.models.enums.TipoUsuario;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * MODELO DE DATOS: USUARIO DEL SISTEMA BIBLIOSYSTEM
 * =================================================
 * 
 * Esta clase representa una entidad Usuario en el sistema de gestión bibliotecaria.
 * Es un POJO (Plain Old Java Object) que mapea directamente con la tabla 'usuarios' 
 * en la base de datos Oracle Cloud.
 * 
 * PROPÓSITO:
 * - Encapsular toda la información de un usuario del sistema
 * - Servir como intermediario entre la base de datos y la lógica de negocio
 * - Proporcionar validación y métodos de utilidad para datos de usuario
 * 
 * TIPOS DE USUARIO SOPORTADOS:
 * - SUPERADMIN: Control total del sistema
 * - ADMIN: Gestión operativa de la biblioteca
 * - BIBLIOTECARIO: Operaciones diarias (préstamos, devoluciones)
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Utilizado por UsuarioService para operaciones CRUD
 * - Usado en controladores (LoginController, AdminFormController, etc.)
 * - Vinculado con AuthService para gestión de sesiones
 * - Relacionado con modelo Prestamo (un usuario puede tener múltiples préstamos)
 */
public class Usuario {
    
    // === CAMPOS DE IDENTIFICACIÓN ===
    private Long id;                    // ID único en base de datos (auto-generado)
    private String nombre;              // Nombre real del usuario
    private String apellido;            // Apellido del usuario
    private String email;               // Email único para comunicaciones
    private String username;            // Nombre de usuario único para login
    private String passwordHash;        // Contraseña encriptada (nunca en texto plano)
    
    // === INFORMACIÓN PERSONAL OPCIONAL ===
    private String telefono;            // Teléfono de contacto
    private LocalDate fechaNacimiento;  // Fecha de nacimiento
    private String direccion;           // Dirección física
    
    // === CAMPOS DE SISTEMA ===
    private TipoUsuario tipoUsuario;    // Rol/tipo: SUPERADMIN, ADMIN, BIBLIOTECARIO
    private boolean activo;             // Estado: true=activo, false=deshabilitado
    private Timestamp fechaCreacion;    // Cuándo se creó la cuenta
    private Timestamp ultimoAcceso;     // Última vez que hizo login
    
    // Constructor vacío
    public Usuario() {
        this.activo = true;
    }
    
    // Constructor con parámetros básicos
    public Usuario(String nombre, String apellido, String email, String username, TipoUsuario tipoUsuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.username = username;
        this.tipoUsuario = tipoUsuario;
        this.activo = true;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
    
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Timestamp getUltimoAcceso() {
        return ultimoAcceso;
    }
    
    public void setUltimoAcceso(Timestamp ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    // Método de conveniencia para obtener fechas como LocalDate
    public LocalDate getFechaCreacionAsLocalDate() {
        return fechaCreacion != null ? fechaCreacion.toLocalDateTime().toLocalDate() : null;
    }
    
    public LocalDate getUltimaConexion() {
        return ultimoAcceso != null ? ultimoAcceso.toLocalDateTime().toLocalDate() : null;
    }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", tipoUsuario=" + tipoUsuario +
                ", activo=" + activo +
                '}';
    }
} 