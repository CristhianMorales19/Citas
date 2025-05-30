-- Script para verificar médicos pendientes
SELECT 
    d.id as doctor_id,
    d.especialidad,
    d.status,
    d.activo,
    u.id as user_id,
    u.name as user_name,
    u.username,
    r.name as role_name
FROM doctores d
LEFT JOIN usuarios u ON d.id_usuario = u.id
LEFT JOIN roles r ON u.role_id = r.id
ORDER BY d.id DESC;
