import React, { createContext, useContext, useState, useEffect } from 'react';
import { servicioAutenticacion, DoctorService } from '../services/api';
import type { User } from '../types/auth';
import type { DoctorProfile } from '../types/appointment';

interface AuthResponse {
  success: boolean;
  message?: string;
  token?: string;
  user?: User;
  doctorStatus?: string;
}

interface UserWithDoctorProfile {
  id: string;
  email: string;
  name: string;
  role: 'paciente' | 'Doctor' | 'medico' | 'admin';
  doctorProfile?: DoctorProfile;
}

export interface AuthContextType {
  usuario: UserWithDoctorProfile | null;
  cargando: boolean;
  iniciarSesion: (nombreUsuario: string, contrasena: string) => Promise<AuthResponse>;
  registrar: (username: string, password: string, confirmPassword: string, name: string, rol: 'paciente' | 'medico' | 'admin') => Promise<{ success: boolean; user: User; isDoctor: boolean; status: string; }>;
  cerrarSesion: () => void;
  estaAutenticado: boolean;
  actualizarUsuario: (updatedUser: Partial<UserWithDoctorProfile>) => void;
}

// Proporcionamos un valor predeterminado seguro para el contexto
const defaultContextValue: AuthContextType = {
  usuario: null,
  cargando: true,
  iniciarSesion: async () => { throw new Error('No implementado'); },
  registrar: async () => { throw new Error('No implementado'); },
  cerrarSesion: () => {},
  estaAutenticado: false,
  actualizarUsuario: () => {}
};

const AuthContext = createContext<AuthContextType>(defaultContextValue);

type ProveedorAutenticacionProps = {
  children: React.ReactNode;
};

export const ProveedorAutenticacion: React.FC<ProveedorAutenticacionProps> = ({ children }) => {
  const [usuario, setUsuario] = useState<UserWithDoctorProfile | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    verificarAutenticacion();
  }, []);

  const verificarAutenticacion = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        const datosUsuario = await servicioAutenticacion.obtenerUsuarioActual();
        
        // Convert User to UserWithDoctorProfile
        const userWithProfile: UserWithDoctorProfile = {
          id: datosUsuario.id,
          email: datosUsuario.email,
          name: datosUsuario.name,
          role: datosUsuario.role,
          doctorProfile: undefined
        };
        
        setUsuario(userWithProfile);
        
        // Verificar si es un médico, para obtener su perfil y determinar redirecciones
        if (datosUsuario.role === 'Doctor' || datosUsuario.role === 'medico') {
          try {
            // Obtener el perfil del médico para verificar su estado
            const doctorProfile = await DoctorService.getProfile();
            
            // Determinar el estado del médico para la redirección adecuada
            // Caso 1: Médico aprobado pero sin perfil configurado -> Página de perfil
            if (doctorProfile.status === 'APPROVED' && !doctorProfile.profileConfigured) {
              console.log('Médico aprobado sin perfil configurado');
              setUsuario(prevUser => ({
                ...prevUser!,
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
                ...prevUser!,
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
                ...prevUser!,
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
      localStorage.removeItem('usuario');
    } finally {
      setCargando(false);
    }
  };

  const iniciarSesion = async (nombreUsuario: string, contrasena: string): Promise<AuthResponse> => {
    try {
      const respuesta = await servicioAutenticacion.iniciarSesion({ nombreUsuario, contrasena });
      console.log('Respuesta del servidor en iniciarSesion:', respuesta);
      
      if (respuesta.success && respuesta.token && respuesta.user) {
        localStorage.setItem('token', respuesta.token);
        // Store user data in localStorage for persistence
        localStorage.setItem('usuario', JSON.stringify(respuesta.user));
        
        // Si es un médico, necesitamos cargar su perfil para determinar redirecciones
        if (respuesta.user.role === 'medico' || respuesta.user.role === 'Doctor') {
          console.log('Médico autenticado, verificando perfil...');
          // Primero establecemos el usuario básico para la autenticación
          const userWithProfile: UserWithDoctorProfile = {
            id: respuesta.user.id,
            email: respuesta.user.email,
            name: respuesta.user.name,
            role: respuesta.user.role,
            doctorProfile: undefined
          };
          setUsuario(userWithProfile);
          // Luego obtenemos la información completa incluyendo el perfil
          await verificarAutenticacion();
        } else {
          // Para usuarios que no son médicos, simplemente establecemos el usuario
          const userWithProfile: UserWithDoctorProfile = {
            id: respuesta.user.id,
            email: respuesta.user.email,
            name: respuesta.user.name,
            role: respuesta.user.role,
            doctorProfile: undefined
          };
          setUsuario(userWithProfile);
        }
        
        // Devolver la respuesta completa para que el componente que llama pueda usarla
        return respuesta;
      } else {
        const errorMessage = respuesta.message || 'Error al iniciar sesión';
        console.error('Error en la respuesta del servidor:', errorMessage);
        throw new Error(errorMessage);
      }
    } catch (error) {
      console.error('Error al iniciar sesión:', error);
      // Clear any partial data on error
      localStorage.removeItem('token');
      localStorage.removeItem('usuario');
      throw error;
    }
  };

  const registrar = async (username: string, password: string, confirmPassword: string, name: string, rol: 'paciente' | 'medico' | 'admin') => {
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
        localStorage.setItem('usuario', JSON.stringify(respuesta.user));
        
        // Convert User to UserWithDoctorProfile
        const userWithProfile: UserWithDoctorProfile = {
          id: respuesta.user.id,
          email: respuesta.user.email,
          name: respuesta.user.name,
          role: respuesta.user.role,
          doctorProfile: undefined
        };
        setUsuario(userWithProfile);
        
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
    } catch (error: any) {
      console.error('Error en registrar:', error);
      throw new Error(error.message || 'Error al registrar el usuario');
    }
  };

  const cerrarSesion = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    setUsuario(null);
  };

  const actualizarUsuario = (updatedUser: Partial<UserWithDoctorProfile>) => {
    setUsuario(prev => prev ? { ...prev, ...updatedUser } : null);
  };

  const valor: AuthContextType = {
    usuario,
    cargando,
    iniciarSesion,
    registrar,
    cerrarSesion,
    estaAutenticado: !!usuario,
    actualizarUsuario,
  };

  return (
    <AuthContext.Provider value={valor}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAutenticacion = (): AuthContextType => {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error('useAutenticacion debe usarse dentro de un ProveedorAutenticacion');
  }
  return contexto;
}; 