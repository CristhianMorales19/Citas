import React, { useState, useEffect } from 'react';
import { Appointment } from '../types/appointment';
import { 
  Container,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Box,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Alert,
  CircularProgress
} from '@mui/material';
import { StatusChip } from '../components/common';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { appointmentService } from '../services/api';
import NoteIcon from '@mui/icons-material/Note';
import CancelIcon from '@mui/icons-material/Cancel';

const PatientAppointments: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [filteredAppointments, setFilteredAppointments] = useState<Appointment[]>([]);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [doctorFilter, setdoctorFilter] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState<boolean>(false);
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null);
  const [notes, setNotes] = useState<string>('');

  useEffect(() => {
    loadAppointments();
  }, []);

  useEffect(() => {
    if (appointments && appointments.length > 0) {
      filterAppointments();
    }
  }, [appointments, statusFilter, doctorFilter]);

  const loadAppointments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await appointmentService.getpacienteAppointments();
      setAppointments(data);
      setFilteredAppointments(data);
    } catch (error) {
      console.error('Error al cargar las citas:', error);
      setError('No se pudieron cargar las citas. Por favor, intente de nuevo más tarde.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAppointments();
  }, []);

  useEffect(() => {
    if (appointments && appointments.length > 0) {
      filterAppointments();
    }
  }, [appointments, statusFilter, doctorFilter]);

  const filterAppointments = () => {
    if (!appointments || appointments.length === 0) {
      setFilteredAppointments([]);
      return;
    }

    let filtered = [...appointments];

    // Aplicar filtros
    if (statusFilter) {
      filtered = filtered.filter(app => app.estado === statusFilter);
    }

    if (doctorFilter) {
      filtered = filtered.filter(app => {
        const doctorName = app.medico?.user?.name || '';
        return doctorName.toLowerCase().includes(doctorFilter.toLowerCase());
      });
    }

    // Ordenar por fecha y hora (más recientes primero)
    filtered.sort((a, b) => {
      const dateA = new Date(a.fecha + 'T' + (a.horaInicio || '00:00:00'));
      const dateB = new Date(b.fecha + 'T' + (b.horaInicio || '00:00:00'));
      return dateB.getTime() - dateA.getTime();
    });

    setFilteredAppointments(filtered);
  };

  const handleCancelAppointment = async (appointmentId: string) => {
    if (window.confirm('¿Está seguro de que desea cancelar esta cita?')) {
      try {
        setError(null);
        setSuccess(null);
        await appointmentService.cancelAppointment(appointmentId);
        setSuccess('Cita cancelada correctamente');
        loadAppointments();
      } catch (error) {
        setError('Error al cancelar la cita');
      }
    }
  };

  const handleOpenNotes = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setNotes(appointment.notas || '');
    setOpenDialog(true);
  };

  const handleCloseNotes = () => {
    setOpenDialog(false);
    setSelectedAppointment(null);
    setNotes('');
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#4a90e2' }}>
          Mis Citas
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

        <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Estado</InputLabel>
                <Select
                  value={statusFilter}
                  label="Estado"
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="PENDIENTE">Pendiente</MenuItem>
                  <MenuItem value="AGENDADA">Agendada</MenuItem>
                  <MenuItem value="CONFIRMADA">Confirmada</MenuItem>
                  <MenuItem value="COMPLETADA">Completada</MenuItem>
                  <MenuItem value="CANCELADA">Cancelada</MenuItem>
                  <MenuItem value="NO_ASISTIO">No asistió</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Buscar por médico"
                value={doctorFilter}
                onChange={(e) => setdoctorFilter(e.target.value)}
              />
            </Grid>
          </Grid>
        </Paper>

        <Paper elevation={3} sx={{ p: 3 }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Fecha</TableCell>
                  <TableCell>Hora</TableCell>
                  <TableCell>Médico</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell>Acciones</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredAppointments.map((appointment) => {
                  const doctorName = appointment.doctorName || appointment.medico?.user?.name || 'Médico no disponible';
                  const appointmentDate = appointment.fecha ? new Date(appointment.fecha) : new Date();
                  const timeString = appointment.horaInicio || '--:--';
                  
                  return (
                    <TableRow key={appointment.id}>
                      <TableCell>
                        {format(appointmentDate, 'dd/MM/yyyy', { locale: es })}
                      </TableCell>
                      <TableCell>{appointment.horaInicio ? appointment.horaInicio.substring(0,5) : '--:--'}</TableCell>
                      <TableCell>{doctorName}</TableCell>
                      <TableCell>
                        <StatusChip
                          status={appointment.estado || 'PENDIENTE'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          {appointment.estado === 'COMPLETADA' && appointment.notas && (
                            <IconButton
                              onClick={() => handleOpenNotes(appointment)}
                              color="primary"
                            >
                              <NoteIcon />
                            </IconButton>
                          )}
                          {(appointment.estado === 'PENDIENTE' || appointment.estado === 'AGENDADA') && (
                            <IconButton
                              onClick={() => handleCancelAppointment(appointment.id)}
                              color="error"
                            >
                              <CancelIcon />
                            </IconButton>
                          )}
                        </Box>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>

        <Dialog open={openDialog} onClose={handleCloseNotes}>
          <DialogTitle>Notas de la Cita</DialogTitle>
          <DialogContent>
            <TextField
              fullWidth
              multiline
              rows={4}
              value={notes}
              disabled
              margin="normal"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseNotes}>Cerrar</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Container>
  );
};

export default PatientAppointments;