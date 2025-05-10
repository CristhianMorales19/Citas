CREATE DATABASE proyectocitas;

USE proyectocitas;

CREATE TABLE Usuario (
                         id_usuario INT PRIMARY KEY AUTO_INCREMENT,
                         nombre_usuario VARCHAR(50) NOT NULL,
                         contrasena VARCHAR(255) NOT NULL,
                         rol enum('admin', 'medico', 'paciente') NOT NULL,
                         UNIQUE (nombre_usuario)
);

CREATE TABLE Medico (
                        id_medico INT PRIMARY KEY AUTO_INCREMENT,
                        id_usuario INT UNIQUE NOT NULL,
                        nombre VARCHAR(75) NOT NULL,
                        especialidad VARCHAR(50) NOT NULL,
                        costo_consulta DECIMAL(10,2) NOT NULL,
                        ubicacion VARCHAR(100) NOT NULL,
                        presentacion TEXT,
                        frecuencia_citas INT NOT NULL,
                        autorizado TINYINT(1) NOT NULL DEFAULT 0,
                        primera_vez TINYINT(1) NOT NULL DEFAULT 1,
                        FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);

CREATE TABLE Horario (
                         id_horario INT PRIMARY KEY AUTO_INCREMENT,
                         id_medico INT NOT NULL,
                         dia DATE NOT NULL,
                         hora_inicio TIME NOT NULL,
                         hora_fin TIME NOT NULL,
                         reservado TINYINT(1) NOT NULL DEFAULT 0,
                         FOREIGN KEY (id_medico) REFERENCES Medico(id_medico) ON DELETE CASCADE
);

CREATE TABLE paciente (
                          id_paciente INT NOT NULL,
                          id_usuario INT,
                          nombre VARCHAR(75) NOT NULL,
                          version INT DEFAULT 0,
                          PRIMARY KEY (id_paciente),
                          CONSTRAINT fk_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

CREATE TABLE cita (
                      id_cita INT NOT NULL AUTO_INCREMENT,
                      id_paciente INT NOT NULL,
                      id_medico INT NOT NULL,
                      horario_id_horario INT,
                      estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
                      PRIMARY KEY (id_cita),
                      CONSTRAINT fk_cita_paciente FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
                      CONSTRAINT fk_cita_medico FOREIGN KEY (id_medico) REFERENCES medico(id_medico),
                      CONSTRAINT fk_cita_horario FOREIGN KEY (horario_id_horario) REFERENCES horario(id_horario)
);
