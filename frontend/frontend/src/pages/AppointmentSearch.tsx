import React from 'react';
// @ts-ignore - Workaround for React import issues
import { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  SelectChangeEvent,
  MenuItem,
  Alert,
  CircularProgress,
  Snackbar,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { es } from 'date-fns/locale';
import { appointmentService } from '../services/api';
import { DoctorSearchResult, SearchParams } from '../types/appointment';

const specialties = [
  'Medicina General',
  'Pediatría',
  'Ginecología',
  'Cardiología',
  'Dermatología',
  'Oftalmología',
  'Ortopedia',
  'Psiquiatría',
];

const AppointmentSearch = () => {
  const [searchParams, setSearchParams] = useState<SearchParams>({});
  const [Doctors, setDoctors] = useState<DoctorSearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedDoctor, setSelectedDoctor] = useState<DoctorSearchResult | null>(null);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [selectedTime, setSelectedTime] = useState<string | null>(null);
  const [bookingSuccess, setBookingSuccess] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  
  // Cargar médicos automáticamente al iniciar el componente
  useEffect(() => {
    handleSearch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearch = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log('=== DEPURACIÓN: Iniciando búsqueda de doctores ===');
      
      // Primero probamos el endpoint de debug para ver todos los doctores
      console.log('Probando endpoint /public/doctors/debug para ver todos los doctores...');
      const allDoctors = await appointmentService.getAllDoctorsDebug();
      console.log('Todos los doctores encontrados:', allDoctors);
      
      // Luego probamos el endpoint de doctores aprobados
      console.log('Probando endpoint /public/doctors/approved para ver doctores aprobados...');
      const approvedDoctors = await appointmentService.getApprovedDoctorsDebug();
      console.log('Doctores aprobados encontrados:', approvedDoctors);
      
      // Finalmente usamos el endpoint normal de búsqueda
      console.log('Probando endpoint normal de búsqueda con parámetros:', searchParams);
      const results = await appointmentService.searchDoctors(searchParams);
      console.log('Resultados de búsqueda normal:', results);
      
      // Inicializar availableSlots como un objeto vacío para cada doctor
      const doctorsWithSlots = results.map(doctor => ({
        ...doctor,
        availableSlots: {}
      }));
      setDoctors(doctorsWithSlots);
      
      console.log('=== FIN DEPURACIÓN ===');
    } catch (error) {
      console.error('Error en handleSearch:', error);
      setError('Error al buscar Doctores');
    } finally {
      setLoading(false);
    }
  };
  
  const handleSelectDoctor = async (doctor: DoctorSearchResult) => {
    console.log('Doctor seleccionado:', doctor);
    setSelectedDoctor(doctor);
    setSelectedDate(null);
    setSelectedTime(null);
    
    try {
      setLoading(true);
      // Obtener las fechas de los próximos 7 días
      const today = new Date();
      const endDate = new Date();
      endDate.setDate(today.getDate() + 7);
      
      // Formatear fechas como strings 'YYYY-MM-DD'
      const formatDate = (date: Date) => date.toISOString().split('T')[0];
      const startDateStr = formatDate(today);
      const endDateStr = formatDate(endDate);
      
      console.log('Buscando horarios desde', startDateStr, 'hasta', endDateStr, 'para el doctor', doctor.id);
      
      // Llamar al backend para obtener los horarios disponibles
      const availableSlots = await appointmentService.getAvailableSlots(
        doctor.id.toString(),
        startDateStr,
        endDateStr
      );
      
      console.log('Horarios disponibles recibidos:', availableSlots);
      
      // Verificar si hay horarios disponibles
      const hasAvailableSlots = Object.keys(availableSlots).length > 0;
      console.log('¿Hay horarios disponibles?', hasAvailableSlots);
      
      // Actualizar el doctor con los horarios disponibles
      const updatedDoctor = {
        ...doctor,
        availableSlots: availableSlots
      };
      
      console.log('Doctor actualizado con horarios:', updatedDoctor);
      setSelectedDoctor(updatedDoctor);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Error desconocido';
      console.error('Error al cargar horarios:', error);
      setError(`Error al cargar los horarios disponibles: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  const handleBookAppointment = async () => {
    if (!selectedDoctor || !selectedDate || !selectedTime) return;

    try {
      setLoading(true);
      setError(null);
      await appointmentService.bookAppointment(
        selectedDoctor.id.toString(),
        selectedDate,
        selectedTime
      );
      setSuccessMessage(`Cita agendada exitosamente con Dr. ${selectedDoctor.name} para el día ${selectedDate} a las ${selectedTime}`);
      setBookingSuccess(true);
      setSelectedDoctor(null);
      setSelectedDate(null);
      setSelectedTime(null);
      // Recargar la lista de médicos para reflejar los cambios en disponibilidad
      handleSearch();
    } catch (error) {
      if (error instanceof Error) {
        setError(`Error al agendar la cita: ${error.message}`);
      } else {
        setError('Error al agendar la cita. Por favor intente de nuevo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        {/* Snackbar para mostrar mensajes de éxito */}
        <Snackbar
          open={Boolean(successMessage)}
          autoHideDuration={6000}
          onClose={() => setSuccessMessage(null)}
          message={successMessage}
          anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
        />
        
        <Paper elevation={3} sx={{ p: 4, mb: 4 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#4a90e2' }}>
            Buscar Citas
          </Typography>

          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <FormControl fullWidth>
                <InputLabel>Especialidad</InputLabel>
                <Select
                  value={searchParams.specialty || ''}
                  label="Especialidad"
                  onChange={(e: SelectChangeEvent) => setSearchParams({ ...searchParams, specialty: e.target.value })}
                >
                  <MenuItem value="">Todas</MenuItem>
                  {specialties.map((specialty) => (
                    <MenuItem key={specialty} value={specialty}>
                      {specialty}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Ubicación"
                value={searchParams.location || ''}
                // @ts-ignore - Omitir verificación de tipo para el evento
                onChange={(e) => setSearchParams({ ...searchParams, location: e.target.value as string })}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={es}>
                <DatePicker
                  label="Fecha"
                  value={searchParams.date ? new Date(searchParams.date) : null}
                  // @ts-ignore - Omitir verificación de tipo para el parámetro date
                  onChange={(date) => {
                    if (date) {
                      setSearchParams({
                        ...searchParams,
                        date: date.toISOString().split('T')[0],
                      });
                    }
                  }}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </LocalizationProvider>
            </Grid>

            <Grid item xs={12}>
              <Button
                variant="contained"
                onClick={handleSearch}
                disabled={loading}
                sx={{
                  backgroundColor: '#4a90e2',
                  '&:hover': {
                    backgroundColor: '#357abD',
                  },
                }}
              >
                {loading ? <CircularProgress size={24} /> : 'Buscar'}
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {bookingSuccess && (
          <Alert severity="success" sx={{ mb: 2 }}>
            Cita agendada exitosamente
          </Alert>
        )}

        <Grid container spacing={3}>
          {/* @ts-ignore - Omitir verificación de tipo para el parámetro Doctor */}
          {Doctors.map((Doctor) => (
            <Grid item xs={12} md={6} key={Doctor.id}>
              <Card>
                {Doctor.photo && (
                  <CardMedia
                    component="img"
                    height="200"
                    image={Doctor.photo}
                    alt={Doctor.name}
                  />
                )}
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Dr. {Doctor.name}
                  </Typography>
                  <Typography color="textSecondary" gutterBottom>
                    {Doctor.specialty}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    {Doctor.location}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Costo: ${Doctor.consultationCost}
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 2 }}>
                    {Doctor.presentation}
                  </Typography>
                </CardContent>
                <CardActions>
                  <Button
                    size="small"
                    color="primary"
                    onClick={() => handleSelectDoctor(Doctor)}
                  >
                    Agendar Cita
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>

        <Dialog
          open={Boolean(selectedDoctor)}
          onClose={() => setSelectedDoctor(null)}
          maxWidth="sm"
          fullWidth
        >
          {selectedDoctor && (
            <>
              <DialogTitle>Agendar Cita con Dr. {selectedDoctor.name}</DialogTitle>
              <DialogContent>
                <Box sx={{ mt: 2 }}>
                  <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={es}>
                    <DatePicker
                      label="Fecha"
                      value={selectedDate ? new Date(selectedDate) : null}
                      // @ts-ignore - Omitir verificación de tipo para el parámetro date
                      onChange={(date) => {
                        if (date) {
                          setSelectedDate(date.toISOString().split('T')[0]);
                          setSelectedTime(null);
                        }
                      }}
                      slotProps={{ textField: { fullWidth: true, sx: { mb: 2 } } }}
                    />
                  </LocalizationProvider>

                  {selectedDate && selectedDoctor.availableSlots[selectedDate] && (
                    <Grid container spacing={1}>
                      {/* @ts-ignore - Omitir verificación de tipo para el parámetro slot */}
                      {selectedDoctor.availableSlots[selectedDate].map((slot) => (
                        <Grid item xs={4} key={slot.time}>
                          <Button
                            variant={selectedTime === slot.time ? 'contained' : 'outlined'}
                            fullWidth
                            disabled={!slot.isAvailable}
                            onClick={() => setSelectedTime(slot.time || null)}
                            sx={{
                              backgroundColor: selectedTime === slot.time ? '#4a90e2' : 'transparent',
                              '&:hover': {
                                backgroundColor: selectedTime === slot.time ? '#357abD' : '#f5f5f5',
                              },
                            }}
                          >
                            {slot.time}
                          </Button>
                        </Grid>
                      ))}
                    </Grid>
                  )}
                </Box>
              </DialogContent>
              <DialogActions>
                <Button onClick={() => setSelectedDoctor(null)}>Cancelar</Button>
                <Button
                  onClick={handleBookAppointment}
                  disabled={!selectedDate || !selectedTime}
                  variant="contained"
                  sx={{
                    backgroundColor: '#4a90e2',
                    '&:hover': {
                      backgroundColor: '#357abD',
                    },
                  }}
                >
                  Confirmar Cita
                </Button>
              </DialogActions>
            </>
          )}
        </Dialog>
      </Box>
    </Container>
  );
};

export default AppointmentSearch;