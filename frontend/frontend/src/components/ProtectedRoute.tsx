import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAutenticacion } from '../contexts/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: ('Doctor' | 'paciente' | 'medico' | 'admin')[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  allowedRoles 
}) => {
  const { estaAutenticado, usuario } = useAutenticacion();
  const location = useLocation();

  if (!estaAutenticado) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && usuario) {
    return allowedRoles.includes(usuario.role) ? (
      <>{children}</>
    ) : (
      <Navigate to="/" replace />
    );
  }

  return <>{children}</>;
}; 