-- Fix para la tabla horario: eliminar columna duplicada doctor_id
-- La entidad Horario usa id_medico, así que eliminamos doctor_id

-- Verificar las columnas actuales
DESCRIBE horario;

-- Eliminar la columna doctor_id duplicada
ALTER TABLE horario DROP COLUMN doctor_id;

-- Verificar que la tabla ahora solo tenga id_medico
DESCRIBE horario;
