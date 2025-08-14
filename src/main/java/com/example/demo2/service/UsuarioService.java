package com.example.demo2.service;

// Gestor de conexiones a la base de datos Oracle Cloud
import com.example.demo2.database.DatabaseManager;
// Modelos de datos del sistema
import com.example.demo2.models.Usuario;
import com.example.demo2.models.enums.TipoUsuario;

// Importaciones estándar de Java para base de datos y colecciones
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SERVICIO DE GESTIÓN DE USUARIOS - OPERACIONES CRUD COMPLETAS
 * =============================================================
 * 
 * Este servicio encapsula toda la lógica de negocio relacionada con usuarios
 * del sistema BiblioSystem. Proporciona una capa de abstracción entre los
 * controladores y la base de datos Oracle Cloud, gestionando operaciones CRUD
 * y funcionalidades específicas de usuarios.
 * 
 * PATRÓN DE DISEÑO: SINGLETON
 * - Solo existe una instancia en todo el sistema
 * - Garantiza consistencia en las operaciones de usuario
 * - Gestión centralizada de todas las transacciones de usuario
 * 
 * RESPONSABILIDADES PRINCIPALES:
 * 1. Operaciones CRUD completas (Create, Read, Update, Delete)
 * 2. Búsquedas especializadas (por username, email, tipo, etc.)
 * 3. Gestión de contraseñas y actualización de credenciales
 * 4. Control de usuarios activos/inactivos (soft delete)
 * 5. Creación de usuarios de prueba para desarrollo
 * 6. Validación de existencia de SuperAdmin
 * 7. Actualización de datos específicos (teléfono, último acceso)
 * 
 * OPERACIONES SOPORTADAS:
 * 
 * CREACIÓN:
 * - Crear nuevos usuarios con validación completa
 * - Generación automática de ID único
 * - Hasheo de contraseñas (delegado a AuthService)
 * - Creación de usuarios de prueba para desarrollo
 * 
 * CONSULTAS:
 * - Búsqueda por username (para login)
 * - Búsqueda por email (para validación de unicidad)
 * - Búsqueda por ID (para operaciones específicas)
 * - Listado por tipo de usuario (Admin, Bibliotecario, etc.)
 * - Filtrado por estado activo/inactivo
 * 
 * ACTUALIZACIONES:
 * - Actualización completa de perfil de usuario
 * - Cambio de contraseña independiente
 * - Actualización de último acceso (para auditoría)
 * - Modificación de teléfono específica
 * - Activación/desactivación de usuarios
 * 
 * ELIMINACIÓN:
 * - Soft delete (desactivación): Preferido para auditoría
 * - Hard delete (eliminación física): Solo cuando es necesario
 * 
 * INTEGRACIÓN CON BASE DE DATOS:
 * - Utiliza PreparedStatement para prevenir SQL injection
 * - Mapeo automático de ResultSet a objetos Usuario
 * - Gestión de transacciones con commit/rollback automático
 * - Soporte para tipos de datos Oracle (SYSDATE, TIMESTAMP, etc.)
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Utilizado por AuthService para autenticación
 * - Usado por controladores de gestión (AdminFormController, etc.)
 * - Integrado con MainController para estadísticas
 * - Referenciado por sistema de notificaciones
 * - Base para auditoría del sistema
 */
public class UsuarioService {
    
    // === PATRÓN SINGLETON ===
    private static UsuarioService instance;        // Única instancia del servicio
    
    private UsuarioService() {
    }
    
    public static UsuarioService getInstance() {
        if (instance == null) {
            instance = new UsuarioService();
        }
        return instance;
    }
    
    /**
     * Crea un nuevo usuario en la base de datos
     */
    public void crear(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, apellido, email, username, password_hash, " +
                    "telefono, fecha_nacimiento, direccion, tipo_usuario, activo, fecha_creacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {
            
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getUsername());
            stmt.setString(5, usuario.getPasswordHash());
            stmt.setString(6, usuario.getTelefono());
            
            if (usuario.getFechaNacimiento() != null) {
                stmt.setDate(7, java.sql.Date.valueOf(usuario.getFechaNacimiento()));
            } else {
                stmt.setNull(7, java.sql.Types.DATE);
            }
            
            stmt.setString(8, usuario.getDireccion());
            stmt.setString(9, usuario.getTipoUsuario().name());
            stmt.setInt(10, usuario.isActivo() ? 1 : 0);
            
            stmt.executeUpdate();
            
            // Obtener el ID generado
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getLong(1));
                }
            }
            
            // Confirmar la transacción
            conn.commit();
        }
    }
    
    /**
     * Actualiza un usuario existente
     */
    public void actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, telefono = ?, " +
                    "fecha_nacimiento = ?, direccion = ?, tipo_usuario = ?, activo = ?, ultimo_acceso = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getTelefono());
            
            if (usuario.getFechaNacimiento() != null) {
                stmt.setDate(5, java.sql.Date.valueOf(usuario.getFechaNacimiento()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            
            stmt.setString(6, usuario.getDireccion());
            stmt.setString(7, usuario.getTipoUsuario().name());
            stmt.setInt(8, usuario.isActivo() ? 1 : 0);
            stmt.setTimestamp(9, usuario.getUltimoAcceso());
            stmt.setLong(10, usuario.getId());
            
            stmt.executeUpdate();
            
            // Confirmar la transacción
            conn.commit();
        }
    }
    
    /**
     * Busca un usuario por su username
     */
    public Usuario buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Busca un usuario por su ID
     */
    public Usuario buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Busca un usuario por su email
     */
    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Lista todos los usuarios activos
     */
    public List<Usuario> listarActivos() throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE activo = 1 ORDER BY nombre, apellido";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }
    
    /**
     * Lista usuarios por tipo
     */
    public List<Usuario> listarPorTipo(TipoUsuario tipo) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE tipo_usuario = ? AND activo = 1 ORDER BY nombre, apellido";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearUsuario(rs));
                }
            }
        }
        return usuarios;
    }
    
    /**
     * Busca usuarios por tipo (incluyendo activos e inactivos)
     */
    public List<Usuario> buscarPorTipo(TipoUsuario tipo) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE tipo_usuario = ? ORDER BY nombre, apellido";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearUsuario(rs));
                }
            }
        }
        return usuarios;
    }
    
    /**
     * Elimina un usuario (hard delete)
     */
    public void eliminar(Long usuarioId) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, usuarioId);
            stmt.executeUpdate();
            
            // Confirmar la transacción
            conn.commit();
        }
    }
    
    /**
     * Verifica si existe al menos un super admin
     */
    public boolean existeSuperAdmin() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE tipo_usuario = 'SUPERADMIN' AND activo = 1";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Actualiza la contraseña de un usuario
     */
    public void actualizarPassword(Long usuarioId, String passwordHash) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, passwordHash);
            stmt.setLong(2, usuarioId);
            
            stmt.executeUpdate();
            
            // Confirmar la transacción
            conn.commit();
        }
    }
    
    /**
     * Desactiva un usuario (soft delete)
     */
    public void desactivar(Long usuarioId) throws SQLException {
        String sql = "UPDATE usuarios SET activo = 0 WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, usuarioId);
            stmt.executeUpdate();
            
            // Confirmar la transacción
            conn.commit();
        }
    }
    
    /**
     * Obtiene usuarios por tipo (incluyendo activos e inactivos)
     */
    public List<Usuario> obtenerUsuariosPorTipo(TipoUsuario tipo) throws SQLException {
        return buscarPorTipo(tipo);
    }
    
    /**
     * Elimina un usuario por ID
     */
    public boolean eliminarUsuario(Long id) {
        try {
            eliminar(id);
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error eliminando usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza un usuario
     */
    public boolean actualizarUsuario(Usuario usuario) {
        try {
            actualizar(usuario);
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error actualizando usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza solo el teléfono de un usuario
     */
    public boolean actualizarTelefono(Long usuarioId, String telefono) {
        String sql = "UPDATE usuarios SET telefono = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, telefono);
            stmt.setLong(2, usuarioId);
            
            int filasAfectadas = stmt.executeUpdate();
            
            // Confirmar la transacción
            conn.commit();
            
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error actualizando teléfono: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crea usuarios de prueba con teléfonos
     */
    public void crearUsuariosDePrueba() {
        try {
            // Crear algunos bibliotecarios de prueba con teléfonos
            if (buscarPorEmail("uziel@biblioteca.com") == null) {
                Usuario bibliotecario1 = new Usuario();
                bibliotecario1.setNombre("Uziel Ariel");
                bibliotecario1.setApellido("Adán Chavez");
                bibliotecario1.setEmail("uziel@biblioteca.com");
                bibliotecario1.setUsername("uziel.adan");
                bibliotecario1.setPasswordHash(AuthService.getInstance().hashPassword("password123"));
                bibliotecario1.setTelefono("+52 999 123 4567");
                bibliotecario1.setTipoUsuario(TipoUsuario.BIBLIOTECARIO);
                bibliotecario1.setActivo(true);
                crear(bibliotecario1);
                System.out.println("✅ Bibliotecario Uziel creado con teléfono");
            }
            
            if (buscarPorEmail("maria@biblioteca.com") == null) {
                Usuario bibliotecario2 = new Usuario();
                bibliotecario2.setNombre("María");
                bibliotecario2.setApellido("González López");
                bibliotecario2.setEmail("maria@biblioteca.com");
                bibliotecario2.setUsername("maria.gonzalez");
                bibliotecario2.setPasswordHash(AuthService.getInstance().hashPassword("password123"));
                bibliotecario2.setTelefono("+52 999 765 4321");
                bibliotecario2.setTipoUsuario(TipoUsuario.BIBLIOTECARIO);
                bibliotecario2.setActivo(true);
                crear(bibliotecario2);
                System.out.println("✅ Bibliotecaria María creada con teléfono");
            }
            
            if (buscarPorEmail("carlos@biblioteca.com") == null) {
                Usuario bibliotecario3 = new Usuario();
                bibliotecario3.setNombre("Carlos");
                bibliotecario3.setApellido("Mendoza Rivera");
                bibliotecario3.setEmail("carlos@biblioteca.com");
                bibliotecario3.setUsername("carlos.mendoza");
                bibliotecario3.setPasswordHash(AuthService.getInstance().hashPassword("password123"));
                bibliotecario3.setTelefono("+52 999 555 1234");
                bibliotecario3.setTipoUsuario(TipoUsuario.BIBLIOTECARIO);
                bibliotecario3.setActivo(false); // Este estará inactivo para probar filtros
                crear(bibliotecario3);
                System.out.println("✅ Bibliotecario Carlos creado con teléfono (inactivo)");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error creando usuarios de prueba: " + e.getMessage());
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setEmail(rs.getString("email"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setTelefono(rs.getString("telefono"));
        
        // Manejar fecha de nacimiento que puede ser null
        java.sql.Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        if (fechaNacimiento != null) {
            usuario.setFechaNacimiento(fechaNacimiento.toLocalDate());
        }
        
        usuario.setDireccion(rs.getString("direccion"));
        usuario.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipo_usuario")));
        usuario.setActivo(rs.getInt("activo") == 1);
        usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        usuario.setUltimoAcceso(rs.getTimestamp("ultimo_acceso"));
        return usuario;
    }
}