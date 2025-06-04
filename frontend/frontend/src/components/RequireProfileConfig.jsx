import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAutenticacion } from '../contexts/AuthContext';

/**
 * Componente que gestiona la redirección de médicos según su estado de configuración
 * - Si el médico no ha configurado su perfil, lo redirige a la página de perfil
 * - Si el médico ya ha configurado su perfil, lo redirige a la página de citas
 * - Para usuarios que no son médicos, permite el acceso normal a la ruta solicitada
 */
const RequireProfileConfig = () => {
  const { usuario, cargando } = useAutenticacion();

  // Si está cargando, muestra un mensaje de carga
  if (cargando) {
    return <div>Cargando...</div>;
  }

  // Solo aplicar lógica de redirección a usuarios médicos autenticados
  if (usuario && usuario.role === 'Doctor') { // Corregido: 'Doctor' en lugar de 'medico'
    // Verificar si es un médico recién logueado
    const isLoginRedirect = window.location.pathname === '/' || 
                          window.location.pathname === '/login';

    // Si el médico necesita configurar su perfil, redirigir a la página de perfil
    if (usuario.doctorProfile && usuario.doctorProfile.status === 'APPROVED' && !usuario.doctorProfile.profileConfigured) {
      console.log('Redirigiendo médico a configuración de perfil');
      return <Navigate to="/Doctor/profile" replace />;
    } 
    // Si ya ha configurado su perfil y viene de login/inicio, redirigir a la página de citas
    else if (isLoginRedirect && 
             usuario.doctorProfile && 
             usuario.doctorProfile.status === 'APPROVED' && 
             usuario.doctorProfile.profileConfigured &&
             window.location.pathname !== '/Doctor/appointments') {
      console.log('Redirigiendo médico a página de citas');
      return <Navigate to="/Doctor/appointments" replace />;
    }
    
    // Agregar registro para depuración
    console.log('Estado del médico:', {
      role: usuario.role,
      hasProfile: !!usuario.doctorProfile,
      status: usuario.doctorProfile?.status,
      profileConfigured: usuario.doctorProfile?.profileConfigured,
      currentPath: window.location.pathname
    });
  }

  // En cualquier otro caso, permitir el acceso a la ruta solicitada
  return <Outlet />;
};

export default RequireProfileConfig;
