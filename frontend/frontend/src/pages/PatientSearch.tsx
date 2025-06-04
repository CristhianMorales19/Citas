import React from 'react';
// @ts-ignore - Workaround for React import issues
import { useState } from 'react';
// @ts-ignore - Force import for TypeScript
type ChangeEvent<T = Element> = React.FormEvent & {
  target: EventTarget & T;
};
import { FC } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Grid,
  TextField,
  Button,
  Card,
  CardContent,
  CardActions,
  Chip,
  Alert,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { format, addDays } from 'date-fns';
import { es } from 'date-fns/locale';
import { appointmentService } from '../services/api';
import type { DoctorSearchResult, SearchParams, AvailableTimeSlot } from '../types/appointment';
import { useAutenticacion } from '../contexts/AuthContext';

const PatientSearch: FC = () => {
  const navigate = useNavigate();
  const { usuario, estaAutenticado } = useAutenticacion();
  const [Doctors, setDoctors] = useState<DoctorSearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [bookError, setBookError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useState<SearchParams>({
    specialty: '',
    location: '',
    date: format(new Date(), 'yyyy-MM-dd'),
  });

  const handleSearch = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await appointmentService.searchDoctors(searchParams);
      setDoctors(data);
    } catch (error) {
      setError('Error al buscar médicos');
    } finally {
      setLoading(false);
    }
  };

  const handleBookAppointment = (DoctorId: string, date: string, time: string) => {
    if (!DoctorId || !date || !time) {
      setBookError('No se puede agendar: falta información de doctor, fecha u hora.');
      return;
    }
    if (!estaAutenticado) {
      navigate('/login', { state: { from: `/appointments/book/${DoctorId}?date=${date}&time=${time}` } });
      return;
    }
    // Usa DoctorId con D mayúscula para que coincida con la ruta de React Router
    navigate(`/appointments/book/${DoctorId}?date=${date}&time=${time}`);
  };

  const handleViewExtendedSchedule = (DoctorId: string) => {
    navigate(`/Doctors/${DoctorId}/schedule`);
  };

  const handleLocationChange = (e: { target: { value: string } }) => {
    setSearchParams(prev => ({ ...prev, location: e.target.value }));
  };

  const handleDateChange = (e: { target: { value: string } }) => {
    setSearchParams(prev => ({ ...prev, date: e.target.value }));
  };

  const handleDoctorClick = (Doctor: DoctorSearchResult) => {
    navigate(`/doctors/${Doctor.id}`);
  };

  const handleSlotClick = (slot: AvailableTimeSlot) => {
    navigate(`/appointment/${slot.time}`);
  };

  const getAvailableDates = () => {
    const dates = [];
    const today = new Date();
    for (let i = 0; i < 3; i++) {
      dates.push(format(addDays(today, i), 'yyyy-MM-dd'));
    }
    return dates;
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#4a90e2' }}>
          Sistema de Citas Médicas
        </Typography>

        {!estaAutenticado && (
          <Paper elevation={3} sx={{ p: 3, mb: 4, textAlign: 'center' }}>
            <Typography variant="h6" gutterBottom>
              ¿Eres médico o paciente?
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', mt: 2 }}>
              <Button
                variant="contained"
                color="primary"
                onClick={() => navigate('/login')}
                sx={{ minWidth: 200 }}
              >
                Iniciar Sesión
              </Button>
              <Button
                variant="outlined"
                color="primary"
                onClick={() => navigate('/register')}
                sx={{ minWidth: 200 }}
              >
                Registrarse
              </Button>
            </Box>
          </Paper>
        )}

        <Paper elevation={3} sx={{ p: 3, mb: 4 }}>
          <Typography variant="h5" gutterBottom>
            Buscar Médicos
          </Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <FormControl fullWidth>
                <InputLabel>Especialidad</InputLabel>
                <Select
                  value={searchParams.specialty}
                  label="Especialidad"
                  onChange={(e: SelectChangeEvent<string>) => setSearchParams({ ...searchParams, specialty: e.target.value })}
                >
                  <MenuItem value="">Todas</MenuItem>
                  <MenuItem value="Medicina General">Medicina General</MenuItem>
                  <MenuItem value="Pediatría">Pediatría</MenuItem>
                  <MenuItem value="Ginecología">Ginecología</MenuItem>
                  <MenuItem value="Cardiología">Cardiología</MenuItem>
                  <MenuItem value="Dermatología">Dermatología</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Ciudad"
                value={searchParams.location}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchParams({ ...searchParams, location: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="contained"
                onClick={handleSearch}
                sx={{ height: '56px' }}
              >
                Buscar
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {bookError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {bookError}
          </Alert>
        )}

        {loading ? (
          <Box display="flex" justifyContent="center" my={4}>
            <CircularProgress />
          </Box>
        ) : (
          <Grid container spacing={3}>
            {Doctors.map((Doctor: any) => (
              <Grid item xs={12} md={6} key={Doctor.id}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Dr. {Doctor.name}
                    </Typography>
                    <Typography color="textSecondary" gutterBottom>
                      {Doctor.specialty}
                    </Typography>
                    <Typography variant="body2" paragraph>
                      {Doctor.location}
                    </Typography>
                    <Typography variant="body2" paragraph>
                      Costo de consulta: ${Doctor.consultationCost}
                    </Typography>
                    <Typography variant="body2" paragraph>
                      {Doctor.presentation}
                    </Typography>

                    <Typography variant="subtitle2" gutterBottom>
                      Espacios disponibles:
                    </Typography>
                    {getAvailableDates().map((date: string) => (
                      <Box key={date} mb={2}>
                        <Typography variant="body2" color="textSecondary">
                          {format(new Date(date), 'EEEE dd/MM', { locale: es })}
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1 }}>
                          {Doctor.availableSlots[date]?.map((slot: any) => (
                            <Chip
                              key={slot.time}
                              label={slot.time}
                              onClick={() => handleBookAppointment(Doctor.id, date, slot.time || '')}
                              color={slot.isAvailable ? 'primary' : 'default'}
                              disabled={!slot.isAvailable}
                              sx={{ m: 0.5 }}
                            />
                          ))}
                        </Box>
                      </Box>
                    ))}
                  </CardContent>
                  <CardActions>
                    <Button
                      size="small"
                      color="primary"
                      onClick={() => handleViewExtendedSchedule(Doctor.id)}
                    >
                      Ver Horario Completo
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Box>
    </Container>
  );
};

export default PatientSearch;