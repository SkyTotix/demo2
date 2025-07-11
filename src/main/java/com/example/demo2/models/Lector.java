package com.example.demo2.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo que representa un Lector (usuario de la biblioteca)
 */
public class Lector {
    private int id;
    private String codigoLector;
    private String nombre;
    private String apellido;
    private String tipoDocumento;
    private String numeroDocumento;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private LocalDateTime fechaRegistro;
    private LocalDate fechaVencimiento;
    private String estado;
    private String fotoUrl;
    private String observaciones;
    private int creadoPor;
    private Integer actualizadoPor;
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    public Lector() {}
    
    public Lector(String codigoLector, String nombre, String apellido, String tipoDocumento, 
                  String numeroDocumento, String email, String telefono, String direccion, 
                  LocalDate fechaNacimiento, LocalDate fechaVencimiento, int creadoPor) {
        this.codigoLector = codigoLector;
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = "ACTIVO";
        this.creadoPor = creadoPor;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getCodigoLector() {
        return codigoLector;
    }
    
    public void setCodigoLector(String codigoLector) {
        this.codigoLector = codigoLector;
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
    
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    
    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    
    public String getNumeroDocumento() {
        return numeroDocumento;
    }
    
    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }
    
    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public boolean isActivo() {
        return "ACTIVO".equals(estado);
    }
    
    public String getFotoUrl() {
        return fotoUrl;
    }
    
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public int getCreadoPor() {
        return creadoPor;
    }
    
    public void setCreadoPor(int creadoPor) {
        this.creadoPor = creadoPor;
    }
    
    public Integer getActualizadoPor() {
        return actualizadoPor;
    }
    
    public void setActualizadoPor(Integer actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    // Método para verificar si la membresía está vencida
    public boolean isVencido() {
        return fechaVencimiento != null && LocalDate.now().isAfter(fechaVencimiento);
    }
    
    // Método para calcular edad
    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return java.time.Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
} 