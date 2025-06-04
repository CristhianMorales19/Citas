import React from 'react';
import { Container, Typography, Box, Paper } from '@mui/material';

const About: React.FC = () => {
  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#4a90e2' }}>
          Acerca de Nosotros
        </Typography>

        <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Nuestra Misión
          </Typography>
          <Typography paragraph>
            Nuestro sistema de citas médicas tiene como objetivo facilitar el proceso de agendamiento
            de citas entre pacientes y médicos, proporcionando una plataforma segura, eficiente y
            fácil de usar.
          </Typography>
        </Paper>

        <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Características Principales
          </Typography>
          <Typography component="div">
            <ul>
              <li>Búsqueda de médicos por especialidad y ubicación</li>
              <li>Visualización de horarios disponibles</li>
              <li>Agendamiento de citas en línea</li>
              <li>Gestión de perfiles médicos</li>
              <li>Sistema de notificaciones</li>
              <li>Historial de citas</li>
            </ul>
          </Typography>
        </Paper>

        <Paper elevation={3} sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Contacto
          </Typography>
          <Typography paragraph>
            Para más información o soporte, no dude en contactarnos:
          </Typography>
          <Typography>
            Email: info@sistemacitas.com
          </Typography>
          <Typography>
            Teléfono: (123) 456-7890
          </Typography>
        </Paper>
      </Box>
    </Container>
  );
};

export default About; 