package com.example.demo2.models;

// Importación para manejo de timestamps de base de datos
import java.sql.Timestamp;

/**
 * MODELO DE DATOS: LIBRO DEL SISTEMA BIBLIOSYSTEM
 * ===============================================
 * 
 * Esta clase representa una entidad Libro en el sistema de gestión bibliotecaria.
 * Es un POJO (Plain Old Java Object) que mapea directamente con la tabla 'libros' 
 * en la base de datos Oracle Cloud y gestiona toda la información bibliográfica.
 * 
 * PROPÓSITO:
 * - Encapsular toda la información de un libro en el catálogo
 * - Gestionar inventario con control de cantidades disponibles/prestadas
 * - Servir como intermediario entre la base de datos y la lógica de negocio
 * - Proporcionar métodos de utilidad para operaciones de préstamo
 * 
 * CARACTERÍSTICAS PRINCIPALES:
 * - Gestión de inventario: cantidad total vs disponible
 * - Control de estado: activo/inactivo para libros dados de baja
 * - Información bibliográfica completa con ISBN único
 * - Categorización para organización del catálogo
 * - Auditoría con fecha de registro
 * 
 * CAMPOS DE IDENTIFICACIÓN:
 * - ISBN: Identificador único internacional del libro
 * - Título: Nombre del libro
 * - Autor: Autor principal
 * - Editorial: Casa editorial
 * 
 * GESTIÓN DE INVENTARIO:
 * - cantidadTotal: Ejemplares totales en la biblioteca
 * - cantidadDisponible: Ejemplares disponibles para préstamo
 * - cantidadPrestada: Se calcula como (total - disponible)
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Utilizado por LibroService para operaciones CRUD
 * - Usado en controladores (LibroFormController, LibroManagementController)
 * - Relacionado con modelo Prestamo (un libro puede tener múltiples préstamos)
 * - Referenciado en sistema de búsqueda y catálogo
 * - Integrado con sistema de estadísticas
 */
public class Libro {
    
    // === CAMPOS DE IDENTIFICACIÓN ===
    private Long id;                    // ID único en base de datos (auto-generado)
    private String isbn;                // ISBN único internacional del libro
    private String titulo;              // Título del libro
    private String autor;               // Autor principal
    private String editorial;           // Casa editorial o publisher
    private int anioPublicacion;        // Año de publicación
    
    // === CLASIFICACIÓN Y ORGANIZACIÓN ===
    private String categoria;           // Categoría temática (Ficción, Ciencia, etc.)
    private String descripcion;         // Descripción o sinopsis del libro
    
    // === GESTIÓN DE INVENTARIO ===
    private int cantidadTotal;          // Total de ejemplares en la biblioteca
    private int cantidadDisponible;     // Ejemplares disponibles para préstamo
    
    // === CAMPOS DE SISTEMA ===
    private boolean activo;             // Estado: true=activo, false=dado de baja
    private Timestamp fechaRegistro;    // Cuándo se registró el libro en el sistema
    
    // Constructor vacío
    public Libro() {
        this.activo = true;
        this.cantidadDisponible = 0;
        this.cantidadTotal = 0;
    }
    
    // Constructor con parámetros básicos
    public Libro(String isbn, String titulo, String autor, String editorial, int anioPublicacion, String categoria, int cantidadTotal) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.categoria = categoria;
        this.cantidadTotal = cantidadTotal;
        this.cantidadDisponible = cantidadTotal;
        this.activo = true;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getAutor() {
        return autor;
    }
    
    public void setAutor(String autor) {
        this.autor = autor;
    }
    
    public String getEditorial() {
        return editorial;
    }
    
    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }
    
    public int getAnioPublicacion() {
        return anioPublicacion;
    }
    
    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public int getCantidadTotal() {
        return cantidadTotal;
    }
    
    public void setCantidadTotal(int cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }
    
    public int getCantidadDisponible() {
        return cantidadDisponible;
    }
    
    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    // Métodos de utilidad
    public boolean isDisponible() {
        return cantidadDisponible > 0;
    }
    
    public int getCantidadPrestada() {
        return cantidadTotal - cantidadDisponible;
    }
    
    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", isbn='" + isbn + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", disponible=" + cantidadDisponible + "/" + cantidadTotal +
                '}';
    }
} 