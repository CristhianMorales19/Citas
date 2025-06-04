import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Container, 
  Typography, 
  Box, 
  Grid, 
  Button, 
  Paper, 
  Divider, 
  CircularProgress, 
  Alert 
} from '@mui/material';
import { format, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';
import { doctorPublicService } from '../services/doctorService';
import { useAutenticacion } from '../contexts/AuthContext';

const DoctorAvailabilityPage = () => {
  const { doctorId } = useParams();
  const [availability, setAvailability] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { usuario, estaAutenticado } = useAutenticacion();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchAvailability = async () => {
      if (!doctorId) return;
      
      try {
        setLoading(true);
        setError('');
        
        const data = await doctorPublicService.getDoctorAvailability(parseInt(doctorId, 10));
        setAvailability(data);
      } catch (err) {
        console.error('Error al cargar disponibilidad:', err);
        setError('No pudimos cargar la disponibilidad del médico. Por favor intente nuevamente.');
      } finally {
        setLoading(false);
      }
    };
    
    fetchAvailability();
  }, [doctorId]);

  const formatDate = (dateString) => {
    try {
      const date = parseISO(dateString);
      return format(date, "EEEE, d 'de' MMMM", { locale: es });
    } catch (error) {
      return dateString;
    }
  };

  const handleBookAppointment = (date, time) => {
    if (estaAutenticado) {
      // Si el usuario está autenticado, redirigir a la página de confirmación de cita
      navigate('/appointments/book', { 
        state: { 
          doctorId,
          doctorName: availability?.doctor.name,
          date,
          time
        } 
      });
    } else {
      // Si no está autenticado, redirigir a la página de inicio de sesión
      navigate('/login', { 
        state: { 
          from: `/public/doctors/${doctorId}/availability`,
          booking: { doctorId, date, time }
        } 
      });
    }
  };

  if (loading) {
    return (
      <Container sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error || !availability) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">{error || 'No se pudo cargar la información del médico'}</Alert>
        <Button 
          variant="outlined" 
          sx={{ mt: 2 }}
          onClick={() => navigate('/doctors')}
        >
          Volver a la búsqueda
        </Button>
      </Container>
    );
  }

  const { doctor, availableDays } = availability;

  return (
    <Container maxWidth="md" sx={{ my: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Dr. {doctor.name}
        </Typography>
        <Typography variant="h6" color="text.secondary" gutterBottom>
          {doctor.specialty}
        </Typography>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid item xs={12} sm={6}>
            <Typography variant="body1">
              <Typography component="span" fontWeight="bold">Ubicación:</Typography> {doctor.location}
            </Typography>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="body1">
              <Typography component="span" fontWeight="bold">Costo de consulta:</Typography> ${doctor.consultationCost}
            </Typography>
          </Grid>
        </Grid>
        {doctor.presentation && (
          <Box sx={{ mt: 2 }}>
            <Typography variant="body1">
              {doctor.presentation}
            </Typography>
          </Box>
        )}
      </Box>

      <Divider sx={{ my: 4 }} />
      
      <Typography variant="h5" gutterBottom>
        Horarios disponibles para los próximos días
      </Typography>
      
      {availableDays.length > 0 ? (
        <Grid container spacing={3} sx={{ mt: 2 }}>
          {availableDays.map((day, dayIndex) => (
            <Grid item xs={12} md={4} key={dayIndex}>
              <Paper elevation={3} sx={{ p: 2, height: '100%' }}>
                <Typography variant="h6" gutterBottom>
                  {formatDate(day.date)}
                </Typography>
                
                <Box sx={{ mt: 2 }}>
                  <Grid container spacing={1}>
                    {day.slots.map((slot, slotIndex) => (
                      <Grid item xs={6} key={slotIndex}>
                        <Button
                          variant={slot.available ? "contained" : "outlined"}
                          color={slot.available ? "primary" : "inherit"}
                          disabled={!slot.available}
                          fullWidth
                          size="small"
                          onClick={() => slot.available && handleBookAppointment(day.date, slot.time)}
                          sx={{ 
                            mb: 1,
                            backgroundColor: slot.available ? undefined : '#f5f5f5',
                            color: slot.available ? undefined : 'text.disabled',
                            '&.Mui-disabled': {
                              opacity: 0.7,
                            }
                          }}
                        >
                          {slot.time.substring(0, 5)}
                        </Button>
                      </Grid>
                    ))}
                  </Grid>
                </Box>
              </Paper>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Alert severity="info" sx={{ mt: 2 }}>
          No hay horarios disponibles para los próximos días.
        </Alert>
      )}
      
      <Box sx={{ mt: 4, display: 'flex', justifyContent: 'space-between' }}>
        <Button 
          variant="outlined" 
          onClick={() => navigate('/doctors')}
        >
          Volver a la búsqueda
        </Button>
      </Box>
    </Container>
  );
};

export default DoctorAvailabilityPage;
