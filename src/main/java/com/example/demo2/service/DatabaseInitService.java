package com.example.demo2.service;

import com.example.demo2.database.DatabaseManager;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Servicio para inicializar la base de datos
 */
public class DatabaseInitService {
    
    /**
     * Ejecuta el script SQL para crear las tablas
     */
    public static void inicializarBaseDatos() {
        System.out.println("🔧 Iniciando proceso de inicialización de base de datos...");
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            System.out.println("✅ Conexión obtenida para inicialización");
            
            // Verificar que la conexión es válida
            if (!conn.isValid(5)) {
                throw new SQLException("La conexión no es válida");
            }
            
            conn.setAutoCommit(false);
            System.out.println("🔧 AutoCommit deshabilitado");
            
            try (Statement stmt = conn.createStatement()) {
                
                // Verificar y crear tabla usuarios
                System.out.println("🔍 Verificando tabla USUARIOS...");
                if (!existeTabla(conn, "USUARIOS")) {
                    System.out.println("📝 Creando tabla USUARIOS...");
                    crearTablaUsuarios(stmt);
                } else {
                    System.out.println("✅ Tabla USUARIOS ya existe");
                }
                
                // Verificar y crear tabla lectores
                System.out.println("🔍 Verificando tabla LECTORES...");
                if (!existeTabla(conn, "LECTORES")) {
                    System.out.println("📝 Creando tabla LECTORES...");
                    crearTablaLectores(stmt);
                } else {
                    System.out.println("✅ Tabla LECTORES ya existe");
                }
                
                // Verificar y crear tabla libros
                System.out.println("🔍 Verificando tabla LIBROS...");
                if (!existeTabla(conn, "LIBROS")) {
                    System.out.println("📝 Creando tabla LIBROS...");
                    crearTablaLibros(stmt);
                } else {
                    System.out.println("✅ Tabla LIBROS ya existe");
                }
                
                // Verificar y crear tabla prestamos
                System.out.println("🔍 Verificando tabla PRESTAMOS...");
                if (!existeTabla(conn, "PRESTAMOS")) {
                    System.out.println("📝 Creando tabla PRESTAMOS...");
                    crearTablaPrestamos(stmt);
                } else {
                    System.out.println("✅ Tabla PRESTAMOS ya existe");
                }
                
                conn.commit();
                System.out.println("✅ Todas las operaciones confirmadas exitosamente");
                System.out.println("🎉 Base de datos inicializada correctamente");
                
            } catch (Exception e) {
                System.err.println("❌ Error durante la creación de tablas, haciendo rollback...");
                try {
                    conn.rollback();
                    System.out.println("🔄 Rollback completado");
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Error durante rollback: " + rollbackEx.getMessage());
                }
                throw e;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error crítico inicializando la base de datos: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error inicializando la base de datos", e);
        }
    }
    
    /**
     * Verifica si una tabla existe en la base de datos
     */
    private static boolean existeTabla(Connection conn, String nombreTabla) {
        String query = "SELECT COUNT(*) FROM user_tables WHERE table_name = ?";
        try (var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nombreTabla.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (Exception e) {
            // Log silencioso para no afectar el rendimiento del login
            return false;
        }
        return false;
    }
    
    /**
     * Verifica si una tabla existe en la base de datos (versión con logs para debug)
     */
    private static boolean existeTablaConLogs(Connection conn, String nombreTabla) {
        String query = "SELECT COUNT(*) FROM user_tables WHERE table_name = ?";
        try (var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nombreTabla.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("🔍 Tabla " + nombreTabla + " - Encontradas: " + count);
                return count > 0;
            }
        } catch (Exception e) {
            System.err.println("❌ Error verificando tabla " + nombreTabla + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Crea la tabla usuarios (versión optimizada sin logs excesivos)
     */
    private static void crearTablaUsuarios(Statement stmt) throws Exception {
        String createUsuarios = """
            CREATE TABLE usuarios (
                id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                nombre VARCHAR2(100) NOT NULL,
                apellido VARCHAR2(100) NOT NULL,
                email VARCHAR2(150) UNIQUE NOT NULL,
                username VARCHAR2(50) UNIQUE NOT NULL,
                password_hash VARCHAR2(255) NOT NULL,
                tipo_usuario VARCHAR2(20) NOT NULL CHECK (tipo_usuario IN ('SUPERADMIN', 'ADMIN', 'BIBLIOTECARIO')),
                activo NUMBER(1) DEFAULT 1 CHECK (activo IN (0, 1)),
                fecha_creacion TIMESTAMP DEFAULT SYSTIMESTAMP,
                ultimo_acceso TIMESTAMP
            )""";
        
        stmt.execute(createUsuarios);
        
        // Crear índices sin logs excesivos
        try {
            stmt.execute("CREATE INDEX idx_usuarios_username ON usuarios(username)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_usuarios_email ON usuarios(email)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
    }
    
    /**
     * Crea la tabla lectores (versión optimizada sin logs excesivos)
     */
    private static void crearTablaLectores(Statement stmt) throws Exception {
        
        String createLectores = """
            CREATE TABLE lectores (
                id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                codigo_lector VARCHAR2(20) UNIQUE NOT NULL,
                nombre VARCHAR2(100) NOT NULL,
                apellido VARCHAR2(100) NOT NULL,
                tipo_documento VARCHAR2(20) NOT NULL CHECK (tipo_documento IN ('DNI', 'PASAPORTE', 'CARNET_EXTRANJERIA')),
                numero_documento VARCHAR2(20) UNIQUE NOT NULL,
                email VARCHAR2(150) UNIQUE NOT NULL,
                telefono VARCHAR2(20) NOT NULL,
                direccion VARCHAR2(300) NOT NULL,
                fecha_nacimiento DATE NOT NULL,
                fecha_registro TIMESTAMP DEFAULT SYSTIMESTAMP,
                fecha_vencimiento DATE NOT NULL,
                estado VARCHAR2(20) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'SUSPENDIDO', 'VENCIDO', 'INACTIVO')),
                foto_url VARCHAR2(500),
                observaciones VARCHAR2(500),
                creado_por NUMBER NOT NULL,
                actualizado_por NUMBER,
                fecha_actualizacion TIMESTAMP,
                CONSTRAINT fk_lector_creado_por FOREIGN KEY (creado_por) REFERENCES usuarios(id),
                CONSTRAINT fk_lector_actualizado_por FOREIGN KEY (actualizado_por) REFERENCES usuarios(id)
            )""";
        
        stmt.execute(createLectores);
        
        // Crear índices sin logs excesivos
        try {
            stmt.execute("CREATE INDEX idx_lectores_codigo ON lectores(codigo_lector)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_lectores_documento ON lectores(numero_documento)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_lectores_email ON lectores(email)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_lectores_estado ON lectores(estado)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
    }
    
    /**
     * Crea la tabla libros (versión optimizada sin logs excesivos)
     */
    private static void crearTablaLibros(Statement stmt) throws Exception {
        String createLibros = """
            CREATE TABLE libros (
                id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                isbn VARCHAR2(20) UNIQUE NOT NULL,
                titulo VARCHAR2(255) NOT NULL,
                autor VARCHAR2(255) NOT NULL,
                editorial VARCHAR2(150),
                anio_publicacion NUMBER(4),
                categoria VARCHAR2(100),
                cantidad_total NUMBER DEFAULT 1 CHECK (cantidad_total >= 0),
                cantidad_disponible NUMBER DEFAULT 1 CHECK (cantidad_disponible >= 0),
                descripcion CLOB,
                activo NUMBER(1) DEFAULT 1 CHECK (activo IN (0, 1)),
                fecha_registro TIMESTAMP DEFAULT SYSTIMESTAMP,
                CONSTRAINT chk_cantidad CHECK (cantidad_disponible <= cantidad_total)
            )""";
        
        stmt.execute(createLibros);
        
        // Crear índices sin logs excesivos
        try {
            stmt.execute("CREATE INDEX idx_libros_isbn ON libros(isbn)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_libros_titulo ON libros(titulo)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_libros_autor ON libros(autor)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_libros_categoria ON libros(categoria)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
    }
    
    /**
     * Crea la tabla prestamos (versión optimizada sin logs excesivos)
     */
    private static void crearTablaPrestamos(Statement stmt) throws Exception {
        String createPrestamos = """
            CREATE TABLE prestamos (
                id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                codigo_prestamo VARCHAR2(20) UNIQUE NOT NULL,
                libro_id NUMBER NOT NULL,
                lector_id NUMBER NOT NULL,
                bibliotecario_prestamo_id NUMBER NOT NULL,
                bibliotecario_devolucion_id NUMBER,
                fecha_prestamo TIMESTAMP DEFAULT SYSTIMESTAMP,
                fecha_devolucion_esperada DATE NOT NULL,
                fecha_devolucion_real TIMESTAMP,
                estado VARCHAR2(20) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'DEVUELTO', 'VENCIDO', 'PERDIDO')),
                condicion_prestamo VARCHAR2(100),
                condicion_devolucion VARCHAR2(100),
                observaciones_prestamo VARCHAR2(500),
                observaciones_devolucion VARCHAR2(500),
                multa NUMBER(10,2) DEFAULT 0,
                multa_pagada NUMBER(1) DEFAULT 0 CHECK (multa_pagada IN (0, 1)),
                CONSTRAINT fk_prestamo_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
                CONSTRAINT fk_prestamo_lector FOREIGN KEY (lector_id) REFERENCES lectores(id),
                CONSTRAINT fk_prestamo_bibliotecario_p FOREIGN KEY (bibliotecario_prestamo_id) REFERENCES usuarios(id),
                CONSTRAINT fk_prestamo_bibliotecario_d FOREIGN KEY (bibliotecario_devolucion_id) REFERENCES usuarios(id)
            )""";
        
        stmt.execute(createPrestamos);
        
        // Crear índices sin logs excesivos
        try {
            stmt.execute("CREATE INDEX idx_prestamos_codigo ON prestamos(codigo_prestamo)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_prestamos_libro ON prestamos(libro_id)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_prestamos_lector ON prestamos(lector_id)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_prestamos_estado ON prestamos(estado)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
        
        try {
            stmt.execute("CREATE INDEX idx_prestamos_fecha_prestamo ON prestamos(fecha_prestamo)");
        } catch (Exception e) {
            // Índice ya existe, continuar
        }
    }
    
    /**
     * Verifica si las tablas principales existen (con logs para debug)
     */
    public static boolean verificarTablas() {
        System.out.println("🔍 Verificando existencia de todas las tablas...");
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            boolean usuariosExiste = existeTablaConLogs(conn, "USUARIOS");
            boolean lectoresExiste = existeTablaConLogs(conn, "LECTORES");
            boolean librosExiste = existeTablaConLogs(conn, "LIBROS");
            boolean prestamosExiste = existeTablaConLogs(conn, "PRESTAMOS");
            
            boolean todasExisten = usuariosExiste && lectoresExiste && librosExiste && prestamosExiste;
            
            System.out.println("📊 Estado de las tablas:");
            System.out.println("   - USUARIOS: " + (usuariosExiste ? "✅" : "❌"));
            System.out.println("   - LECTORES: " + (lectoresExiste ? "✅" : "❌"));
            System.out.println("   - LIBROS: " + (librosExiste ? "✅" : "❌"));
            System.out.println("   - PRESTAMOS: " + (prestamosExiste ? "✅" : "❌"));
            System.out.println("   - TODAS: " + (todasExisten ? "✅" : "❌"));
            
            return todasExisten;
        } catch (Exception e) {
            System.err.println("❌ Error verificando tablas: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Fuerza la recreación de todas las tablas
     */
    public static void recrearTablas() {
        System.out.println("🔄 Iniciando recreación completa de tablas...");
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            conn.setAutoCommit(false);
            
            // Eliminar tablas si existen (en orden inverso por las FK)
            System.out.println("🗑️ Eliminando tablas existentes...");
            try { 
                stmt.execute("DROP TABLE prestamos CASCADE CONSTRAINTS"); 
                System.out.println("🗑️ Tabla prestamos eliminada");
            } catch (Exception e) { 
                System.out.println("ℹ️ Tabla prestamos no existía");
            }
            
            try { 
                stmt.execute("DROP TABLE lectores CASCADE CONSTRAINTS"); 
                System.out.println("🗑️ Tabla lectores eliminada");
            } catch (Exception e) { 
                System.out.println("ℹ️ Tabla lectores no existía");
            }
            
            try { 
                stmt.execute("DROP TABLE libros CASCADE CONSTRAINTS"); 
                System.out.println("🗑️ Tabla libros eliminada");
            } catch (Exception e) { 
                System.out.println("ℹ️ Tabla libros no existía");
            }
            
            try { 
                stmt.execute("DROP TABLE usuarios CASCADE CONSTRAINTS"); 
                System.out.println("🗑️ Tabla usuarios eliminada");
            } catch (Exception e) { 
                System.out.println("ℹ️ Tabla usuarios no existía");
            }
            
            conn.commit();
            System.out.println("✅ Eliminación completada");
            
            // Crear tablas nuevamente
            inicializarBaseDatos();
            
        } catch (Exception e) {
            System.err.println("❌ Error recreando tablas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error recreando tablas", e);
        }
    }
    
    /**
     * Verificación rápida de tablas sin logs verbosos (optimizado para login)
     */
    public static boolean verificarTablasRapido() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return existeTabla(conn, "USUARIOS") && 
                   existeTabla(conn, "LECTORES") && 
                   existeTabla(conn, "LIBROS") && 
                   existeTabla(conn, "PRESTAMOS");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Inicialización rápida de base de datos sin logs excesivos (optimizado para login)
     */
    public static void inicializarBaseDatosRapido() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            
            try (Statement stmt = conn.createStatement()) {
                // Crear solo las tablas que no existen, sin logs verbosos
                if (!existeTabla(conn, "USUARIOS")) {
                    crearTablaUsuarios(stmt);
                }
                
                if (!existeTabla(conn, "LECTORES")) {
                    crearTablaLectores(stmt);
                }
                
                if (!existeTabla(conn, "LIBROS")) {
                    crearTablaLibros(stmt);
                }
                
                if (!existeTabla(conn, "PRESTAMOS")) {
                    crearTablaPrestamos(stmt);
                }
                
                conn.commit();
                
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Ignorar errores de rollback
                }
                throw e;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando la base de datos", e);
        }
    }
    
    /**
     * Método de utilidad para debug - lista todas las tablas del usuario
     */
    public static void listarTablas() {
        System.out.println("📋 Listando todas las tablas del usuario...");
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT table_name FROM user_tables ORDER BY table_name");
            
            int count = 0;
            while (rs.next()) {
                System.out.println("   📄 " + rs.getString("table_name"));
                count++;
            }
            
            System.out.println("📊 Total de tablas encontradas: " + count);
            
        } catch (Exception e) {
            System.err.println("❌ Error listando tablas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Actualiza la estructura de la tabla usuarios agregando campos faltantes
     */
    public static void actualizarEstructuraUsuarios() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Verificar si los nuevos campos ya existen
            boolean telefonoExists = verificarColumnaExiste(conn, "USUARIOS", "TELEFONO");
            boolean fechaNacimientoExists = verificarColumnaExiste(conn, "USUARIOS", "FECHA_NACIMIENTO");
            boolean direccionExists = verificarColumnaExiste(conn, "USUARIOS", "DIRECCION");
            
            // Agregar campos faltantes
            if (!telefonoExists) {
                ejecutarSQL(conn, "ALTER TABLE usuarios ADD telefono VARCHAR2(20)");
                System.out.println("✅ Campo 'telefono' agregado a la tabla usuarios");
            }
            
            if (!fechaNacimientoExists) {
                ejecutarSQL(conn, "ALTER TABLE usuarios ADD fecha_nacimiento DATE");
                System.out.println("✅ Campo 'fecha_nacimiento' agregado a la tabla usuarios");
            }
            
            if (!direccionExists) {
                ejecutarSQL(conn, "ALTER TABLE usuarios ADD direccion VARCHAR2(300)");
                System.out.println("✅ Campo 'direccion' agregado a la tabla usuarios");
            }
            
            if (telefonoExists && fechaNacimientoExists && direccionExists) {
                System.out.println("ℹ️ La tabla usuarios ya tiene todos los campos necesarios");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error actualizando estructura de usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza la estructura de la tabla prestamos para corregir problemas de columnas
     */
    public static void actualizarEstructuraPrestamos() {
        System.out.println("🔧 Verificando y actualizando estructura de la tabla prestamos...");
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Verificar si la columna lector_id existe
            if (!verificarColumnaExiste(conn, "PRESTAMOS", "LECTOR_ID")) {
                System.out.println("⚠️ La columna LECTOR_ID no existe en PRESTAMOS");
                
                // Verificar si existe usuario_id (estructura antigua)
                if (verificarColumnaExiste(conn, "PRESTAMOS", "USUARIO_ID")) {
                    System.out.println("🔄 Renombrando USUARIO_ID a LECTOR_ID...");
                    ejecutarSQL(conn, "ALTER TABLE prestamos RENAME COLUMN usuario_id TO lector_id");
                    System.out.println("✅ Columna renombrada exitosamente");
                } else {
                    System.out.println("⚠️ No se encontró USUARIO_ID, la tabla puede tener una estructura incorrecta");
                    System.out.println("🔄 Recreando tabla PRESTAMOS con estructura correcta...");
                    recrearTablaPrestamos(conn);
                }
            } else {
                System.out.println("✅ Columna LECTOR_ID ya existe en PRESTAMOS");
            }
            
            // Verificar otras columnas importantes
            String[] columnasRequeridas = {
                "CODIGO_PRESTAMO", "LIBRO_ID", "BIBLIOTECARIO_PRESTAMO_ID",
                "FECHA_DEVOLUCION_ESPERADA", "ESTADO", "CONDICION_PRESTAMO"
            };
            
            boolean todasExisten = true;
            for (String columna : columnasRequeridas) {
                if (!verificarColumnaExiste(conn, "PRESTAMOS", columna)) {
                    System.out.println("⚠️ Columna faltante: " + columna);
                    todasExisten = false;
                }
            }
            
            if (!todasExisten) {
                System.out.println("🔄 Recreando tabla PRESTAMOS con estructura completa...");
                recrearTablaPrestamos(conn);
            } else {
                System.out.println("✅ Estructura de PRESTAMOS verificada correctamente");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error actualizando estructura de prestamos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Recrea la tabla prestamos con la estructura correcta
     */
    private static void recrearTablaPrestamos(Connection conn) throws SQLException {
        System.out.println("🔄 Recreando tabla PRESTAMOS...");
        
        try (Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            
            // Eliminar tabla existente
            try {
                stmt.execute("DROP TABLE prestamos CASCADE CONSTRAINTS");
                System.out.println("🗑️ Tabla prestamos eliminada");
            } catch (SQLException e) {
                System.out.println("ℹ️ Tabla prestamos no existía previamente");
            }
            
            // Crear tabla con estructura correcta
            try {
                crearTablaPrestamos(stmt);
            } catch (Exception e) {
                throw new SQLException("Error creando tabla prestamos", e);
            }
            
            conn.commit();
            System.out.println("✅ Tabla PRESTAMOS recreada exitosamente");
            
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.out.println("🔄 Rollback realizado");
            } catch (SQLException rollbackEx) {
                System.err.println("❌ Error en rollback: " + rollbackEx.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Verifica si una columna existe en una tabla
     */
    private static boolean verificarColumnaExiste(Connection conn, String tabla, String columna) {
        try {
            String sql = "SELECT COUNT(*) FROM user_tab_columns WHERE table_name = ? AND column_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, tabla.toUpperCase());
                stmt.setString(2, columna.toUpperCase());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error verificando columna " + columna + ": " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Ejecuta una sentencia SQL
     */
    private static void ejecutarSQL(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}