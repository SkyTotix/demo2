package com.example.demo2.service;

import com.example.demo2.database.DatabaseManager;
import com.example.demo2.models.Lector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar operaciones CRUD de lectores
 */
public class LectorService {
    
    private static LectorService instance;
    
    private LectorService() {}
    
    public static LectorService getInstance() {
        if (instance == null) {
            instance = new LectorService();
        }
        return instance;
    }
    
    /**
     * Crea un nuevo lector en la base de datos
     */
    public Lector crear(Lector lector) throws SQLException {
        String sql = "INSERT INTO lectores (codigo_lector, nombre, apellido, tipo_documento, " +
                    "numero_documento, email, telefono, direccion, fecha_nacimiento, " +
                    "fecha_vencimiento, estado, foto_url, observaciones, creado_por) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            
            pstmt.setString(1, lector.getCodigoLector());
            pstmt.setString(2, lector.getNombre());
            pstmt.setString(3, lector.getApellido());
            pstmt.setString(4, lector.getTipoDocumento());
            pstmt.setString(5, lector.getNumeroDocumento());
            pstmt.setString(6, lector.getEmail());
            pstmt.setString(7, lector.getTelefono());
            pstmt.setString(8, lector.getDireccion());
            pstmt.setDate(9, Date.valueOf(lector.getFechaNacimiento()));
            pstmt.setDate(10, Date.valueOf(lector.getFechaVencimiento()));
            pstmt.setString(11, lector.getEstado());
            pstmt.setString(12, lector.getFotoUrl());
            pstmt.setString(13, lector.getObservaciones());
            pstmt.setInt(14, lector.getCreadoPor());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        lector.setId(generatedKeys.getInt(1));
                    }
                }
            }
            
            return lector;
        }
    }
    
    /**
     * Actualiza un lector existente
     */
    public boolean actualizar(Lector lector) throws SQLException {
        String sql = "UPDATE lectores SET nombre = ?, apellido = ?, tipo_documento = ?, " +
                    "numero_documento = ?, email = ?, telefono = ?, direccion = ?, " +
                    "fecha_nacimiento = ?, fecha_vencimiento = ?, estado = ?, foto_url = ?, " +
                    "observaciones = ?, actualizado_por = ?, fecha_actualizacion = SYSTIMESTAMP " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, lector.getNombre());
            pstmt.setString(2, lector.getApellido());
            pstmt.setString(3, lector.getTipoDocumento());
            pstmt.setString(4, lector.getNumeroDocumento());
            pstmt.setString(5, lector.getEmail());
            pstmt.setString(6, lector.getTelefono());
            pstmt.setString(7, lector.getDireccion());
            pstmt.setDate(8, Date.valueOf(lector.getFechaNacimiento()));
            pstmt.setDate(9, Date.valueOf(lector.getFechaVencimiento()));
            pstmt.setString(10, lector.getEstado());
            pstmt.setString(11, lector.getFotoUrl());
            pstmt.setString(12, lector.getObservaciones());
            pstmt.setObject(13, lector.getActualizadoPor());
            pstmt.setInt(14, lector.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Elimina un lector por ID
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM lectores WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Busca un lector por ID
     */
    public Lector buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM lectores WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearLector(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Busca un lector por código
     */
    public Lector buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM lectores WHERE codigo_lector = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codigo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearLector(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Busca un lector por número de documento
     */
    public Lector buscarPorDocumento(String numeroDocumento) throws SQLException {
        String sql = "SELECT * FROM lectores WHERE numero_documento = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroDocumento);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearLector(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los lectores
     */
    public List<Lector> obtenerTodos() throws SQLException {
        List<Lector> lectores = new ArrayList<>();
        String sql = "SELECT * FROM lectores ORDER BY apellido, nombre";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                lectores.add(mapearLector(rs));
            }
        }
        return lectores;
    }
    
    /**
     * Obtiene lectores por estado
     */
    public List<Lector> obtenerPorEstado(String estado) throws SQLException {
        List<Lector> lectores = new ArrayList<>();
        String sql = "SELECT * FROM lectores WHERE estado = ? ORDER BY apellido, nombre";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lectores.add(mapearLector(rs));
                }
            }
        }
        return lectores;
    }
    
    /**
     * Busca lectores por nombre o apellido
     */
    public List<Lector> buscarPorNombre(String termino) throws SQLException {
        List<Lector> lectores = new ArrayList<>();
        String sql = "SELECT * FROM lectores WHERE UPPER(nombre) LIKE UPPER(?) " +
                    "OR UPPER(apellido) LIKE UPPER(?) ORDER BY apellido, nombre";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String terminoBusqueda = "%" + termino + "%";
            pstmt.setString(1, terminoBusqueda);
            pstmt.setString(2, terminoBusqueda);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lectores.add(mapearLector(rs));
                }
            }
        }
        return lectores;
    }
    
    /**
     * Verifica si existe un lector con el mismo código
     */
    public boolean existeCodigo(String codigo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM lectores WHERE codigo_lector = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codigo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Verifica si existe un lector con el mismo documento (excluyendo un ID)
     */
    public boolean existeDocumento(String numeroDocumento, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM lectores WHERE numero_documento = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroDocumento);
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Verifica si existe un lector con el mismo email (excluyendo un ID)
     */
    public boolean existeEmail(String email, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM lectores WHERE email = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Genera un nuevo código de lector
     */
    public String generarCodigoLector() throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(codigo_lector, 5))), 0) + 1 " +
                    "FROM lectores WHERE REGEXP_LIKE(codigo_lector, '^LEC-[0-9]+$')";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int siguiente = rs.getInt(1);
                return String.format("LEC-%06d", siguiente);
            }
        }
        return "LEC-000001";
    }
    
    /**
     * Actualiza el estado de lectores vencidos
     */
    public int actualizarLectoresVencidos() throws SQLException {
        String sql = "UPDATE lectores SET estado = 'VENCIDO' " +
                    "WHERE estado = 'ACTIVO' AND fecha_vencimiento < TRUNC(SYSDATE)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            return stmt.executeUpdate(sql);
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Lector
     */
    private Lector mapearLector(ResultSet rs) throws SQLException {
        Lector lector = new Lector();
        lector.setId(rs.getInt("id"));
        lector.setCodigoLector(rs.getString("codigo_lector"));
        lector.setNombre(rs.getString("nombre"));
        lector.setApellido(rs.getString("apellido"));
        lector.setTipoDocumento(rs.getString("tipo_documento"));
        lector.setNumeroDocumento(rs.getString("numero_documento"));
        lector.setEmail(rs.getString("email"));
        lector.setTelefono(rs.getString("telefono"));
        lector.setDireccion(rs.getString("direccion"));
        
        Date fechaNac = rs.getDate("fecha_nacimiento");
        if (fechaNac != null) {
            lector.setFechaNacimiento(fechaNac.toLocalDate());
        }
        
        Timestamp fechaReg = rs.getTimestamp("fecha_registro");
        if (fechaReg != null) {
            lector.setFechaRegistro(fechaReg.toLocalDateTime());
        }
        
        Date fechaVenc = rs.getDate("fecha_vencimiento");
        if (fechaVenc != null) {
            lector.setFechaVencimiento(fechaVenc.toLocalDate());
        }
        
        lector.setEstado(rs.getString("estado"));
        lector.setFotoUrl(rs.getString("foto_url"));
        lector.setObservaciones(rs.getString("observaciones"));
        lector.setCreadoPor(rs.getInt("creado_por"));
        
        int actualizadoPor = rs.getInt("actualizado_por");
        if (!rs.wasNull()) {
            lector.setActualizadoPor(actualizadoPor);
        }
        
        Timestamp fechaAct = rs.getTimestamp("fecha_actualizacion");
        if (fechaAct != null) {
            lector.setFechaActualizacion(fechaAct.toLocalDateTime());
        }
        
        return lector;
    }
    
    /**
     * Crea lectores de prueba
     */
    public void crearLectoresDePrueba() {
        try {
            // Verificar si ya existen lectores
            if (!obtenerTodos().isEmpty()) {
                System.out.println("✅ Ya existen lectores en la base de datos");
                return;
            }
            
            // Obtener ID del primer admin para usarlo como creador
            int adminId = 1; // Asumiendo que el admin tiene ID 1
            
            // Crear lectores de prueba
            List<Lector> lectoresPrueba = new ArrayList<>();
            
            // Lector 1
            Lector lector1 = new Lector();
            lector1.setCodigoLector(generarCodigoLector());
            lector1.setNombre("Juan");
            lector1.setApellido("Pérez García");
            lector1.setTipoDocumento("DNI");
            lector1.setNumeroDocumento("12345678");
            lector1.setEmail("juan.perez@email.com");
            lector1.setTelefono("555-0101");
            lector1.setDireccion("Av. Principal 123, Lima");
            lector1.setFechaNacimiento(LocalDate.of(1990, 5, 15));
            lector1.setFechaVencimiento(LocalDate.now().plusYears(1));
            lector1.setEstado("ACTIVO");
            lector1.setCreadoPor(adminId);
            lectoresPrueba.add(lector1);
            
            // Lector 2
            Lector lector2 = new Lector();
            lector2.setCodigoLector(generarCodigoLector());
            lector2.setNombre("María");
            lector2.setApellido("González López");
            lector2.setTipoDocumento("DNI");
            lector2.setNumeroDocumento("87654321");
            lector2.setEmail("maria.gonzalez@email.com");
            lector2.setTelefono("555-0102");
            lector2.setDireccion("Jr. Las Flores 456, Miraflores");
            lector2.setFechaNacimiento(LocalDate.of(1985, 8, 22));
            lector2.setFechaVencimiento(LocalDate.now().plusYears(1));
            lector2.setEstado("ACTIVO");
            lector2.setCreadoPor(adminId);
            lectoresPrueba.add(lector2);
            
            // Lector 3
            Lector lector3 = new Lector();
            lector3.setCodigoLector(generarCodigoLector());
            lector3.setNombre("Carlos");
            lector3.setApellido("Rodríguez Martín");
            lector3.setTipoDocumento("CARNET_EXTRANJERIA");
            lector3.setNumeroDocumento("CE-123456");
            lector3.setEmail("carlos.rodriguez@email.com");
            lector3.setTelefono("555-0103");
            lector3.setDireccion("Calle Los Pinos 789, San Isidro");
            lector3.setFechaNacimiento(LocalDate.of(1995, 3, 10));
            lector3.setFechaVencimiento(LocalDate.now().plusMonths(6));
            lector3.setEstado("ACTIVO");
            lector3.setCreadoPor(adminId);
            lectoresPrueba.add(lector3);
            
            // Lector 4 (Suspendido)
            Lector lector4 = new Lector();
            lector4.setCodigoLector(generarCodigoLector());
            lector4.setNombre("Ana");
            lector4.setApellido("Ramírez Silva");
            lector4.setTipoDocumento("DNI");
            lector4.setNumeroDocumento("11223344");
            lector4.setEmail("ana.ramirez@email.com");
            lector4.setTelefono("555-0104");
            lector4.setDireccion("Av. Los Álamos 321, Surco");
            lector4.setFechaNacimiento(LocalDate.of(2000, 12, 5));
            lector4.setFechaVencimiento(LocalDate.now().plusYears(1));
            lector4.setEstado("SUSPENDIDO");
            lector4.setObservaciones("Suspendido por no devolver libros a tiempo");
            lector4.setCreadoPor(adminId);
            lectoresPrueba.add(lector4);
            
            // Lector 5 (Vencido)
            Lector lector5 = new Lector();
            lector5.setCodigoLector(generarCodigoLector());
            lector5.setNombre("Luis");
            lector5.setApellido("Torres Vega");
            lector5.setTipoDocumento("PASAPORTE");
            lector5.setNumeroDocumento("P-987654");
            lector5.setEmail("luis.torres@email.com");
            lector5.setTelefono("555-0105");
            lector5.setDireccion("Jr. Las Palmeras 567, La Molina");
            lector5.setFechaNacimiento(LocalDate.of(1988, 7, 18));
            lector5.setFechaVencimiento(LocalDate.now().minusDays(30)); // Vencido hace 30 días
            lector5.setEstado("VENCIDO");
            lector5.setCreadoPor(adminId);
            lectoresPrueba.add(lector5);
            
            // Crear todos los lectores
            for (Lector lector : lectoresPrueba) {
                crear(lector);
                System.out.println("✅ Lector creado: " + lector.getNombreCompleto() + 
                                 " (" + lector.getCodigoLector() + ")");
            }
            
            System.out.println("✅ " + lectoresPrueba.size() + " lectores de prueba creados exitosamente");
            
        } catch (SQLException e) {
            System.err.println("❌ Error creando lectores de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 