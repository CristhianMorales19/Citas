import React from 'react';
import { Typography, Grid, Box } from '@mui/material';

interface DoctorAppointmentInfoProps {
  doctor: {
    name: string;
    specialty: string;
    location: string;
    consultationCost: number;
  };
}

const DoctorAppointmentInfo: React.FC<DoctorAppointmentInfoProps> = ({ doctor }) => {
  return (
    <Grid item xs={12}>
      <Typography variant="h6" gutterBottom>
        Información del Doctor
      </Typography>
      <Box sx={{ ml: 2 }}>
        <Typography variant="body1" gutterBottom>
          <strong>Dr. {doctor.name}</strong>
        </Typography>
        <Typography variant="body1" gutterBottom>
          <strong>Especialidad:</strong> {doctor.specialty}
        </Typography>
        <Typography variant="body1" gutterBottom>
          <strong>Ubicación:</strong> {doctor.location}
        </Typography>
        <Typography variant="body1" gutterBottom>
          <strong>Costo de consulta:</strong> ${doctor.consultationCost}
        </Typography>
      </Box>
    </Grid>
  );
};

export default DoctorAppointmentInfo;
