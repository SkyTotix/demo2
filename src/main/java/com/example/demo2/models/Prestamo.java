package com.example.demo2.models;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Modelo que representa un préstamo de libro en el sistema BiblioSystem
 */
public class Prestamo {
    private Long id;
    private String codigoPrestamo;
    private Long libroId;
    private Long lectorId;
    private Long bibliotecarioPrestamoId;
    private Long bibliotecarioDevolucionId;
    private Timestamp fechaPrestamo;
    private LocalDate fechaDevolucionEsperada;
    private Timestamp fechaDevolucionReal;
    private String estado; // ACTIVO, DEVUELTO, VENCIDO, PERDIDO
    private String condicionPrestamo;
    private String condicionDevolucion;
    private String observacionesPrestamo;
    private String observacionesDevolucion;
    private double multa;
    private boolean multaPagada;
    
    // Referencias a objetos (se cargarán según necesidad)
    private Libro libro;
    private Lector lector;
    private Usuario bibliotecarioPrestamo;
    private Usuario bibliotecarioDevolucion;
    
    // Campos adicionales para mostrar en tabla (se cargan desde el servicio)
    private String libroTitulo;
    private String libroIsbn;
    private String lectorCodigo;
    private String lectorNombre;
    private String bibliotecarioPrestamoNombre;
    private String bibliotecarioDevolucionNombre;
    
    // Constructor vacío
    public Prestamo() {
        this.estado = "ACTIVO";
        this.multa = 0.0;
        this.multaPagada = false;
    }
    
    // Constructor con parámetros básicos
    public Prestamo(String codigoPrestamo, Long libroId, Long lectorId, Long bibliotecarioPrestamoId, LocalDate fechaDevolucionEsperada) {
        this.codigoPrestamo = codigoPrestamo;
        this.libroId = libroId;
        this.lectorId = lectorId;
        this.bibliotecarioPrestamoId = bibliotecarioPrestamoId;
        this.fechaPrestamo = new Timestamp(System.currentTimeMillis());
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
        this.estado = "ACTIVO";
        this.multa = 0.0;
        this.multaPagada = false;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigoPrestamo() {
        return codigoPrestamo;
    }
    
    public void setCodigoPrestamo(String codigoPrestamo) {
        this.codigoPrestamo = codigoPrestamo;
    }
    
    public Long getLibroId() {
        return libroId;
    }
    
    public void setLibroId(Long libroId) {
        this.libroId = libroId;
    }
    
    public Long getLectorId() {
        return lectorId;
    }
    
    public void setLectorId(Long lectorId) {
        this.lectorId = lectorId;
    }
    
    public Long getBibliotecarioPrestamoId() {
        return bibliotecarioPrestamoId;
    }
    
    public void setBibliotecarioPrestamoId(Long bibliotecarioPrestamoId) {
        this.bibliotecarioPrestamoId = bibliotecarioPrestamoId;
    }
    
    public Long getBibliotecarioDevolucionId() {
        return bibliotecarioDevolucionId;
    }
    
    public void setBibliotecarioDevolucionId(Long bibliotecarioDevolucionId) {
        this.bibliotecarioDevolucionId = bibliotecarioDevolucionId;
    }
    
    public Timestamp getFechaPrestamo() {
        return fechaPrestamo;
    }
    
    public void setFechaPrestamo(Timestamp fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }
    
    public LocalDate getFechaDevolucionEsperada() {
        return fechaDevolucionEsperada;
    }
    
    public void setFechaDevolucionEsperada(LocalDate fechaDevolucionEsperada) {
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
    }
    
    public Timestamp getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }
    
    public void setFechaDevolucionReal(Timestamp fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getCondicionPrestamo() {
        return condicionPrestamo;
    }
    
    public void setCondicionPrestamo(String condicionPrestamo) {
        this.condicionPrestamo = condicionPrestamo;
    }
    
    public String getCondicionDevolucion() {
        return condicionDevolucion;
    }
    
    public void setCondicionDevolucion(String condicionDevolucion) {
        this.condicionDevolucion = condicionDevolucion;
    }
    
    public String getObservacionesPrestamo() {
        return observacionesPrestamo;
    }
    
    public void setObservacionesPrestamo(String observacionesPrestamo) {
        this.observacionesPrestamo = observacionesPrestamo;
    }
    
    public String getObservacionesDevolucion() {
        return observacionesDevolucion;
    }
    
    public void setObservacionesDevolucion(String observacionesDevolucion) {
        this.observacionesDevolucion = observacionesDevolucion;
    }
    
    public double getMulta() {
        return multa;
    }
    
    public void setMulta(double multa) {
        this.multa = multa;
    }
    
    public boolean isMultaPagada() {
        return multaPagada;
    }
    
    public void setMultaPagada(boolean multaPagada) {
        this.multaPagada = multaPagada;
    }
    
    // Getters y setters para objetos relacionados
    public Libro getLibro() {
        return libro;
    }
    
    public void setLibro(Libro libro) {
        this.libro = libro;
    }
    
    public Lector getLector() {
        return lector;
    }
    
    public void setLector(Lector lector) {
        this.lector = lector;
    }
    
    public Usuario getBibliotecarioPrestamo() {
        return bibliotecarioPrestamo;
    }
    
    public void setBibliotecarioPrestamo(Usuario bibliotecarioPrestamo) {
        this.bibliotecarioPrestamo = bibliotecarioPrestamo;
    }
    
    public Usuario getBibliotecarioDevolucion() {
        return bibliotecarioDevolucion;
    }
    
    public void setBibliotecarioDevolucion(Usuario bibliotecarioDevolucion) {
        this.bibliotecarioDevolucion = bibliotecarioDevolucion;
    }
    
    // Métodos de utilidad
    public boolean isActivo() {
        return "ACTIVO".equals(estado);
    }
    
    public boolean isDevuelto() {
        return "DEVUELTO".equals(estado);
    }
    
    public boolean isPerdido() {
        return "PERDIDO".equals(estado);
    }
    
    public boolean isVencido() {
        if (!"ACTIVO".equals(estado)) {
            return false;
        }
        return LocalDate.now().isAfter(fechaDevolucionEsperada);
    }
    
    public long getDiasRetraso() {
        if (!"ACTIVO".equals(estado) || !isVencido()) {
            return 0;
        }
        return LocalDate.now().toEpochDay() - fechaDevolucionEsperada.toEpochDay();
    }
    
    public double calcularMulta(double multaPorDia) {
        if (!isVencido()) return 0.0;
        return getDiasRetraso() * multaPorDia;
    }
    
    // Getters y setters para campos adicionales
    public String getLibroTitulo() {
        return libroTitulo;
    }
    
    public void setLibroTitulo(String libroTitulo) {
        this.libroTitulo = libroTitulo;
    }
    
    public String getLibroIsbn() {
        return libroIsbn;
    }
    
    public void setLibroIsbn(String libroIsbn) {
        this.libroIsbn = libroIsbn;
    }
    
    public String getLectorCodigo() {
        return lectorCodigo;
    }
    
    public void setLectorCodigo(String lectorCodigo) {
        this.lectorCodigo = lectorCodigo;
    }
    
    public String getLectorNombre() {
        return lectorNombre;
    }
    
    public void setLectorNombre(String lectorNombre) {
        this.lectorNombre = lectorNombre;
    }
    
    public String getBibliotecarioPrestamoNombre() {
        return bibliotecarioPrestamoNombre;
    }
    
    public void setBibliotecarioPrestamoNombre(String bibliotecarioPrestamoNombre) {
        this.bibliotecarioPrestamoNombre = bibliotecarioPrestamoNombre;
    }
    
    public String getBibliotecarioDevolucionNombre() {
        return bibliotecarioDevolucionNombre;
    }
    
    public void setBibliotecarioDevolucionNombre(String bibliotecarioDevolucionNombre) {
        this.bibliotecarioDevolucionNombre = bibliotecarioDevolucionNombre;
    }
    
    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", codigoPrestamo='" + codigoPrestamo + '\'' +
                ", libroId=" + libroId +
                ", lectorId=" + lectorId +
                ", estado='" + estado + '\'' +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaDevolucion=" + fechaDevolucionEsperada +
                '}';
    }
} 