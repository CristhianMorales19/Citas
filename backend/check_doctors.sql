-- Check all doctors in the database
SELECT * FROM medico;

-- Check doctors with status
SELECT id, status, especialidad, location FROM medico WHERE status = 'APPROVED';

-- Check all doctors regardless of status
SELECT id, status, especialidad, location FROM medico;

-- Check user table to see if users exist
SELECT id, username, name, email FROM usuario WHERE role = 'DOCTOR';
