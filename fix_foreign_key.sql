-- Script para corregir la clave foránea de la tabla cita
-- Este script corrige el problema donde la tabla cita apunta a la tabla 'patients' vacía
-- en lugar de la tabla 'paciente' que realmente contiene los datos

USE proyectocitas;

-- Mostrar las claves foráneas actuales
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'cita' AND CONSTRAINT_NAME LIKE '%paciente%';

-- Eliminar la clave foránea incorrecta que apunta a 'patients'
ALTER TABLE cita DROP FOREIGN KEY cita_ibfk_3;

-- Agregar la clave foránea correcta que apunta a 'paciente'
ALTER TABLE cita ADD CONSTRAINT FK_cita_paciente 
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente);

-- Verificar que la nueva clave foránea se creó correctamente
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'cita' AND CONSTRAINT_NAME = 'FK_cita_paciente';

SELECT 'Clave foránea corregida exitosamente!' as Status;
