import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAutenticacion } from '../contexts/AuthContext';
import { ROUTES } from '../constants/routes';

const Header: React.FC = () => {
  const navigate = useNavigate();
  const { usuario, estaAutenticado, cerrarSesion } = useAutenticacion();

  const handleLogout = () => {
    cerrarSesion();
    navigate(ROUTES.HOME);
  };

  const renderUserNavigation = () => {
    if (!usuario) return null;

    switch (usuario.role) {
      case 'Doctor':
      case 'medico':
        return (
          <>
            <Button color="inherit" onClick={() => navigate(ROUTES.DOCTOR.PROFILE)}>
              Perfil
            </Button>
            <Button color="inherit" onClick={() => navigate(ROUTES.DOCTOR.APPOINTMENTS)}>
              Mis Citas
            </Button>
          </>
        );
      
      case 'paciente':
        return (
          <>
            <Button color="inherit" onClick={() => navigate(ROUTES.PATIENT.SEARCH)}>
              Buscar Doctor
            </Button>
            <Button color="inherit" onClick={() => navigate(ROUTES.PATIENT.APPOINTMENTS)}>
              Mis Citas
            </Button>
          </>
        );
      
      case 'admin':
        return (
          <>
            <Button color="inherit" onClick={() => navigate(ROUTES.ADMIN.PENDING_DOCTORS)}>
              Doctores Pendientes
            </Button>
            <Button color="inherit" onClick={() => navigate(ROUTES.ADMIN.DASHBOARD)}>
              Panel Admin
            </Button>
          </>
        );
      
      default:
        return null;
    }
  };

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography 
          variant="h6" 
          component="div" 
          sx={{ flexGrow: 1, cursor: 'pointer' }}
          onClick={() => navigate(ROUTES.HOME)}
        >
          Sistema de Citas Médicas
        </Typography>
        
        <Box>
          {estaAutenticado ? (
            <>
              <Button color="inherit" onClick={() => navigate(ROUTES.HOME)}>
                Inicio
              </Button>
              {renderUserNavigation()}
              <Button color="inherit" onClick={handleLogout}>
                Cerrar Sesión
              </Button>
            </>
          ) : (
            <>
              <Button color="inherit" onClick={() => navigate(ROUTES.LOGIN)}>
                Iniciar Sesión
              </Button>
              <Button color="inherit" onClick={() => navigate(ROUTES.REGISTER)}>
                Registrarse
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header; 