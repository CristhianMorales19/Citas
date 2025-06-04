import React from 'react';
import { Container, Typography, Box, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const pacienteDashboard: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#4a90e2' }}>
          Panel de Paciente
        </Typography>

        <Box sx={{ mt: 4 }}>
          <Button
            variant="contained"
            onClick={() => navigate('/appointments/search')}
            sx={{
              backgroundColor: '#4a90e2',
              '&:hover': {
                backgroundColor: '#357abD',
              },
            }}
          >
            Buscar Citas
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default pacienteDashboard; 