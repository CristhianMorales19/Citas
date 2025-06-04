import React from 'react';
import { Typography, Grid, Box } from '@mui/material';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

interface AppointmentDetailsProps {
  date: string;
  time: string;
}

const AppointmentDetails: React.FC<AppointmentDetailsProps> = ({ date, time }) => {
  return (
    <Grid item xs={12}>
      <Typography variant="h6" gutterBottom>
        Detalles de la Cita
      </Typography>
      <Box sx={{ ml: 2 }}>
        <Typography variant="body1" gutterBottom>
          <strong>Fecha:</strong> {format(new Date(date), 'EEEE dd/MM/yyyy', { locale: es })}
        </Typography>
        <Typography variant="body1" gutterBottom>
          <strong>Hora:</strong> {time}
        </Typography>
      </Box>
    </Grid>
  );
};

export default AppointmentDetails;
