-- Script para diagnosticar el problema de restricción única en prestamos

-- 1. Verificar códigos duplicados
SELECT codigo_prestamo, COUNT(*) as cantidad
FROM prestamos 
GROUP BY codigo_prestamo 
HAVING COUNT(*) > 1;

-- 2. Ver todos los códigos de préstamo ordenados
SELECT codigo_prestamo, id, fecha_prestamo
FROM prestamos 
ORDER BY codigo_prestamo;

-- 3. Verificar el último código generado
SELECT MAX(TO_NUMBER(SUBSTR(codigo_prestamo, 5))) as ultimo_numero
FROM prestamos 
WHERE REGEXP_LIKE(codigo_prestamo, '^PRES-[0-9]+$');

-- 4. Ver la estructura de la tabla prestamos
DESC prestamos;

-- 5. Verificar restricciones únicas
SELECT constraint_name, constraint_type, search_condition
FROM user_constraints 
WHERE table_name = 'PRESTAMOS' AND constraint_type IN ('U', 'P');

-- 6. Ver todos los préstamos recientes
SELECT id, codigo_prestamo, lector_id, libro_id, fecha_prestamo, estado
FROM prestamos 
ORDER BY fecha_prestamo DESC
FETCH FIRST 10 ROWS ONLY;