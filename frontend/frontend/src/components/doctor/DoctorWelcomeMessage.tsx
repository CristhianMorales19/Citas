import React from 'react';
import {
  Box,
  Alert,
  AlertTitle,
  Typography,
  Button,
} from '@mui/material';
import { CheckCircle, Info } from '@mui/icons-material';

interface DoctorWelcomeMessageProps {
  onDismiss: () => void;
}

const DoctorWelcomeMessage: React.FC<DoctorWelcomeMessageProps> = ({ onDismiss }) => {
  return (
    <Alert
      severity="success"
      icon={<CheckCircle />}
      sx={{ mb: 3 }}
      action={
        <Button color="inherit" size="small" onClick={onDismiss}>
          Entendido
        </Button>
      }
    >
      <AlertTitle>¡Bienvenido! Tu cuenta ha sido aprobada</AlertTitle>
      <Typography variant="body2">
        ¡Felicitaciones! Tu cuenta de médico ha sido aprobada por nuestro equipo. 
        Ahora puedes completar tu perfil profesional y comenzar a recibir citas de pacientes.
      </Typography>
      <Box sx={{ mt: 2 }}>
        <Alert severity="info" icon={<Info />}>
          <Typography variant="body2">
            <strong>Importante:</strong> Por favor, completa toda la información de tu perfil 
            para que los pacientes puedan encontrarte y agendar citas contigo.
          </Typography>
        </Alert>
      </Box>
    </Alert>
  );
};

export default DoctorWelcomeMessage;
