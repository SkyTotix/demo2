package com.example.demo2.models.enums;

/**
 * ENUMERACIÓN DE TIPOS DE USUARIO - SISTEMA DE ROLES Y PERMISOS
 * =============================================================
 * 
 * Esta enumeración define la jerarquía de roles en BiblioSystem con un sistema
 * de permisos granular. Implementa control de acceso basado en roles (RBAC)
 * con jerarquía descendente de privilegios.
 * 
 * JERARQUÍA DE ROLES (del mayor al menor privilegio):
 * 
 * 1. SUPERADMIN (Nivel 1) - Control Total del Sistema:
 *    - Puede crear administradores y bibliotecarios
 *    - Acceso completo a configuración del sistema
 *    - Gestión de backups y mantenimiento

 *    - Control total sobre todos los módulos
 * 
 * 2. ADMIN (Nivel 2) - Gestión Operativa de la Biblioteca:
 *    - Puede crear solo bibliotecarios (no otros admins)
 *    - Gestión completa de usuarios, libros y préstamos

 *    - Gestión de multas y categorías
 *    - Operaciones diarias de administración
 * 
 * 3. BIBLIOTECARIO (Nivel 3) - Operaciones de Primera Línea:
 *    - No puede crear otros usuarios
 *    - Gestión de préstamos y devoluciones
 *    - Consulta de catálogo y disponibilidad
 *    - Búsqueda de libros y historial
 *    - Operaciones básicas de atención al público
 * 
 * SISTEMA DE PERMISOS:
 * - Cada rol tiene permisos específicos definidos como array de strings
 * - Los permisos son verificados por AuthService en toda la aplicación
 * - La jerarquía permite que roles superiores hereden capacidades básicas
 * - Sistema extensible para agregar nuevos permisos sin cambiar código
 * 
 * PRINCIPIOS DE SEGURIDAD:
 * - Principio de menor privilegio: cada rol solo tiene lo que necesita
 * - No escalación horizontal: bibliotecarios no pueden crear usuarios
 * - Jerarquía clara: SuperAdmin > Admin > Bibliotecario
 * - Permisos granulares para control preciso
 * 
 * RELACIÓN CON OTROS COMPONENTES:
 * - Utilizado por AuthService para control de acceso
 * - Referenciado en MainController para personalizar menús
 * - Base para sistema de notificaciones por rol
 * - Integrado con UsuarioService para validaciones
 * - Usado en todos los controladores para verificar permisos
 */
public enum TipoUsuario {
    
    /**
     * SUPERADMIN - Control total del sistema
     * Nivel de acceso más alto con todos los permisos del sistema
     */
    SUPERADMIN("Super Administrador", 1, new String[]{
        "GESTIONAR_SISTEMA",           // Control total del sistema
        "CREAR_ADMIN",                 // Crear administradores
        "CREAR_BIBLIOTECARIO",         // Crear bibliotecarios
        "GESTIONAR_USUARIOS",          // CRUD completo de usuarios
        "GESTIONAR_LIBROS",            // CRUD completo de libros
        "GESTIONAR_PRESTAMOS",         // Gestión completa de préstamos
        "GESTIONAR_CATEGORIAS",        // Gestión de categorías de libros
        
        "CONFIGURAR_SISTEMA",          // Configuración global
        "BACKUP_SISTEMA"               // Backups y mantenimiento
    }),
    
    /**
     * ADMIN - Gestión operativa de la biblioteca
     * Nivel intermedio con permisos administrativos limitados
     */
    ADMIN("Administrador", 2, new String[]{
        "CREAR_BIBLIOTECARIO",         // Solo puede crear bibliotecarios
        "GESTIONAR_USUARIOS",          // CRUD de usuarios (no admins)
        "GESTIONAR_LIBROS",            // CRUD completo de libros
        "GESTIONAR_PRESTAMOS",         // Gestión completa de préstamos
        "GESTIONAR_CATEGORIAS",        // Gestión de categorías
        
        "GESTIONAR_MULTAS"             // Gestión de multas y penalizaciones
    }),
    
    /**
     * BIBLIOTECARIO - Operaciones diarias de primera línea
     * Nivel básico con permisos operativos esenciales
     */
    BIBLIOTECARIO("Bibliotecario", 3, new String[]{
        "GESTIONAR_PRESTAMOS",         // Operaciones de préstamos
        "REGISTRAR_PRESTAMO",          // Registrar nuevos préstamos
        "REGISTRAR_DEVOLUCION",        // Procesar devoluciones
        "CONSULTAR_CATALOGO",          // Consultar catálogo de libros
        "VER_DISPONIBILIDAD",          // Ver disponibilidad de libros
        "GESTIONAR_MULTAS",            // Gestión básica de multas
        "BUSCAR_LIBROS",               // Búsqueda en catálogo
        "VER_HISTORIAL_PRESTAMOS"      // Consultar historial
    });
    
    // === CAMPOS INMUTABLES DEL ENUM ===
    private final String descripcion;          // Nombre descriptivo del rol
    private final int nivelAcceso;             // Nivel jerárquico (1=mayor, 3=menor)
    private final String[] permisos;           // Array de permisos específicos
    
    TipoUsuario(String descripcion, int nivelAcceso, String[] permisos) {
        this.descripcion = descripcion;
        this.nivelAcceso = nivelAcceso;
        this.permisos = permisos;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public int getNivelAcceso() {
        return nivelAcceso;
    }
    
    public String[] getPermisos() {
        return permisos;
    }
    
    /**
     * Verifica si este tipo de usuario puede crear usuarios del tipo especificado
     */
    public boolean puedeCrear(TipoUsuario tipo) {
        switch (this) {
            case SUPERADMIN:
                return tipo == ADMIN || tipo == BIBLIOTECARIO;
            case ADMIN:
                return tipo == BIBLIOTECARIO;
            default:
                return false;
        }
    }
    
    /**
     * Verifica si este usuario tiene un permiso específico
     */
    public boolean tienePermiso(String permiso) {
        for (String p : permisos) {
            if (p.equals(permiso)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica si este tipo de usuario tiene los permisos del tipo especificado
     */
    public boolean tienePermisoDe(TipoUsuario tipo) {
        return this.nivelAcceso <= tipo.nivelAcceso;
    }
} 