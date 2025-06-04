import React from 'react';
// @ts-ignore - Workaround for React import issues
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Grid,
  Button,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  CircularProgress,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { es } from 'date-fns/locale';
import { addDays, subDays, format } from 'date-fns';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { appointmentService } from '../services/api';
import type { DoctorExtendedSchedule } from '../types/appointment';

const DoctorSchedulePage = () => {
  const { DoctorId } = useParams<{ DoctorId: string }>();
  const navigate = useNavigate();
  const [schedule, setSchedule] = useState<DoctorExtendedSchedule | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentDate, setCurrentDate] = useState(new Date());

  useEffect(() => {
    if (DoctorId) {
      loadSchedule();
    }
  }, [DoctorId, currentDate]);

  const loadSchedule = async () => {
    try {
      setLoading(true);
      setError(null);
      const startDate = format(currentDate, 'yyyy-MM-dd');
      const endDate = format(addDays(currentDate, 6), 'yyyy-MM-dd');
      const data = await appointmentService.getDoctorExtendedSchedule(DoctorId!, startDate, endDate);
      setSchedule(data);
    } catch (error) {
      setError('Error al cargar el horario del Doctor');
    } finally {
      setLoading(false);
    }
  };

  const handlePreviousWeek = () => {
    setCurrentDate(subDays(currentDate, 7));
  };

  const handleNextWeek = () => {
    setCurrentDate(addDays(currentDate, 7));
  };

  const handleBookAppointment = async (date: string, time: string) => {
    try {
      await appointmentService.bookAppointment(DoctorId!, date, time);
      navigate('/paciente/appointments');
    } catch (error) {
      setError('Error al agendar la cita');
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  if (!schedule) {
    return (
      <Container maxWidth="lg">
        <Alert severity="error">No se encontró el horario del Doctor</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#4a90e2' }}>
          Horario del Doctor {schedule.name}
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Button onClick={handlePreviousWeek}>Semana Anterior</Button>
          <Typography variant="h6">
            {format(currentDate, 'dd MMMM yyyy', { locale: es })} - {format(addDays(currentDate, 6), 'dd MMMM yyyy', { locale: es })}
          </Typography>
          <Button onClick={handleNextWeek}>Siguiente Semana</Button>
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Hora</TableCell>
                {schedule.schedule.map((day: any) => (
                  <TableCell key={day.date}>
                    {format(new Date(day.date), 'EEEE dd/MM', { locale: es })}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {schedule.schedule[0]?.timeSlots.map((slot: any, slotIndex: number) => (
                <TableRow key={slot.time}>
                  <TableCell>{slot.time}</TableCell>
                  {schedule.schedule.map((day: any) => (
                    <TableCell key={`${day.date}-${slot.time}`}>
                      {day.timeSlots[slotIndex]?.isAvailable ? (
                        <Button
                          variant="contained"
                          size="small"
                          color="primary"
                          onClick={() => handleBookAppointment(day.date, slot.time || '')}
                        >
                          Agendar
                        </Button>
                      ) : (
                        <Typography color="error">No disponible</Typography>
                      )}
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>
    </Container>
  );
};

export default DoctorSchedulePage; 