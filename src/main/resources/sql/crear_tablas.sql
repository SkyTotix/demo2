-- Script para crear las tablas del sistema BiblioSystem
-- Base de datos Oracle

-- Eliminar tablas si existen (en orden inverso por las foreign keys)
DROP TABLE prestamos CASCADE CONSTRAINTS;
DROP TABLE lectores CASCADE CONSTRAINTS;
DROP TABLE libros CASCADE CONSTRAINTS;
DROP TABLE usuarios CASCADE CONSTRAINTS;

-- Crear tabla de usuarios
CREATE TABLE usuarios (
    id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    nombre VARCHAR2(100) NOT NULL,
    apellido VARCHAR2(100) NOT NULL,
    email VARCHAR2(150) UNIQUE NOT NULL,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password_hash VARCHAR2(255) NOT NULL,
    telefono VARCHAR2(20),
    fecha_nacimiento DATE,
    direccion VARCHAR2(300),
    tipo_usuario VARCHAR2(20) NOT NULL CHECK (tipo_usuario IN ('SUPERADMIN', 'ADMIN', 'BIBLIOTECARIO')),
    activo NUMBER(1) DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TIMESTAMP DEFAULT SYSTIMESTAMP,
    ultimo_acceso TIMESTAMP
);

-- Crear índices para usuarios
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_tipo ON usuarios(tipo_usuario);

-- Crear tabla de lectores (usuarios de la biblioteca)
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
);

-- Crear índices para lectores
CREATE INDEX idx_lectores_codigo ON lectores(codigo_lector);
CREATE INDEX idx_lectores_documento ON lectores(numero_documento);
CREATE INDEX idx_lectores_email ON lectores(email);
CREATE INDEX idx_lectores_estado ON lectores(estado);

-- Crear tabla de libros
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
);

-- Crear índices para libros
CREATE INDEX idx_libros_isbn ON libros(isbn);
CREATE INDEX idx_libros_titulo ON libros(titulo);
CREATE INDEX idx_libros_autor ON libros(autor);
CREATE INDEX idx_libros_categoria ON libros(categoria);

-- Crear tabla de préstamos
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
);

-- Crear índices para préstamos
CREATE INDEX idx_prestamos_codigo ON prestamos(codigo_prestamo);
CREATE INDEX idx_prestamos_libro ON prestamos(libro_id);
CREATE INDEX idx_prestamos_lector ON prestamos(lector_id);
CREATE INDEX idx_prestamos_estado ON prestamos(estado);
CREATE INDEX idx_prestamos_fecha_prestamo ON prestamos(fecha_prestamo);

-- Crear secuencias para IDs (opcional, ya que usamos IDENTITY)
-- CREATE SEQUENCE seq_usuarios START WITH 1 INCREMENT BY 1;
-- CREATE SEQUENCE seq_libros START WITH 1 INCREMENT BY 1;
-- CREATE SEQUENCE seq_prestamos START WITH 1 INCREMENT BY 1;

-- Crear vista para préstamos activos con información completa
CREATE OR REPLACE VIEW v_prestamos_activos AS
SELECT 
    p.id,
    p.codigo_prestamo,
    p.fecha_prestamo,
    p.fecha_devolucion_esperada,
    p.estado,
    p.multa,
    l.isbn,
    l.titulo,
    l.autor,
    lec.codigo_lector,
    lec.nombre || ' ' || lec.apellido AS lector_nombre_completo,
    lec.email AS lector_email,
    lec.telefono AS lector_telefono,
    bp.username AS bibliotecario_prestamo_username,
    bp.nombre || ' ' || bp.apellido AS bibliotecario_prestamo_nombre,
    CASE 
        WHEN p.estado = 'ACTIVO' AND TRUNC(SYSDATE) > p.fecha_devolucion_esperada 
        THEN TRUNC(SYSDATE) - p.fecha_devolucion_esperada 
        ELSE 0 
    END AS dias_retraso
FROM prestamos p
JOIN libros l ON p.libro_id = l.id
JOIN lectores lec ON p.lector_id = lec.id
JOIN usuarios bp ON p.bibliotecario_prestamo_id = bp.id
WHERE p.estado = 'ACTIVO';

-- Comentarios en las tablas
COMMENT ON TABLE usuarios IS 'Tabla de usuarios del sistema BiblioSystem';
COMMENT ON TABLE lectores IS 'Tabla de lectores/usuarios de la biblioteca';
COMMENT ON TABLE libros IS 'Tabla de libros disponibles en la biblioteca';
COMMENT ON TABLE prestamos IS 'Tabla de préstamos de libros';

-- Comentarios en columnas importantes
COMMENT ON COLUMN usuarios.tipo_usuario IS 'Tipo de usuario: SUPERADMIN, ADMIN, BIBLIOTECARIO';
COMMENT ON COLUMN prestamos.estado IS 'Estado del préstamo: ACTIVO, DEVUELTO, VENCIDO';

-- Trigger para actualizar cantidad disponible al registrar préstamo
CREATE OR REPLACE TRIGGER trg_prestamo_actualizar_disponible
AFTER INSERT ON prestamos
FOR EACH ROW
WHEN (NEW.estado = 'ACTIVO')
BEGIN
    UPDATE libros 
    SET cantidad_disponible = cantidad_disponible - 1
    WHERE id = :NEW.libro_id;
END;

-- Trigger para actualizar cantidad disponible al devolver libro
CREATE OR REPLACE TRIGGER trg_devolucion_actualizar_disponible
AFTER UPDATE ON prestamos
FOR EACH ROW
WHEN (OLD.estado = 'ACTIVO' AND NEW.estado = 'DEVUELTO')
BEGIN
    UPDATE libros 
    SET cantidad_disponible = cantidad_disponible + 1
    WHERE id = :NEW.libro_id;
END; 