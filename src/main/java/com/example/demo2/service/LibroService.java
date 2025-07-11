package com.example.demo2.service;

import com.example.demo2.database.DatabaseManager;
import com.example.demo2.models.Libro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Servicio para gestión de libros en el sistema BiblioSystem
 */
public class LibroService {
    private static LibroService instance;
    private DatabaseManager databaseManager;
    
    private LibroService() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    public static LibroService getInstance() {
        if (instance == null) {
            instance = new LibroService();
        }
        return instance;
    }
    
    /**
     * Obtiene todos los libros activos del sistema
     */
    public ObservableList<Libro> obtenerTodosLosLibros() {
        ObservableList<Libro> libros = FXCollections.observableArrayList();
        String sql = "SELECT * FROM libros ORDER BY titulo ASC";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Libro libro = mapearResultSetALibro(rs);
                libros.add(libro);
            }
            
            System.out.println("✅ Cargados " + libros.size() + " libros desde la base de datos");
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo libros: " + e.getMessage());
            e.printStackTrace();
        }
        
        return libros;
    }
    
    /**
     * Busca libros por criterio
     */
    public ObservableList<Libro> buscarLibros(String criterio, String valor) {
        ObservableList<Libro> libros = FXCollections.observableArrayList();
        String sql = "SELECT * FROM libros WHERE UPPER(" + criterio + ") LIKE UPPER(?) ORDER BY titulo ASC";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + valor + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Libro libro = mapearResultSetALibro(rs);
                    libros.add(libro);
                }
            }
            
            System.out.println("🔍 Encontrados " + libros.size() + " libros con " + criterio + " = " + valor);
            
        } catch (SQLException e) {
            System.err.println("❌ Error buscando libros: " + e.getMessage());
            e.printStackTrace();
        }
        
        return libros;
    }
    
    /**
     * Obtiene un libro por su ID
     */
    public Libro obtenerLibroPorId(Long id) {
        String sql = "SELECT * FROM libros WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetALibro(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo libro por ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Busca un libro por ISBN
     */
    public Libro obtenerLibroPorIsbn(String isbn) {
        String sql = "SELECT * FROM libros WHERE isbn = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetALibro(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo libro por ISBN: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Crea un nuevo libro
     */
    public boolean crearLibro(Libro libro) {
        String sql = "INSERT INTO libros (isbn, titulo, autor, editorial, anio_publicacion, categoria, " +
                    "cantidad_total, cantidad_disponible, descripcion, activo, fecha_registro) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, libro.getIsbn());
            stmt.setString(2, libro.getTitulo());
            stmt.setString(3, libro.getAutor());
            stmt.setString(4, libro.getEditorial());
            stmt.setInt(5, libro.getAnioPublicacion());
            stmt.setString(6, libro.getCategoria());
            stmt.setInt(7, libro.getCantidadTotal());
            stmt.setInt(8, libro.getCantidadDisponible());
            stmt.setString(9, libro.getDescripcion());
            stmt.setBoolean(10, libro.isActivo());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                // Para Oracle, buscar el libro recién creado por ISBN para obtener el ID
                Libro libroCreado = obtenerLibroPorIsbn(libro.getIsbn());
                if (libroCreado != null) {
                    libro.setId(libroCreado.getId());
                }
                
                System.out.println("✅ Libro creado exitosamente: " + libro.getTitulo());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creando libro: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Actualiza un libro existente
     */
    public boolean actualizarLibro(Libro libro) {
        String sql = "UPDATE libros SET isbn = ?, titulo = ?, autor = ?, editorial = ?, " +
                    "anio_publicacion = ?, categoria = ?, cantidad_total = ?, cantidad_disponible = ?, " +
                    "descripcion = ?, activo = ? WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, libro.getIsbn());
            stmt.setString(2, libro.getTitulo());
            stmt.setString(3, libro.getAutor());
            stmt.setString(4, libro.getEditorial());
            stmt.setInt(5, libro.getAnioPublicacion());
            stmt.setString(6, libro.getCategoria());
            stmt.setInt(7, libro.getCantidadTotal());
            stmt.setInt(8, libro.getCantidadDisponible());
            stmt.setString(9, libro.getDescripcion());
            stmt.setBoolean(10, libro.isActivo());
            stmt.setLong(11, libro.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✅ Libro actualizado exitosamente: " + libro.getTitulo());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error actualizando libro: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Elimina un libro (soft delete)
     */
    public boolean eliminarLibro(Long id) {
        String sql = "UPDATE libros SET activo = false WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✅ Libro desactivado exitosamente");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error eliminando libro: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Obtiene todas las categorías disponibles
     */
    public List<String> obtenerCategorias() {
        List<String> categorias = new ArrayList<>();
        String sql = "SELECT DISTINCT categoria FROM libros WHERE categoria IS NOT NULL ORDER BY categoria";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categorias.add(rs.getString("categoria"));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo categorías: " + e.getMessage());
        }
        
        // Crear lista completa de categorías (predefinidas + existentes en BD)
        List<String> categoriasCompletas = new ArrayList<>();
        
        // Agregar todas las categorías predefinidas
        categoriasCompletas.addAll(List.of(
            // === FICCIÓN ===
            "Ficción",
            "Ficción Clásica",
            "Ficción Contemporánea",
            "Ficción Histórica",
            "Ficción Realista",
            "Ficción Experimental",
            
            // === GÉNEROS DE FICCIÓN ===
            "Ciencia Ficción",
            "Fantasía",
            "Fantasía Épica",
            "Fantasía Urbana",
            "Terror",
            "Suspense",
            "Thriller",
            "Misterio",
            "Policial",
            "Novela Negra",
            "Romance",
            "Romance Histórico",
            "Romance Contemporáneo",
            "Aventura",
            "Western",
            "Distopía",
            "Utopía",
            
            // === NO FICCIÓN ===
            "No Ficción",
            "Biografía",
            "Autobiografía",
            "Memorias",
            "Historia",
            "Historia Universal",
            "Historia de México",
            "Historia Antigua",
            "Historia Medieval",
            "Historia Moderna",
            "Historia Contemporánea",
            
            // === CIENCIAS ===
            "Ciencia",
            "Física",
            "Química",
            "Biología",
            "Matemáticas",
            "Astronomía",
            "Geología",
            "Medicina",
            "Anatomía",
            "Psicología",
            "Sociología",
            "Antropología",
            
            // === FILOSOFÍA Y RELIGIÓN ===
            "Filosofía",
            "Filosofía Antigua",
            "Filosofía Moderna",
            "Ética",
            "Lógica",
            "Metafísica",
            "Religión",
            "Teología",
            "Espiritualidad",
            "Mitología",
            
            // === ARTES ===
            "Arte",
            "Historia del Arte",
            "Pintura",
            "Escultura",
            "Arquitectura",
            "Fotografía",
            "Música",
            "Teatro",
            "Danza",
            "Cine",
            
            // === LITERATURA ===
            "Literatura",
            "Poesía",
            "Drama",
            "Ensayo",
            "Crítica Literaria",
            "Literatura Clásica",
            "Literatura Contemporánea",
            "Literatura Hispanoamericana",
            "Literatura Española",
            "Literatura Universal",
            
            // === EDUCACIÓN Y DESARROLLO ===
            "Educación",
            "Pedagogía",
            "Didáctica",
            "Psicología Educativa",
            "Desarrollo Personal",
            "Autoayuda",
            "Motivación",
            "Liderazgo",
            "Coaching",
            
            // === TECNOLOGÍA ===
            "Tecnología",
            "Informática",
            "Programación",
            "Internet",
            "Inteligencia Artificial",
            "Robótica",
            "Ingeniería",
            "Electrónica",
            
            // === DEPORTES Y SALUD ===
            "Deportes",
            "Fitness",
            "Nutrición",
            "Salud",
            "Medicina Alternativa",
            "Yoga",
            "Meditación",
            
            // === ECONOMÍA Y NEGOCIOS ===
            "Economía",
            "Finanzas",
            "Negocios",
            "Marketing",
            "Administración",
            "Emprendimiento",
            "Inversiones",
            "Contabilidad",
            
            // === POLÍTICA Y SOCIEDAD ===
            "Política",
            "Ciencias Políticas",
            "Derecho",
            "Constitucional",
            "Penal",
            "Civil",
            "Internacional",
            "Derechos Humanos",
            
            // === INFANTIL Y JUVENIL ===
            "Infantil",
            "Juvenil",
            "Cuentos Infantiles",
            "Literatura Juvenil",
            "Educación Infantil",
            "Primeros Lectores",
            "Adolescentes",
            
            // === IDIOMAS ===
            "Idiomas",
            "Inglés",
            "Francés",
            "Alemán",
            "Italiano",
            "Portugués",
            "Japonés",
            "Chino",
            "Lingüística",
            
            // === COCINA Y HOGAR ===
            "Cocina",
            "Gastronomía",
            "Recetas",
            "Repostería",
            "Vinos",
            "Jardinería",
            "Decoración",
            "Bricolaje",
            
            // === VIAJES ===
            "Viajes",
            "Guías de Viaje",
            "Turismo",
            "Geografía",
            "Culturas",
            
            // === ESPECIALIDADES ===
            "Referencia",
            "Diccionarios",
            "Enciclopedias",
            "Atlas",
            "Manuales",
            "Académico",
            "Universitario",
            "Investigación",
            "Tesis",
            "Divulgación Científica"
        ));
        
        // Agregar categorías que existen en la BD pero no están en la lista predefinida
        for (String categoria : categorias) {
            if (!categoriasCompletas.contains(categoria)) {
                categoriasCompletas.add(categoria);
            }
        }
        
        // Ordenar alfabéticamente
        categoriasCompletas.sort(String.CASE_INSENSITIVE_ORDER);
        
        return categoriasCompletas;
    }
    
    /**
     * Verifica si un ISBN ya existe
     */
    public boolean existeIsbn(String isbn, Long excluirId) {
        String sql = "SELECT COUNT(*) FROM libros WHERE isbn = ?";
        if (excluirId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);
            if (excluirId != null) {
                stmt.setLong(2, excluirId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error verificando ISBN: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Mapea un ResultSet a un objeto Libro
     */
    private Libro mapearResultSetALibro(ResultSet rs) throws SQLException {
        Libro libro = new Libro();
        libro.setId(rs.getLong("id"));
        libro.setIsbn(rs.getString("isbn"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setAutor(rs.getString("autor"));
        libro.setEditorial(rs.getString("editorial"));
        libro.setAnioPublicacion(rs.getInt("anio_publicacion"));
        libro.setCategoria(rs.getString("categoria"));
        libro.setCantidadTotal(rs.getInt("cantidad_total"));
        libro.setCantidadDisponible(rs.getInt("cantidad_disponible"));
        libro.setDescripcion(rs.getString("descripcion"));
        libro.setActivo(rs.getBoolean("activo"));
        libro.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return libro;
    }
    
    /**
     * Crea algunos libros de ejemplo para pruebas
     */
    public void crearLibrosDePrueba() {
        try {
            // Verificar si ya hay libros
            if (obtenerTodosLosLibros().size() > 0) {
                System.out.println("ℹ️ Ya existen libros en la base de datos");
                return;
            }
            
            // Crear libros de ejemplo con categorías variadas
            Libro[] librosEjemplo = {
                new Libro("978-84-376-0494-7", "Don Quijote de la Mancha", "Miguel de Cervantes", "Planeta", 1605, "Ficción Clásica", 3),
                new Libro("978-84-663-0016-4", "Cien años de soledad", "Gabriel García Márquez", "Sudamericana", 1967, "Literatura Hispanoamericana", 2),
                new Libro("978-84-08-04314-1", "1984", "George Orwell", "Destino", 1949, "Distopía", 4),
                new Libro("978-84-259-1383-1", "El Principito", "Antoine de Saint-Exupéry", "Salamandra", 1943, "Infantil", 5),
                new Libro("978-84-344-1231-2", "Fundación", "Isaac Asimov", "Plaza & Janés", 1951, "Ciencia Ficción", 2),
                new Libro("978-84-450-7984-5", "Sapiens", "Yuval Noah Harari", "Debate", 2014, "Historia", 3),
                new Libro("978-84-233-5050-5", "El arte de la guerra", "Sun Tzu", "Gredos", 500, "Filosofía Antigua", 4),
                new Libro("978-84-08-12345-6", "Steve Jobs", "Walter Isaacson", "Random House", 2011, "Biografía", 2),
                new Libro("978-84-376-3456-8", "Breve historia del tiempo", "Stephen Hawking", "Crítica", 1988, "Física", 3),
                new Libro("978-84-663-7890-1", "El nombre de la rosa", "Umberto Eco", "Lumen", 1980, "Misterio", 2),
                new Libro("978-84-234-5678-9", "Orgullo y prejuicio", "Jane Austen", "Alba", 1813, "Romance Clásico", 3),
                new Libro("978-84-345-6789-0", "El señor de los anillos", "J.R.R. Tolkien", "Minotauro", 1954, "Fantasía Épica", 4),
                new Libro("978-84-456-7890-1", "Comer, rezar, amar", "Elizabeth Gilbert", "Aguilar", 2006, "Desarrollo Personal", 2),
                new Libro("978-84-567-8901-2", "El universo en una cáscara de nuez", "Stephen Hawking", "Planeta", 2001, "Astronomía", 3),
                new Libro("978-84-678-9012-3", "La casa de los espíritus", "Isabel Allende", "Sudamericana", 1982, "Ficción Contemporánea", 2)
            };
            
            for (Libro libro : librosEjemplo) {
                libro.setDescripcion("Libro destacado de la biblioteca - " + libro.getCategoria());
                libro.setCantidadDisponible(libro.getCantidadTotal());
                crearLibro(libro);
            }
            
            System.out.println("✅ Libros de ejemplo creados exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error creando libros de ejemplo: " + e.getMessage());
        }
    }
} 