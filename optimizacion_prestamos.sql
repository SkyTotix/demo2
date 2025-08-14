-- Script de optimización para mejorar el rendimiento de la sección de préstamos
-- Ejecutar este script para crear índices que aceleren las consultas

-- 1. Índice compuesto para consultas de estado y fecha (usado en estadísticas)
CREATE INDEX IF NOT EXISTS idx_prestamos_estado_fecha 
ON prestamos (estado, fecha_devolucion_esperada);

-- 2. Índice para búsquedas por código de préstamo
CREATE INDEX IF NOT EXISTS idx_prestamos_codigo 
ON prestamos (codigo_prestamo);

-- 3. Índice para consultas por lector
CREATE INDEX IF NOT EXISTS idx_prestamos_lector 
ON prestamos (lector_id, estado);

-- 4. Índice para consultas por libro
CREATE INDEX IF NOT EXISTS idx_prestamos_libro 
ON prestamos (libro_id, estado);

-- 5. Índice para ordenamiento por fecha de préstamo (usado en obtenerTodos)
CREATE INDEX IF NOT EXISTS idx_prestamos_fecha_prestamo 
ON prestamos (fecha_prestamo DESC);

-- 6. Verificar que las estadísticas de las tablas estén actualizadas
-- (Oracle actualiza automáticamente, pero podemos forzar si es necesario)
BEGIN
    DBMS_STATS.GATHER_TABLE_STATS(
        ownname => USER,
        tabname => 'PRESTAMOS',
        estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
        cascade => TRUE
    );
END;
/

-- 7. Mostrar información sobre los índices creados
SELECT 
    index_name,
    table_name,
    column_name,
    column_position
FROM user_ind_columns 
WHERE table_name = 'PRESTAMOS'
ORDER BY index_name, column_position;

-- 8. Verificar el plan de ejecución de la consulta optimizada de estadísticas
EXPLAIN PLAN FOR
SELECT 
    COUNT(*) as total_prestamos,
    COUNT(CASE WHEN estado = 'ACTIVO' THEN 1 END) as prestamos_activos,
    COUNT(CASE WHEN estado = 'ACTIVO' AND fecha_devolucion_esperada < TRUNC(SYSDATE) THEN 1 END) as prestamos_vencidos,
    COUNT(CASE WHEN estado = 'DEVUELTO' THEN 1 END) as prestamos_devueltos
FROM prestamos;

-- Mostrar el plan de ejecución
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY());

PROMPT 'Optimización completada. Los índices han sido creados para mejorar el rendimiento.';
PROMPT 'Reinicie la aplicación para ver las mejoras en velocidad.';