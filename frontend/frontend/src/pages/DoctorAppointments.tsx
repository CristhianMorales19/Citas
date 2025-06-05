import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Grid,
  TextField,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Alert,
  CircularProgress,
  IconButton,
  Tooltip,
  Chip,
  Card,
  CardContent,
  Divider,
  useTheme,
  alpha,
  TablePagination
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  AccessTime as AccessTimeIcon,
  Event as EventIcon,
  Person as PersonIcon,
  LocalHospital as LocalHospitalIcon,
  Note as NoteIcon,
  Info as InfoIcon,
  Refresh as RefreshIcon,
  FilterList as FilterListIcon,
  Search as SearchIcon
} from '@mui/icons-material';
import { format, parseISO, isToday, isBefore, addDays, isTomorrow, isPast } from 'date-fns';
import { es } from 'date-fns/locale';
import { DoctorService, appointmentService } from '../services/api';
import { Appointment } from '../types/appointment';

type AppointmentStatus = 'PENDIENTE' | 'AGENDADA' | 'CONFIRMADA' | 'COMPLETADA' | 'CANCELADA' | 'NO_ASISTIO';
type DateRange = 'all' | 'today' | 'tomorrow' | 'week' | 'past' | 'upcoming';
type DialogAction = 'view' | 'edit' | 'cancel' | 'complete';

// Función para formatear fechas en español
const formatDate = (dateString: string) => {
  try {
    const date = parseISO(dateString);
    return format(date, "EEEE d 'de' MMMM 'de' yyyy", { locale: es });
  } catch (error) {
    return 'Fecha no disponible';
  }
};

// Función para obtener el color según el estado de la cita
const getStatusColor = (status: AppointmentStatus) => {
  switch (status) {
    case 'CONFIRMADA':
      return 'success';
    case 'CANCELADA':
    case 'NO_ASISTIO':
      return 'error';
    case 'COMPLETADA':
      return 'info';
    case 'PENDIENTE':
    case 'AGENDADA':
      return 'warning';
    default:
      return 'default';
  }
};

// Función para obtener el texto legible del estado
const getStatusText = (status: AppointmentStatus) => {
  const statusMap: Record<AppointmentStatus, string> = {
    'PENDIENTE': 'Pendiente',
    'AGENDADA': 'Agendada',
    'CONFIRMADA': 'Confirmada',
    'COMPLETADA': 'Completada',
    'CANCELADA': 'Cancelada',
    'NO_ASISTIO': 'No asistió'
  };
  return statusMap[status] || status;
};

// Función para formatear la hora
const formatTime = (timeString: string) => {
  try {
    const [hours, minutes] = timeString.split(':');
    const date = new Date();
    date.setHours(parseInt(hours, 10), parseInt(minutes, 10), 0);
    return format(date, 'h:mm a', { locale: es });
  } catch (error) {
    return timeString;
  }
};

const DoctorAppointments: React.FC = () => {
  const theme = useTheme();
  
  // Estados principales
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [filteredAppointments, setFilteredAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);
  
  // Estados para filtros y búsqueda
  const [filterStatus, setFilterStatus] = useState<AppointmentStatus | ''>('');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [dateRange, setDateRange] = useState<DateRange>('all');
  
  // Estados para la cita seleccionada
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null);
  const [notes, setNotes] = useState<string>('');
  const [dialogOpen, setDialogOpen] = useState<boolean>(false);
  const [dialogAction, setDialogAction] = useState<DialogAction>('view');
  const [actionType, setActionType] = useState<DialogAction>('view');
  
  // Estados para paginación
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  
  // Estados para feedback
  const [success, setSuccess] = useState<string | null>(null);
  const [filterpaciente, setFilterpaciente] = useState<string>('');

  // Manejador seguro para el cambio de filtro de estado
  const handleFilterStatusChange = (value: string) => {
    const validStatuses: (AppointmentStatus | '')[] = [
      '', 
      'AGENDADA', 
      'COMPLETADA', 
      'CANCELADA', 
      'PENDIENTE', 
      'NO_ASISTIO'
    ];
    
    if (validStatuses.includes(value as any)) {
      setFilterStatus(value as AppointmentStatus | '');
    }
  };

  // Cargar citas al montar el componente
  useEffect(() => {
    loadAppointments();
  }, []);

  // Filtrar citas cuando cambian los filtros
  useEffect(() => {
    const filtered = appointments.filter((appointment: Appointment) => {
      // Filtrar por estado
      if (filterStatus !== '' && appointment.estado !== filterStatus) {
        return false;
      }
      
      // Filtrar por término de búsqueda
      if (searchTerm) {
        const searchLower = searchTerm.toLowerCase();
        const matchesPatient = appointment.paciente?.user?.name?.toLowerCase().includes(searchLower) || false;
        const matchesReason = appointment.motivoConsulta?.toLowerCase().includes(searchLower) || false;
        if (!matchesPatient && !matchesReason) {
          return false;
        }
      }
      
      // Filtrar por rango de fechas
      if (dateRange !== 'all') {
        const appointmentDate = new Date(`${appointment.fecha}T${appointment.horaInicio}`);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        switch (dateRange) {
          case 'today':
            return isToday(appointmentDate);
          case 'tomorrow':
            return isTomorrow(appointmentDate);
          case 'past':
            return isPast(appointmentDate);
          case 'upcoming':
            return !isPast(appointmentDate) && !isToday(appointmentDate);
          case 'week':
            const nextWeek = addDays(today, 7);
            return isBefore(appointmentDate, nextWeek) && !isPast(appointmentDate);
          default:
            return true;
        }
      }
      
      return true;
    });
    
    setFilteredAppointments(filtered);
    setPage(0); // Resetear a la primera página al cambiar los filtros
  }, [appointments, filterStatus, searchTerm, dateRange]);

  // Filtrar citas cuando cambian los filtros o las citas
  useEffect(() => {
    const filtered = appointments.filter(appointment => {
      // Filtrar por estado
      if (filterStatus && appointment.estado !== filterStatus) {
        return false;
      }
      
      // Filtrar por término de búsqueda (paciente o motivo de consulta)
      if (searchTerm) {
        const searchLower = searchTerm.toLowerCase();
        const pacienteNombre = `${appointment.paciente?.user?.name || ''}`.toLowerCase();
        const motivoConsulta = (appointment.motivoConsulta || '').toLowerCase();
        
        if (!pacienteNombre.includes(searchLower) && !motivoConsulta.includes(searchLower)) {
          return false;
        }
      }
      
      // Filtrar por rango de fechas
      if (dateRange !== 'all' && appointment.fecha) {
        const appointmentDate = parseISO(appointment.fecha);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        switch (dateRange) {
          case 'today':
            if (!isToday(appointmentDate)) return false;
            break;
          case 'tomorrow':
            if (!isTomorrow(appointmentDate)) return false;
            break;
          case 'week':
            const nextWeek = addDays(today, 7);
            if (appointmentDate < today || appointmentDate > nextWeek) return false;
            break;
          case 'past':
            if (!isPast(parseISO(`${appointment.fecha}T${appointment.horaFin || '23:59'}`))) return false;
            break;
          case 'upcoming':
            if (isPast(parseISO(`${appointment.fecha}T${appointment.horaInicio || '00:00'}`))) return false;
            break;
          default:
            break;
        }
      }
      
      return true;
    });
    
    setFilteredAppointments(filtered);
    setPage(0); // Resetear a la primera página al cambiar los filtros
  }, [appointments, filterStatus, searchTerm, dateRange]);

  // Cargar las citas desde el servicio
  const loadAppointments = async () => {
    try {
      setLoading(true);
      setError(null);
      setInfo('Cargando citas...');
      
      const appointments = await DoctorService.getAppointments();
      
      // Ordenar citas por fecha y hora (más recientes primero)
      const sortedAppointments = [...appointments].sort((a, b) => {
        const dateA = new Date(`${a.fecha}T${a.horaInicio}`).getTime();
        const dateB = new Date(`${b.fecha}T${b.horaInicio}`).getTime();
        return dateA - dateB;
      });
      
      setAppointments(sortedAppointments);
      setInfo(sortedAppointments.length === 0 ? 'No hay citas programadas.' : null);
    } catch (error: any) {
      console.error('Error al cargar las citas:', error);
      setError(error.message || 'Error al cargar las citas. Por favor, intente de nuevo.');
    } finally {
      setLoading(false);
    }
  };
  
  // Manejar cambio de página
  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  // Manejar cambio de filas por página
  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };
  
  // Abrir diálogo para ver/editar/cancelar cita
  const handleOpenDialog = (appointment: Appointment, type: 'view' | 'edit' | 'cancel' | 'complete' = 'view') => {
    setSelectedAppointment(appointment);
    setActionType(type);
    setNotes(appointment.notas || '');
    setDialogOpen(true);
  };
  
  // Cerrar diálogo
  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedAppointment(null);
    setNotes('');
  };
  
  // Confirmar acción (cancelar/completar cita)
  const confirmAction = async () => {
    if (!selectedAppointment) return;
    
    try {
      setLoading(true);
      
      if (actionType === 'cancel') {
        await appointmentService.cancelAppointment(selectedAppointment.id.toString());
        setSuccess('Cita cancelada correctamente');
      } else if (actionType === 'complete') {
        // Aquí iría la lógica para marcar como completada
        // await DoctorService.completeAppointment(selectedAppointment.id.toString(), { notas });
        setSuccess('Cita marcada como completada');
      } else if (actionType === 'edit') {
        // Aquí iría la lógica para actualizar las notas
        // await DoctorService.updateAppointment(selectedAppointment.id.toString(), { notas });
        setSuccess('Notas actualizadas correctamente');
      }
      
      // Recargar citas
      await loadAppointments();
      handleCloseDialog();
    } catch (error: any) {
      console.error('Error al actualizar la cita:', error);
      setError(error.message || 'Error al actualizar la cita');
    } finally {
      setLoading(false);
    }
  };
  
  // Datos para la paginación
  const paginatedAppointments = filteredAppointments.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );
  
  // Contadores para estadísticas
  const stats = {
    total: appointments.length,
    confirmadas: appointments.filter(a => a.estado === 'CONFIRMADA' || a.estado === 'AGENDADA').length,
    pendientes: appointments.filter(a => a.estado === 'PENDIENTE').length,
    canceladas: appointments.filter(a => a.estado === 'CANCELADA' || a.estado === 'NO_ASISTIO').length,
    completadas: appointments.filter(a => a.estado === 'COMPLETADA').length,
  };

  const handleStatusChange = async (appointmentId: string, newStatus: AppointmentStatus) => {
    try {
      setError(null);
      setSuccess(null);
      setLoading(true);
      
      // Actualizar el estado de la cita usando el servicio de citas
      await DoctorService.updateAppointment(appointmentId, { estado: newStatus });
      
      // Recargar las citas
      await loadAppointments();
      
      setSuccess(`Cita ${newStatus.toLowerCase()} correctamente`);
    } catch (error: any) {
      console.error('Error al actualizar el estado de la cita:', error);
      setError(error.message || 'Error al actualizar el estado de la cita');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveNotes = async () => {
    if (!selectedAppointment) return;

    try {
      setError(null);
      setSuccess(null);
      await DoctorService.updateAppointment(selectedAppointment.id, { notas: notes });
      setSuccess('Notas guardadas correctamente');
      setDialogOpen(false);
      
      // Actualizar la lista de citas
      const updatedAppointments = appointments.map(appt => 
        appt.id === selectedAppointment.id 
          ? { ...appt, notas: notes }
          : appt
      );
      setAppointments(updatedAppointments);
    } catch (error) {
      setError('Error al guardar las notas');
      console.error('Error saving notes:', error);
    }
  };

  // Manejador para ver los detalles de una cita
  const handleViewAppointment = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setNotes(appointment.notas || '');
    setDialogAction('view');
    setDialogOpen(true);
  };

  // Manejador para editar una cita
  const handleEditAppointment = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setNotes(appointment.notas || '');
    setDialogAction('edit');
    setDialogOpen(true);
  };

  // Calcular filas para la página actual
  const startIndex = page * rowsPerPage;
  const endIndex = startIndex + rowsPerPage;
  const currentPageAppointments = filteredAppointments.slice(startIndex, endIndex);

  // Contadores de estado
  const statusCounts = {
    total: appointments.length,
    pendientes: appointments.filter(a => a.estado === 'PENDIENTE').length,
    agendadas: appointments.filter(a => a.estado === 'AGENDADA').length,
    confirmadas: appointments.filter(a => a.estado === 'CONFIRMADA').length,
    completadas: appointments.filter(a => a.estado === 'COMPLETADA').length,
    canceladas: appointments.filter(a => a.estado === 'CANCELADA').length,
    noAsistio: appointments.filter(a => a.estado === 'NO_ASISTIO').length,
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
          Gestión de Citas
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

        <Paper elevation={3} sx={{ p: 3, mb: 4 }}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Estado</InputLabel>
                <Select
                  value={filterStatus}
                  label="Estado"
                  onChange={(e) => handleFilterStatusChange(e.target.value)}
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="AGENDADA">Agendada</MenuItem>
                  <MenuItem value="COMPLETADA">Completada</MenuItem>
                  <MenuItem value="CANCELADA">Cancelada</MenuItem>
                  <MenuItem value="PENDIENTE">Pendiente</MenuItem>
                  <MenuItem value="NO_ASISTIO">No Asistió</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Buscar por paciente"
                value={filterpaciente}
                onChange={(e) => setFilterpaciente(e.target.value)}
              />
            </Grid>
          </Grid>
        </Paper>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Fecha</TableCell>
                <TableCell>Hora</TableCell>
                <TableCell>Paciente</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredAppointments.map((appointment) => (
                <TableRow key={appointment.id}>
                  <TableCell>{appointment.fecha ? formatDate(appointment.fecha) : 'N/A'}</TableCell>
                  <TableCell>{appointment.horaInicio ? formatTime(appointment.horaInicio) : 'N/A'}</TableCell>
                  <TableCell>{appointment.paciente?.user?.name || 'N/A'}</TableCell>
                  <TableCell>
                    <Typography
                      sx={{
                        color: getStatusColor(appointment.estado as AppointmentStatus),
                        fontWeight: 'bold',
                      }}
                    >
                      {getStatusText(appointment.estado as AppointmentStatus)}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      {(appointment.estado === 'AGENDADA' || appointment.estado === 'PENDIENTE') && (
                        <>
                          <Button
                            size="small"
                            variant="contained"
                            color="success"
                            onClick={() => handleStatusChange(appointment.id, 'COMPLETADA')}
                          >
                            Completar
                          </Button>
                          <Button
                            size="small"
                            variant="contained"
                            color="error"
                            onClick={() => handleStatusChange(appointment.id, 'CANCELADA')}
                          >
                            Cancelar
                          </Button>
                        </>
                      )}
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => handleViewAppointment(appointment)}
                      >
                        Ver
                      </Button>
                      {(appointment.estado === 'AGENDADA' || appointment.estado === 'PENDIENTE') && (
                        <Button
                          size="small"
                          variant="outlined"
                          color="primary"
                          onClick={() => handleEditAppointment(appointment)}
                        >
                          Editar
                        </Button>
                      )}
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={filteredAppointments.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            labelRowsPerPage="Filas por página:"
          />
        </TableContainer>

        {/* Dialog para ver/editar cita */}
        <Dialog
          open={dialogOpen}
          onClose={() => setDialogOpen(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            {dialogAction === 'view' ? 'Detalles de la cita' : 'Editar cita'}
          </DialogTitle>
          <DialogContent>
            {selectedAppointment && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle1" gutterBottom>
                  <strong>Paciente:</strong> {selectedAppointment.paciente?.user?.name || 'N/A'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                  <strong>Fecha:</strong> {selectedAppointment.fecha ? formatDate(selectedAppointment.fecha) : 'N/A'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                  <strong>Hora:</strong> {selectedAppointment.horaInicio ? formatTime(selectedAppointment.horaInicio) : 'N/A'} - {selectedAppointment.horaFin ? formatTime(selectedAppointment.horaFin) : 'N/A'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                  <strong>Estado:</strong> {getStatusText(selectedAppointment.estado as AppointmentStatus)}
                </Typography>
                <Typography variant="body1" gutterBottom>
                  <strong>Motivo:</strong> {selectedAppointment.motivoConsulta || 'No especificado'}
                </Typography>
                <TextField
                  fullWidth
                  label="Notas"
                  multiline
                  rows={4}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  margin="normal"
                  disabled={dialogAction === 'view'}
                />
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cerrar</Button>
            {dialogAction === 'edit' && (
              <Button onClick={handleSaveNotes} variant="contained" color="primary">
                Guardar
              </Button>
            )}
          </DialogActions>
        </Dialog>
      </Box>
    </Container>
  );
};

export default DoctorAppointments; 