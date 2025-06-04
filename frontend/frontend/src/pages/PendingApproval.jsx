import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Paper, Typography, Button, Box } from '@mui/material';
import PendingIcon from '@mui/icons-material/Pending';

const PendingApproval = () => {
  return (
    <Container component="main" maxWidth="sm">
      <Paper elevation={3} sx={{ p: 4, mt: 8, mb: 4, textAlign: 'center' }}>
        <PendingIcon sx={{ fontSize: 60, color: '#f9a825', mb: 2 }} />
        
        <Typography component="h1" variant="h4" gutterBottom>
          Cuenta pendiente de aprobación
        </Typography>
        
        <Typography variant="body1" paragraph>
          Tu registro como médico está pendiente de aprobación por el administrador.
        </Typography>
        
        <Typography variant="body1" paragraph>
          Recibirás notificaciones sobre el estado de tu solicitud. Una vez aprobada, 
          podrás configurar tu información profesional y comenzar a recibir citas.
        </Typography>
        
        <Typography variant="body2" color="text.secondary" paragraph>
          Este proceso generalmente toma entre 24 y 48 horas hábiles.
        </Typography>
        
        <Box mt={3}>
          <Button
            variant="contained"
            color="primary"
            component={Link}
            to="/"
            sx={{ mb: 2, width: '100%' }}
          >
            Ir a la página principal
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default PendingApproval;
