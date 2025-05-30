-- Script para crear un médico pendiente de prueba
INSERT INTO usuarios (username, password, name, email, enabled, role_id) 
SELECT 'doctor_test', '$2a$10$dummy', 'Dr. Test Pendiente', 'test@doctor.com', 1, r.id 
FROM roles r WHERE r.name = 'medico';

INSERT INTO medico (id_usuario, especialidad, costo_consulta, calificacion, activo, status, location, appointment_duration, presentation, photo_url, profile_configured, descripcion) 
SELECT u.id, 'Medicina General', 100.0, 0.0, 1, 'PENDING', 'Ciudad Test', 30, 'Doctor de prueba', '', 0, 'Descripción de prueba'
FROM usuarios u WHERE u.username = 'doctor_test';
