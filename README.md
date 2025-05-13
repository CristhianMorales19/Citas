# Sistema de Citas Médicas

Este proyecto es un sistema de gestión de citas médicas con un backend en Spring Boot y un frontend en React.

## Requisitos

- Java 17
- Node.js 14+
- MySQL

## Configuración de la Base de Datos

1. Asegúrate de tener MySQL instalado y ejecutándose
2. La aplicación creará automáticamente la base de datos `proyectocitas` si no existe
3. Por defecto, la aplicación usa el usuario `root` sin contraseña. Si necesitas cambiar esto, edita el archivo `backend/src/main/resources/application.properties`

## Ejecutar el Backend

1. Navega a la carpeta del backend:
   ```
   cd backend
   ```

2. Ejecuta la aplicación:
   ```
   mvn spring-boot:run
   ```
   
   El backend se ejecutará en http://localhost:8080

## Ejecutar el Frontend

1. Navega a la carpeta del frontend:
   ```
   cd frontend
   ```

2. Instala las dependencias:
   ```
   npm install
   ```

3. Ejecuta la aplicación:
   ```
   npm start
   ```
   
   El frontend se ejecutará en http://localhost:3000

## Usuarios Predeterminados

La aplicación incluye tres usuarios predeterminados para facilitar las pruebas:

1. **Administrador**
   - Usuario: admin
   - Contraseña: admin123
   - Rol: admin

2. **Doctor**
   - Usuario: doctor
   - Contraseña: doctor123
   - Rol: medico

3. **Paciente**
   - Usuario: paciente
   - Contraseña: paciente123
   - Rol: paciente

## Funcionalidades

- Registro e inicio de sesión de usuarios
- Los pacientes pueden buscar médicos y reservar citas
- Los médicos pueden gestionar su perfil y ver/gestionar sus citas
- Los administradores pueden aprobar perfiles de médicos
