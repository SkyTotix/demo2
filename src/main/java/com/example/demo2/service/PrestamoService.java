package com.example.demo2.service;

import com.example.demo2.database.DatabaseManager;
import com.example.demo2.models.Prestamo;
import com.example.demo2.models.Libro;
import com.example.demo2.models.Lector;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar operaciones CRUD de préstamos
 */
public class PrestamoService {
    
    private static PrestamoService instance;
    
    private PrestamoService() {}
    
    public static PrestamoService getInstance() {
        if (instance == null) {
            instance = new PrestamoService();
        }
        return instance;
    }
    
    /**
     * Crea un nuevo préstamo en la base de datos
     */
    public Prestamo crear(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamos (codigo_prestamo, libro_id, lector_id, " +
                    "bibliotecario_prestamo_id, fecha_devolucion_esperada, estado, " +
                    "condicion_prestamo, observaciones_prestamo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            
            pstmt.setString(1, prestamo.getCodigoPrestamo());
            pstmt.setLong(2, prestamo.getLibroId());
            pstmt.setLong(3, prestamo.getLectorId());
            pstmt.setLong(4, prestamo.getBibliotecarioPrestamoId());
            pstmt.setDate(5, Date.valueOf(prestamo.getFechaDevolucionEsperada()));
            pstmt.setString(6, prestamo.getEstado());
            pstmt.setString(7, prestamo.getCondicionPrestamo());
            pstmt.setString(8, prestamo.getObservacionesPrestamo());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        prestamo.setId(generatedKeys.getLong(1));
                    }
                }
                
                // Actualizar cantidad disponible del libro
                actualizarCantidadDisponible(prestamo.getLibroId(), -1);
            }
            
            return prestamo;
        }
    }
    
    /**
     * Actualiza un préstamo existente
     */
    public boolean actualizar(Prestamo prestamo) throws SQLException {
        String sql = "UPDATE prestamos SET estado = ?, fecha_devolucion_real = ?, " +
                    "bibliotecario_devolucion_id = ?, condicion_devolucion = ?, " +
                    "observaciones_devolucion = ?, multa = ?, multa_pagada = ? " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, prestamo.getEstado());
            
            if (prestamo.getFechaDevolucionReal() != null) {
                pstmt.setTimestamp(2, prestamo.getFechaDevolucionReal());
            } else {
                pstmt.setNull(2, Types.TIMESTAMP);
            }
            
            if (prestamo.getBibliotecarioDevolucionId() != null) {
                pstmt.setLong(3, prestamo.getBibliotecarioDevolucionId());
            } else {
                pstmt.setNull(3, Types.BIGINT);
            }
            
            pstmt.setString(4, prestamo.getCondicionDevolucion());
            pstmt.setString(5, prestamo.getObservacionesDevolucion());
            pstmt.setDouble(6, prestamo.getMulta());
            pstmt.setBoolean(7, prestamo.isMultaPagada());
            pstmt.setLong(8, prestamo.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Registra la devolución de un libro
     */
    public boolean registrarDevolucion(Long prestamoId, Long bibliotecarioId, 
                                     String condicionDevolucion, String observaciones) throws SQLException {
        String sql = "UPDATE prestamos SET estado = 'DEVUELTO', fecha_devolucion_real = SYSTIMESTAMP, " +
                    "bibliotecario_devolucion_id = ?, condicion_devolucion = ?, " +
                    "observaciones_devolucion = ? WHERE id = ? AND estado = 'ACTIVO'";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, bibliotecarioId);
            pstmt.setString(2, condicionDevolucion);
            pstmt.setString(3, observaciones);
            pstmt.setLong(4, prestamoId);
            
            int rowsUpdated = pstmt.executeUpdate();
            
            if (rowsUpdated > 0) {
                // Obtener el libro_id del préstamo para actualizar cantidad
                Prestamo prestamo = buscarPorId(prestamoId);
                if (prestamo != null) {
                    actualizarCantidadDisponible(prestamo.getLibroId(), 1);
                }
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Elimina un préstamo por ID (solo si no está activo)
     */
    public boolean eliminar(Long id) throws SQLException {
        // Primero verificar si el préstamo está activo
        Prestamo prestamo = buscarPorId(id);
        if (prestamo != null && prestamo.isActivo()) {
            throw new SQLException("No se puede eliminar un préstamo activo. Debe devolverse primero.");
        }
        
        String sql = "DELETE FROM prestamos WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Busca un préstamo por ID con información completa
     */
    public Prestamo buscarPorId(Long id) throws SQLException {
        String sql = """
            SELECT p.*, l.titulo, l.isbn, l.autor,
                   lec.codigo_lector, lec.nombre || ' ' || lec.apellido AS lector_nombre,
                   bp.nombre || ' ' || bp.apellido AS bibliotecario_prestamo_nombre,
                   bd.nombre || ' ' || bd.apellido AS bibliotecario_devolucion_nombre
            FROM prestamos p
            JOIN libros l ON p.libro_id = l.id
            JOIN lectores lec ON p.lector_id = lec.id
            JOIN usuarios bp ON p.bibliotecario_prestamo_id = bp.id
            LEFT JOIN usuarios bd ON p.bibliotecario_devolucion_id = bd.id
            WHERE p.id = ?
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestamoCompleto(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Busca un préstamo por código
     */
    public Prestamo buscarPorCodigo(String codigo) throws SQLException {
        String sql = """
            SELECT p.*, l.titulo, l.isbn, l.autor,
                   lec.codigo_lector, lec.nombre || ' ' || lec.apellido AS lector_nombre,
                   bp.nombre || ' ' || bp.apellido AS bibliotecario_prestamo_nombre,
                   bd.nombre || ' ' || bd.apellido AS bibliotecario_devolucion_nombre
            FROM prestamos p
            JOIN libros l ON p.libro_id = l.id
            JOIN lectores lec ON p.lector_id = lec.id
            JOIN usuarios bp ON p.bibliotecario_prestamo_id = bp.id
            LEFT JOIN usuarios bd ON p.bibliotecario_devolucion_id = bd.id
            WHERE p.codigo_prestamo = ?
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codigo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestamoCompleto(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los préstamos con información completa
     */
    public List<Prestamo> obtenerTodos() throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = """
            SELECT p.*, l.titulo, l.isbn, l.autor,
                   lec.codigo_lector, lec.nombre || ' ' || lec.apellido AS lector_nombre,
                   bp.nombre || ' ' || bp.apellido AS bibliotecario_prestamo_nombre,
                   bd.nombre || ' ' || bd.apellido AS bibliotecario_devolucion_nombre
            FROM prestamos p
            JOIN libros l ON p.libro_id = l.id
            JOIN lectores lec ON p.lector_id = lec.id
            JOIN usuarios bp ON p.bibliotecario_prestamo_id = bp.id
            LEFT JOIN usuarios bd ON p.bibliotecario_devolucion_id = bd.id
            ORDER BY p.fecha_prestamo DESC
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                prestamos.add(mapearPrestamoCompleto(rs));
            }
        }
        return prestamos;
    }
    
    /**
     * Obtiene préstamos por estado
     */
    public List<Prestamo> obtenerPorEstado(String estado) throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = """
            SELECT p.*, l.titulo, l.isbn, l.autor,
                   lec.codigo_lector, lec.nombre || ' ' || lec.apellido AS lector_nombre,
                   bp.nombre || ' ' || bp.apellido AS bibliotecario_prestamo_nombre,
                   bd.nombre || ' ' || bd.apellido AS bibliotecario_devolucion_nombre
            FROM prestamos p
            JOIN libros l ON p.libro_id = l.id
            JOIN lectores lec ON p.lector_id = lec.id
            JOIN usuarios bp ON p.bibliotecario_prestamo_id = bp.id
            LEFT JOIN usuarios bd ON p.bibliotecario_devolucion_id = bd.id
            WHERE p.estado = ?
            ORDER BY p.fecha_prestamo DESC
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapearPrestamoCompleto(rs));
                }
            }
        }
        return prestamos;
    }
    
    /**
     * Obtiene préstamos activos (pendientes de devolución)
     */
    public List<Prestamo> obtenerActivos() throws SQLException {
        return obtenerPorEstado("ACTIVO");
    }
    
    /**
     * Obtiene préstamos vencidos
     */
    public List<Prestamo> obtenerVencidos() throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = """
            SELECT p.*, l.titulo, l.isbn, l.autor,
                   lec.codigo_lector, lec.nombre || ' ' || lec.apellido AS lector_nombre,
                   bp.nombre || ' ' || bp.apellido AS bibliotecario_prestamo_nombre,
                   bd.nombre || ' ' || bd.apellido AS bibliotecario_devolucion_nombre
            FROM prestamos p
            JOIN libros l ON p.libro_id = l.id
            JOIN lectores lec ON p.lector_id = lec.id
            JOIN usuarios bp ON p.bibliotecario_prestamo_id = bp.id
            LEFT JOIN usuarios bd ON p.bibliotecario_devolucion_id = bd.id
            WHERE p.estado = 'ACTIVO' AND p.fecha_devolucion_esperada < TRUNC(SYSDATE)
            ORDER BY p.fecha_devolucion_esperada ASC
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                prestamos.add(mapearPrestamoCompleto(rs));
            }
        }
        return prestamos;
    }
    
    /**
     * Obtiene préstamos de un lector específico
     */
    public List<Prestamo> obtenerPorLector(Long lectorId) throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = """
            SELECT p.*, l.titulo, l.isbn, l.autor,
                   lec.codigo_lector, lec.nombre || ' ' || lec.apellido AS lector_nombre,
                   bp.nombre || ' ' || bp.apellido AS bibliotecario_prestamo_nombre,
                   bd.nombre || ' ' || bd.apellido AS bibliotecario_devolucion_nombre
            FROM prestamos p
            JOIN libros l ON p.libro_id = l.id
            JOIN lectores lec ON p.lector_id = lec.id
            JOIN usuarios bp ON p.bibliotecario_prestamo_id = bp.id
            LEFT JOIN usuarios bd ON p.bibliotecario_devolucion_id = bd.id
            WHERE p.lector_id = ?
            ORDER BY p.fecha_prestamo DESC
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, lectorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapearPrestamoCompleto(rs));
                }
            }
        }
        return prestamos;
    }
    
    /**
     * Verifica si existe un código de préstamo
     */
    public boolean existeCodigo(String codigo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE codigo_prestamo = ?";
        
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
     * Verifica si un libro está disponible para préstamo
     */
    public boolean libroDisponible(Long libroId) throws SQLException {
        String sql = "SELECT cantidad_disponible FROM libros WHERE id = ? AND activo = 1";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, libroId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cantidad_disponible") > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Verifica si un lector tiene préstamos activos pendientes
     */
    public boolean lectorTienePrestamosActivos(Long lectorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE lector_id = ? AND estado = 'ACTIVO'";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, lectorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Genera un nuevo código de préstamo
     */
    public String generarCodigoPrestamo() throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(codigo_prestamo, 5))), 0) + 1 " +
                    "FROM prestamos WHERE REGEXP_LIKE(codigo_prestamo, '^PRES-[0-9]+$')";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int siguiente = rs.getInt(1);
                return String.format("PRES-%06d", siguiente);
            }
        }
        return "PRES-000001";
    }
    
    /**
     * Actualiza estados de préstamos vencidos
     */
    public int actualizarPrestamosVencidos() throws SQLException {
        String sql = "UPDATE prestamos SET estado = 'VENCIDO' " +
                    "WHERE estado = 'ACTIVO' AND fecha_devolucion_esperada < TRUNC(SYSDATE)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            return stmt.executeUpdate(sql);
        }
    }
    
    /**
     * Calcula y actualiza multas para préstamos vencidos usando configuración del sistema
     */
    public int calcularMultasConConfiguracion() throws SQLException {
        ConfigurationService configService = ConfigurationService.getInstance();
        var config = configService.getConfiguracion();
        
        // Usar configuración del sistema para calcular multas
        return calcularMultasPersonalizadas(
            config.diasGraciaMulta,
            config.montoMultaDiario,
            config.montoMultaMaxima
        );
    }
    
    /**
     * Calcula y actualiza multas para préstamos vencidos con parámetros personalizados
     */
    public int calcularMultasPersonalizadas(int diasGracia, double multaPorDia, double multaMaxima) throws SQLException {
        String sql = """
            UPDATE prestamos 
            SET multa = LEAST(
                GREATEST(0, (TRUNC(SYSDATE) - fecha_devolucion_esperada - ?)) * ?,
                ?
            )
            WHERE estado IN ('ACTIVO', 'VENCIDO') 
            AND fecha_devolucion_esperada < TRUNC(SYSDATE) - ?
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, diasGracia);           // Días de gracia
            pstmt.setDouble(2, multaPorDia);       // Monto por día
            pstmt.setDouble(3, multaMaxima);       // Monto máximo
            pstmt.setInt(4, diasGracia);           // Días de gracia para WHERE
            
            int prestamosActualizados = pstmt.executeUpdate();
            
            System.out.println("💰 Multas calculadas automáticamente:");
            System.out.println("   - Días de gracia: " + diasGracia);
            System.out.println("   - Multa por día: $" + multaPorDia);
            System.out.println("   - Multa máxima: $" + multaMaxima);
            System.out.println("   - Préstamos actualizados: " + prestamosActualizados);
            
            return prestamosActualizados;
        }
    }
    
    /**
     * Calcula multa individual para un préstamo específico
     */
    public double calcularMultaIndividual(Prestamo prestamo) {
        ConfigurationService configService = ConfigurationService.getInstance();
        var config = configService.getConfiguracion();
        
        return calcularMultaIndividual(prestamo, config.diasGraciaMulta, config.montoMultaDiario, config.montoMultaMaxima);
    }
    
    /**
     * Calcula multa individual para un préstamo con parámetros personalizados
     */
    public double calcularMultaIndividual(Prestamo prestamo, int diasGracia, double multaPorDia, double multaMaxima) {
        if (!"ACTIVO".equals(prestamo.getEstado()) && !"VENCIDO".equals(prestamo.getEstado())) {
            return 0.0;
        }
        
        LocalDate fechaVencimiento = prestamo.getFechaDevolucionEsperada();
        LocalDate fechaActual = LocalDate.now();
        
        // Calcular días de retraso considerando período de gracia
        long diasRetraso = fechaActual.toEpochDay() - fechaVencimiento.toEpochDay() - diasGracia;
        
        if (diasRetraso <= 0) {
            return 0.0; // Dentro del período de gracia
        }
        
        // Calcular multa = días de retraso * monto por día, pero no mayor al máximo
        double multaCalculada = diasRetraso * multaPorDia;
        return Math.min(multaCalculada, multaMaxima);
    }
    
    /**
     * Calcula y actualiza multas para préstamos vencidos (método legacy)
     */
    public int calcularMultas(double multaPorDia) throws SQLException {
        String sql = """
            UPDATE prestamos 
            SET multa = (TRUNC(SYSDATE) - fecha_devolucion_esperada) * ?
            WHERE estado IN ('ACTIVO', 'VENCIDO') 
            AND fecha_devolucion_esperada < TRUNC(SYSDATE)
            AND multa = 0
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, multaPorDia);
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * Obtiene información detallada sobre configuración de multas
     */
    public String obtenerInformacionMultas() {
        ConfigurationService configService = ConfigurationService.getInstance();
        var config = configService.getConfiguracion();
        
        StringBuilder info = new StringBuilder();
        info.append("📊 CONFIGURACIÓN ACTUAL DE MULTAS:\n");
        info.append("   • Días de gracia: ").append(config.diasGraciaMulta).append(" días\n");
        info.append("   • Multa por día: $").append(String.format("%.2f", config.montoMultaDiario)).append("\n");
        info.append("   • Multa máxima: $").append(String.format("%.2f", config.montoMultaMaxima)).append("\n");
        info.append("   • Ejemplo: Un préstamo con 10 días de retraso tendría una multa de $");
        
        double multaEjemplo = Math.min((10 - config.diasGraciaMulta) * config.montoMultaDiario, config.montoMultaMaxima);
        if (multaEjemplo <= 0) {
            info.append("0.00 (dentro del período de gracia)");
        } else {
            info.append(String.format("%.2f", multaEjemplo));
        }
        
        return info.toString();
    }
    
    /**
     * Obtiene estadísticas de multas del sistema
     */
    public String obtenerEstadisticasMultas() throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as total_multas,
                SUM(multa) as total_monto,
                AVG(multa) as promedio_multa,
                MAX(multa) as multa_maxima,
                COUNT(CASE WHEN multa_pagada = 1 THEN 1 END) as multas_pagadas,
                COUNT(CASE WHEN multa_pagada = 0 AND multa > 0 THEN 1 END) as multas_pendientes
            FROM prestamos 
            WHERE multa > 0
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                StringBuilder stats = new StringBuilder();
                stats.append("📈 ESTADÍSTICAS DE MULTAS:\n");
                stats.append("   • Total de multas: ").append(rs.getInt("total_multas")).append("\n");
                stats.append("   • Monto total: $").append(String.format("%.2f", rs.getDouble("total_monto"))).append("\n");
                stats.append("   • Promedio por multa: $").append(String.format("%.2f", rs.getDouble("promedio_multa"))).append("\n");
                stats.append("   • Multa más alta: $").append(String.format("%.2f", rs.getDouble("multa_maxima"))).append("\n");
                stats.append("   • Multas pagadas: ").append(rs.getInt("multas_pagadas")).append("\n");
                stats.append("   • Multas pendientes: ").append(rs.getInt("multas_pendientes"));
                
                return stats.toString();
            }
        }
        
        return "No hay datos de multas disponibles";
    }
    
    /**
     * Actualiza la cantidad disponible de un libro
     */
    private void actualizarCantidadDisponible(Long libroId, int cambio) throws SQLException {
        String sql = "UPDATE libros SET cantidad_disponible = cantidad_disponible + ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cambio);
            pstmt.setLong(2, libroId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Prestamo completo
     */
    private Prestamo mapearPrestamoCompleto(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(rs.getLong("id"));
        prestamo.setCodigoPrestamo(rs.getString("codigo_prestamo"));
        prestamo.setLibroId(rs.getLong("libro_id"));
        prestamo.setLectorId(rs.getLong("lector_id"));
        prestamo.setBibliotecarioPrestamoId(rs.getLong("bibliotecario_prestamo_id"));
        
        Long bibliotecarioDevId = rs.getLong("bibliotecario_devolucion_id");
        if (!rs.wasNull()) {
            prestamo.setBibliotecarioDevolucionId(bibliotecarioDevId);
        }
        
        Timestamp fechaPrestamo = rs.getTimestamp("fecha_prestamo");
        if (fechaPrestamo != null) {
            prestamo.setFechaPrestamo(fechaPrestamo);
        }
        
        Date fechaDevEsperada = rs.getDate("fecha_devolucion_esperada");
        if (fechaDevEsperada != null) {
            prestamo.setFechaDevolucionEsperada(fechaDevEsperada.toLocalDate());
        }
        
        Timestamp fechaDevReal = rs.getTimestamp("fecha_devolucion_real");
        if (fechaDevReal != null) {
            prestamo.setFechaDevolucionReal(fechaDevReal);
        }
        
        prestamo.setEstado(rs.getString("estado"));
        prestamo.setCondicionPrestamo(rs.getString("condicion_prestamo"));
        prestamo.setCondicionDevolucion(rs.getString("condicion_devolucion"));
        prestamo.setObservacionesPrestamo(rs.getString("observaciones_prestamo"));
        prestamo.setObservacionesDevolucion(rs.getString("observaciones_devolucion"));
        prestamo.setMulta(rs.getDouble("multa"));
        prestamo.setMultaPagada(rs.getBoolean("multa_pagada"));
        
        // Información adicional
        prestamo.setLibroTitulo(rs.getString("titulo"));
        prestamo.setLibroIsbn(rs.getString("isbn"));
        prestamo.setLectorCodigo(rs.getString("codigo_lector"));
        prestamo.setLectorNombre(rs.getString("lector_nombre"));
        prestamo.setBibliotecarioPrestamoNombre(rs.getString("bibliotecario_prestamo_nombre"));
        prestamo.setBibliotecarioDevolucionNombre(rs.getString("bibliotecario_devolucion_nombre"));
        
        return prestamo;
    }
    
    /**
     * Crea préstamos de prueba
     */
    public void crearPrestamosDePrueba() {
        try {
            // Verificar si ya existen préstamos
            if (!obtenerTodos().isEmpty()) {
                System.out.println("✅ Ya existen préstamos en la base de datos");
                return;
            }
            
            // Obtener datos necesarios
            LectorService lectorService = LectorService.getInstance();
            LibroService libroService = LibroService.getInstance();
            UsuarioService usuarioService = UsuarioService.getInstance();
            
            var lectores = lectorService.obtenerTodos();
            var libros = libroService.obtenerTodosLosLibros();
            var bibliotecarios = usuarioService.buscarPorTipo(com.example.demo2.models.enums.TipoUsuario.BIBLIOTECARIO);
            
            if (lectores.isEmpty() || libros.isEmpty() || bibliotecarios.isEmpty()) {
                System.out.println("⚠️ No hay suficientes datos para crear préstamos de prueba");
                return;
            }
            
            List<Prestamo> prestamosPrueba = new ArrayList<>();
            
            // Préstamo 1 - Activo
            if (lectores.size() > 0 && libros.size() > 0) {
                Prestamo prestamo1 = new Prestamo();
                prestamo1.setCodigoPrestamo(generarCodigoPrestamo());
                prestamo1.setLibroId((long) libros.get(0).getId());
                prestamo1.setLectorId((long) lectores.get(0).getId());
                prestamo1.setBibliotecarioPrestamoId(bibliotecarios.get(0).getId());
                prestamo1.setFechaDevolucionEsperada(LocalDate.now().plusDays(15));
                prestamo1.setEstado("ACTIVO");
                prestamo1.setCondicionPrestamo("Excelente");
                prestamo1.setObservacionesPrestamo("Préstamo regular");
                prestamosPrueba.add(prestamo1);
            }
            
            // Préstamo 2 - Vencido
            if (lectores.size() > 1 && libros.size() > 1) {
                Prestamo prestamo2 = new Prestamo();
                prestamo2.setCodigoPrestamo(generarCodigoPrestamo());
                prestamo2.setLibroId((long) libros.get(1).getId());
                prestamo2.setLectorId((long) lectores.get(1).getId());
                prestamo2.setBibliotecarioPrestamoId(bibliotecarios.get(0).getId());
                prestamo2.setFechaDevolucionEsperada(LocalDate.now().minusDays(5)); // Vencido
                prestamo2.setEstado("VENCIDO");
                prestamo2.setCondicionPrestamo("Buena");
                prestamo2.setObservacionesPrestamo("Préstamo con retraso");
                prestamo2.setMulta(25.0); // Multa por retraso
                prestamosPrueba.add(prestamo2);
            }
            
            // Préstamo 3 - Devuelto
            if (lectores.size() > 2 && libros.size() > 2) {
                Prestamo prestamo3 = new Prestamo();
                prestamo3.setCodigoPrestamo(generarCodigoPrestamo());
                prestamo3.setLibroId((long) libros.get(2).getId());
                prestamo3.setLectorId((long) lectores.get(2).getId());
                prestamo3.setBibliotecarioPrestamoId(bibliotecarios.get(0).getId());
                prestamo3.setBibliotecarioDevolucionId(bibliotecarios.get(0).getId());
                prestamo3.setFechaDevolucionEsperada(LocalDate.now().minusDays(2));
                prestamo3.setFechaDevolucionReal(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
                prestamo3.setEstado("DEVUELTO");
                prestamo3.setCondicionPrestamo("Excelente");
                prestamo3.setCondicionDevolucion("Buena");
                prestamo3.setObservacionesPrestamo("Préstamo normal");
                prestamo3.setObservacionesDevolucion("Devuelto a tiempo");
                prestamosPrueba.add(prestamo3);
            }
            
            // Crear todos los préstamos
            for (Prestamo prestamo : prestamosPrueba) {
                crear(prestamo);
                System.out.println("✅ Préstamo creado: " + prestamo.getCodigoPrestamo() + 
                                 " - Estado: " + prestamo.getEstado());
            }
            
            System.out.println("✅ " + prestamosPrueba.size() + " préstamos de prueba creados exitosamente");
            
        } catch (SQLException e) {
            System.err.println("❌ Error creando préstamos de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * =============================
     * MÉTODOS PARA ESTADÍSTICAS DEL DASHBOARD
     * =============================
     */
    
    /**
     * Obtiene préstamos que vencen en los próximos días especificados
     */
    public List<Prestamo> obtenerPrestamosProximosAVencer(int diasProximidad) {
        List<Prestamo> prestamosProximosAVencer = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT p.id, p.codigo_prestamo, p.fecha_devolucion_esperada, p.estado,
                       p.fecha_prestamo, p.multa, p.multa_pagada,
                       l.titulo, l.isbn,
                       lec.codigo_lector, lec.nombre || ' ' || lec.apellido as lector_nombre,
                       u1.nombre || ' ' || u1.apellido as bibliotecario_prestamo_nombre
                FROM prestamos p
                LEFT JOIN libros l ON p.libro_id = l.id
                LEFT JOIN lectores lec ON p.lector_id = lec.id
                LEFT JOIN usuarios u1 ON p.bibliotecario_prestamo_id = u1.id
                WHERE p.estado = 'ACTIVO' 
                AND p.fecha_devolucion_esperada BETWEEN TRUNC(SYSDATE) AND (TRUNC(SYSDATE) + ?)
                ORDER BY p.fecha_devolucion_esperada ASC
                """;
            
            System.out.println("🔍 Ejecutando consulta para préstamos próximos a vencer...");
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, diasProximidad);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Prestamo prestamo = new Prestamo();
                        prestamo.setId(rs.getLong("id"));
                        prestamo.setCodigoPrestamo(rs.getString("codigo_prestamo"));
                        prestamo.setFechaDevolucionEsperada(rs.getDate("fecha_devolucion_esperada").toLocalDate());
                        prestamo.setEstado(rs.getString("estado"));
                        prestamo.setFechaPrestamo(rs.getTimestamp("fecha_prestamo"));
                        prestamo.setMulta(rs.getDouble("multa"));
                        prestamo.setMultaPagada(rs.getBoolean("multa_pagada"));
                        prestamo.setLibroTitulo(rs.getString("titulo"));
                        prestamo.setLibroIsbn(rs.getString("isbn"));
                        prestamo.setLectorCodigo(rs.getString("codigo_lector"));
                        prestamo.setLectorNombre(rs.getString("lector_nombre"));
                        prestamo.setBibliotecarioPrestamoNombre(rs.getString("bibliotecario_prestamo_nombre"));
                        prestamosProximosAVencer.add(prestamo);
                        
                        System.out.println("✅ Préstamo encontrado: " + prestamo.getCodigoPrestamo() + 
                                         " - " + prestamo.getLibroTitulo() + 
                                         " - Vence: " + prestamo.getFechaDevolucionEsperada());
                    }
                }
            }
            
            System.out.println("📋 Préstamos próximos a vencer (" + diasProximidad + " días): " + prestamosProximosAVencer.size());
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo préstamos próximos a vencer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return prestamosProximosAVencer;
    }
    
    /**
     * Obtiene préstamos vencidos con multa pendiente
     */
    public List<Prestamo> obtenerPrestamosConMulta() {
        List<Prestamo> prestamosConMulta = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT p.id, p.codigo_prestamo, p.fecha_devolucion_esperada, p.estado,
                       p.fecha_prestamo, p.multa, p.multa_pagada,
                       l.titulo, l.isbn,
                       lec.codigo_lector, lec.nombre || ' ' || lec.apellido as lector_nombre,
                       u1.nombre || ' ' || u1.apellido as bibliotecario_prestamo_nombre
                FROM prestamos p
                LEFT JOIN libros l ON p.libro_id = l.id
                LEFT JOIN lectores lec ON p.lector_id = lec.id
                LEFT JOIN usuarios u1 ON p.bibliotecario_prestamo_id = u1.id
                WHERE p.estado = 'ACTIVO' 
                AND p.fecha_devolucion_esperada < TRUNC(SYSDATE)
                AND (p.multa > 0 OR p.fecha_devolucion_esperada < TRUNC(SYSDATE))
                ORDER BY p.fecha_devolucion_esperada ASC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Prestamo prestamo = new Prestamo();
                    prestamo.setId(rs.getLong("id"));
                    prestamo.setCodigoPrestamo(rs.getString("codigo_prestamo"));
                    prestamo.setFechaDevolucionEsperada(rs.getDate("fecha_devolucion_esperada").toLocalDate());
                    prestamo.setEstado(rs.getString("estado"));
                    prestamo.setFechaPrestamo(rs.getTimestamp("fecha_prestamo"));
                    prestamo.setMulta(rs.getDouble("multa"));
                    prestamo.setMultaPagada(rs.getBoolean("multa_pagada"));
                    prestamo.setLibroTitulo(rs.getString("titulo"));
                    prestamo.setLibroIsbn(rs.getString("isbn"));
                    prestamo.setLectorCodigo(rs.getString("codigo_lector"));
                    prestamo.setLectorNombre(rs.getString("lector_nombre"));
                    prestamo.setBibliotecarioPrestamoNombre(rs.getString("bibliotecario_prestamo_nombre"));
                    
                    if (prestamo.getMulta() <= 0 && prestamo.isVencido()) {
                        double multaCalculada = calcularMultaIndividual(prestamo);
                        prestamo.setMulta(multaCalculada);
                    }
                    
                    if (prestamo.getMulta() > 0) {
                        prestamosConMulta.add(prestamo);
                    }
                }
            }
            
            System.out.println("💰 Préstamos con multa: " + prestamosConMulta.size());
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo préstamos con multa: " + e.getMessage());
        }
        
        return prestamosConMulta;
    }
    
    /**
     * Obtiene estadísticas rápidas para el dashboard
     */
    public EstadisticasPrestamos obtenerEstadisticasRapidas() {
        EstadisticasPrestamos stats = new EstadisticasPrestamos();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Contar préstamos próximos a vencer (próximos 3 días)
            String sqlProximos = """
                SELECT COUNT(*) as total
                FROM prestamos 
                WHERE estado = 'ACTIVO' 
                AND fecha_devolucion_esperada BETWEEN TRUNC(SYSDATE) AND (TRUNC(SYSDATE) + 3)
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlProximos);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.prestamosProximosAVencer = rs.getInt("total");
                }
            }
            
            // Contar préstamos con multa
            String sqlMultas = """
                SELECT COUNT(*) as total
                FROM prestamos 
                WHERE estado = 'ACTIVO' 
                AND fecha_devolucion_esperada < TRUNC(SYSDATE)
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlMultas);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.prestamosConMulta = rs.getInt("total");
                }
            }
            
            System.out.println("📊 Estadísticas de préstamos calculadas");
            
        } catch (SQLException e) {
            System.err.println("❌ Error calculando estadísticas de préstamos: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Clase interna para estadísticas rápidas de préstamos
     */
    public static class EstadisticasPrestamos {
        public int prestamosProximosAVencer = 0;
        public int prestamosConMulta = 0;
    }
} 