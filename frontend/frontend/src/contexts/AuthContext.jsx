import React, { createContext, useContext, useState, useEffect } from 'react';
import { servicioAutenticacion, DoctorService } from '../services/api';
import axios from 'axios';

// Crear el contexto con un valor predeterminado
const AuthContext = createContext(null);

export const ProveedorAutenticacion = ({ children }) => {
  const [usuario, setUsuario] = useState(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    verificarAutenticacion();
  }, []);

  const verificarAutenticacion = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        const datosUsuario = await servicioAutenticacion.obtenerUsuarioActual();
        setUsuario(datosUsuario);
        
        // Verificar si es un médico, para obtener su perfil y determinar redirecciones
        if (datosUsuario.role === 'Doctor') {
          try {
            // Obtener el perfil del médico para verificar su estado
            const doctorProfile = await DoctorService.getProfile();
            
            // Determinar el estado del médico para la redirección adecuada
            // Caso 1: Médico aprobado pero sin perfil configurado -> Página de perfil
            if (doctorProfile.status === 'APPROVED' && !doctorProfile.profileConfigured) {
              console.log('Médico aprobado sin perfil configurado');
              setUsuario(prevUser => ({
                ...prevUser,
                doctorProfile: {
                  ...doctorProfile,
                  // Usamos directamente profileConfigured = false (ya viene del backend)
                }
              }));
            }
            // Caso 2: Médico con perfil configurado -> Página de citas
            else if (doctorProfile.status === 'APPROVED' && doctorProfile.profileConfigured) {
              console.log('Médico con perfil ya configurado');
              setUsuario(prevUser => ({
                ...prevUser,
                doctorProfile: {
                  ...doctorProfile,
                  // Usamos directamente profileConfigured = true (ya viene del backend)
                }
              }));
            }
            // Caso 3: Médico pendiente o rechazado -> Sin redirección especial
            else {
              console.log('Médico en estado:', doctorProfile.status);
              setUsuario(prevUser => ({
                ...prevUser,
                doctorProfile: doctorProfile
              }));
            }
          } catch (err) {
            console.error('Error al obtener perfil del médico:', err);
          }
        }
      }
    } catch (error) {
      console.error('Error al verificar autenticación:', error);
      localStorage.removeItem('token');
    } finally {
      setCargando(false);
    }
  };

  const iniciarSesion = async (nombreUsuario, contrasena) => {
    try {
      const respuesta = await servicioAutenticacion.iniciarSesion({ nombreUsuario, contrasena });
      if (respuesta.success && respuesta.token && respuesta.user) {
        localStorage.setItem('token', respuesta.token);
        
        // Si es un médico, necesitamos cargar su perfil para determinar redirecciones
        if (respuesta.user.role === 'Doctor') {
          console.log('Médico autenticado, verificando perfil...');
          // Primero establecemos el usuario básico para la autenticación
          setUsuario(respuesta.user);
          // Luego obtenemos la información completa incluyendo el perfil
          await verificarAutenticacion();
        } else {
          // Para usuarios que no son médicos, simplemente establecemos el usuario
          setUsuario(respuesta.user);
        }
      } else {
        throw new Error(respuesta.message || 'Error al iniciar sesión');
      }
    } catch (error) {
      console.error('Error al iniciar sesión:', error);
      throw error;
    }
  };

  const registrar = async (username, password, confirmPassword, name, rol) => {
    try {
      const respuesta = await servicioAutenticacion.registrar({
        username,
        password,
        confirmPassword,
        name,
        role: rol // Corregido: enviar como 'role' que es lo que espera el backend
      });
      if (respuesta.success && respuesta.token && respuesta.user) {
        localStorage.setItem('token', respuesta.token);
        setUsuario(respuesta.user);
        
        // Retornar información adicional junto con el resultado exitoso
        return {
          success: true,
          user: respuesta.user,
          isDoctor: respuesta.user.role === 'medico',
          status: respuesta.doctorStatus || 'PENDING' // Por defecto, asumimos PENDING para médicos nuevos
        };
      } else {
        throw new Error(respuesta.message || 'Error al registrar el usuario');
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data?.message || 'Error al registrar el usuario');
      }
      throw new Error('Error al registrar el usuario');
    }
  };

  const cerrarSesion = () => {
    localStorage.removeItem('token');
    setUsuario(null);
  };

  const valor = {
    usuario,
    cargando,
    iniciarSesion,
    registrar,
    cerrarSesion,
    estaAutenticado: !!usuario,
  };

  return (
    <AuthContext.Provider value={valor}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAutenticacion = () => {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error('useAutenticacion debe usarse dentro de un ProveedorAutenticacion');
  }
  return contexto;
};
