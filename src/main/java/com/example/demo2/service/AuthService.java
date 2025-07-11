package com.example.demo2.service;

// Modelo de datos del usuario del sistema
import com.example.demo2.models.Usuario;
import com.example.demo2.models.enums.TipoUsuario;

// Librería BCrypt para hash seguro de contraseñas
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Timestamp;

/**
 * SERVICIO DE AUTENTICACIÓN Y GESTIÓN DE SESIONES - CORE DE SEGURIDAD
 * ===================================================================
 * 
 * Este servicio es el núcleo de seguridad del sistema BiblioSystem.
 * Gestiona la autenticación de usuarios, mantenimiento de sesiones activas,
 * encriptación de contraseñas y control de permisos por rol.
 * 
 * PATRÓN DE DISEÑO: SINGLETON
 * - Solo existe una instancia en todo el sistema
 * - Garantiza consistencia en la gestión de sesiones
 * - Punto central para todas las operaciones de autenticación
 * 
 * RESPONSABILIDADES PRINCIPALES:
 * 1. Validar credenciales de usuarios contra la base de datos
 * 2. Mantener información del usuario actualmente autenticado
 * 3. Encriptar contraseñas usando BCrypt (salt + hash)
 * 4. Gestionar sesiones activas y logout
 * 5. Verificar permisos por rol de usuario
 * 6. Actualizar timestamp de último acceso
 * 7. Crear usuario SuperAdmin inicial del sistema
 * 
 * SEGURIDAD IMPLEMENTADA:
 * - Contraseñas hasheadas con BCrypt (no se almacenan en texto plano)
 * - Verificación de usuario activo antes de autenticar
 * - Control de permisos basado en roles jerárquicos
 * - Registro de último acceso para auditoría
 * - Validación de existencia de usuario antes de login
 * 
 * FLUJO DE AUTENTICACIÓN:
 * 1. Usuario ingresa credenciales -> 2. Búsqueda en base de datos
 * 3. Verificación de hash de contraseña -> 4. Validación de estado activo
 * 5. Actualización de último acceso -> 6. Establecimiento de sesión
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Utilizado por LoginController para validar credenciales
 * - Usado por MainController para verificar permisos de menús
 * - Integrado con UsuarioService para operaciones de base de datos
 * - Referenciado por todos los controladores para control de acceso
 * - Esencial para sistema de notificaciones personalizado por rol
 */
public class AuthService {
    
    // === PATRÓN SINGLETON ===
    private static AuthService instance;        // Única instancia del servicio
    private Usuario usuarioActual;              // Usuario actualmente autenticado en el sistema
    
    private AuthService() {
        // Constructor privado para singleton
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Hashea una contraseña usando BCrypt
     */
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Verifica si una contraseña coincide con su hash
     */
    public boolean verifyPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
    
    /**
     * Intenta autenticar un usuario
     * @return true si la autenticación es exitosa
     */
    public boolean login(String username, String password) {
        try {
            UsuarioService usuarioService = UsuarioService.getInstance();
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            if (usuario != null && usuario.isActivo() && 
                verifyPassword(password, usuario.getPasswordHash())) {
                
                // Actualizar último acceso
                usuario.setUltimoAcceso(new Timestamp(System.currentTimeMillis()));
                usuarioService.actualizar(usuario);
                
                this.usuarioActual = usuario;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Cierra la sesión actual
     */
    public void logout() {
        this.usuarioActual = null;
    }
    
    /**
     * Obtiene el usuario actualmente autenticado
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isLoggedIn() {
        return usuarioActual != null;
    }
    
    /**
     * Verifica si el usuario actual tiene un permiso específico
     */
    public boolean tienePermiso(String permiso) {
        if (!isLoggedIn()) {
            return false;
        }
        return usuarioActual.getTipoUsuario().tienePermiso(permiso);
    }
    
    /**
     * Verifica si el usuario actual puede crear usuarios de un tipo específico
     */
    public boolean puedeCrearUsuario(TipoUsuario tipo) {
        if (!isLoggedIn()) {
            return false;
        }
        return usuarioActual.getTipoUsuario().puedeCrear(tipo);
    }
    
    /**
     * Crea el usuario super admin inicial si no existe
     */
    public void crearSuperAdminInicial() {
        try {
            UsuarioService usuarioService = UsuarioService.getInstance();
            
            // Verificar si ya existe un super admin
            if (!usuarioService.existeSuperAdmin()) {
                Usuario superAdmin = new Usuario();
                superAdmin.setNombre("Super");
                superAdmin.setApellido("Admin");
                superAdmin.setEmail("admin@bibliosystem.com");
                superAdmin.setUsername("admin");
                superAdmin.setPasswordHash(hashPassword("admin123"));
                superAdmin.setTelefono("+52 123 456 7890");
                superAdmin.setTipoUsuario(TipoUsuario.SUPERADMIN);
                superAdmin.setActivo(true);
                
                usuarioService.crear(superAdmin);
                System.out.println("Usuario Super Admin creado exitosamente");
                System.out.println("Username: admin");
                System.out.println("Password: admin123");
            }
        } catch (Exception e) {
            System.err.println("Error al crear Super Admin inicial: " + e.getMessage());
        }
    }
} 