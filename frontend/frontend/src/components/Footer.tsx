import React from 'react';
import { Box, Container, Typography, Link } from '@mui/material';

const Footer: React.FC = () => {
  return (
    <Box
      component="footer"
      sx={{
        py: 3,
        px: 2,
        mt: 'auto',
        backgroundColor: (theme) => theme.palette.grey[200],
      }}
    >
      <Container maxWidth="lg">
        <Typography variant="body2" color="text.secondary" align="center">
          {'© '}
          {new Date().getFullYear()}
          {' Sistema de Citas Médicas. Todos los derechos reservados. '}
        </Typography>
        <Typography variant="body2" color="text.secondary" align="center">
          <Link color="inherit" href="/about">
            Acerca de
          </Link>
          {' | '}
          <Link color="inherit" href="/contact">
            Contacto
          </Link>
          {' | '}
          <Link color="inherit" href="/privacy">
            Política de Privacidad
          </Link>
        </Typography>
      </Container>
    </Box>
  );
};

export default Footer; 