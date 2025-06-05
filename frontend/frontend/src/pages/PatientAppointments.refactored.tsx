import React, { useState, useEffect } from 'react';
import { Appointment } from '../types/appointment';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { appointmentService } from '../services/api';
import {
  PageContainer,
  DataTable,
  DataTableColumn,
  DataTableAction,
  StatusChip,
  SearchAndFilter,
  FilterField,
  ConfirmDialog,
  ErrorMessage,
} from '../components/common';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
} from '@mui/material';
import NoteIcon from '@mui/icons-material/Note';
import CancelIcon from '@mui/icons-material/Cancel';

const PatientAppointments: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [filteredAppointments, setFilteredAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Filter states
  const [searchValue, setSearchValue] = useState<string>('');
  const [filterValues, setFilterValues] = useState<Record<string, any>>({
    status: '',
    doctor: '',
  });

  // Dialog states
  const [openNotesDialog, setOpenNotesDialog] = useState<boolean>(false);
  const [openCancelDialog, setOpenCancelDialog] = useState<boolean>(false);
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null);
  const [notes, setNotes] = useState<string>('');
  const [cancelLoading, setCancelLoading] = useState<boolean>(false);

  useEffect(() => {
    loadAppointments();
  }, []);

  useEffect(() => {
    filterAppointments();
  }, [appointments, searchValue, filterValues]);

  const loadAppointments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await appointmentService.getpacienteAppointments();
      setAppointments(data);
    } catch (error) {
      console.error('Error al cargar las citas:', error);
      setError('No se pudieron cargar las citas. Por favor, intente de nuevo más tarde.');
    } finally {
      setLoading(false);
    }
  };

  const filterAppointments = () => {
    if (!appointments || appointments.length === 0) {
      setFilteredAppointments([]);
      return;
    }

    let filtered = [...appointments];

    // Search filter
    if (searchValue) {
      filtered = filtered.filter(app => {
        const doctorName = app.medico?.user?.name || '';
        const specialty = app.medico?.especialidad || '';
        return (
          doctorName.toLowerCase().includes(searchValue.toLowerCase()) ||
          specialty.toLowerCase().includes(searchValue.toLowerCase())
        );
      });
    }

    // Status filter
    if (filterValues.status) {
      filtered = filtered.filter(app => app.estado === filterValues.status);
    }

    // Doctor filter
    if (filterValues.doctor) {
      filtered = filtered.filter(app => {
        const doctorName = app.medico?.user?.name || '';
        return doctorName.toLowerCase().includes(filterValues.doctor.toLowerCase());
      });
    }

    setFilteredAppointments(filtered);
  };

  const handleFilterChange = (name: string, value: any) => {
    setFilterValues(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleClearFilters = () => {
    setSearchValue('');
    setFilterValues({
      status: '',
      doctor: '',
    });
  };

  const handleOpenNotes = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setNotes(appointment.notas || '');
    setOpenNotesDialog(true);
  };

  const handleOpenCancel = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setOpenCancelDialog(true);
  };

  const handleSaveNotes = async () => {
    if (!selectedAppointment) return;

    try {
      // await appointmentService.addNotes(selectedAppointment.id, notes);
      setSuccess('Notas guardadas correctamente');
      setOpenNotesDialog(false);
      loadAppointments();
    } catch (error) {
      setError('Error al guardar las notas');
    }
  };

  const handleCancelAppointment = async () => {
    if (!selectedAppointment) return;

    try {
      setCancelLoading(true);
      await appointmentService.cancelAppointment(selectedAppointment.id);
      setSuccess('Cita cancelada correctamente');
      setOpenCancelDialog(false);
      loadAppointments();
    } catch (error) {
      setError('Error al cancelar la cita');
    } finally {
      setCancelLoading(false);
    }
  };

  // Table columns definition
  const columns: DataTableColumn<Appointment>[] = [
    {
      id: 'medico',
      label: 'Doctor',
      format: (value, row) => row.doctorName || row.medico?.user?.name || 'No disponible',
    },
    {
      id: 'specialty',
      label: 'Especialidad',
      format: (value, row) => row.medico?.especialidad || 'No disponible',
    },
    {
      id: 'fecha',
      label: 'Fecha',
      format: (value) => format(new Date(value), 'dd/MM/yyyy', { locale: es }),
    },
    {
      id: 'hora',
      label: 'Hora',
    },
    {
      id: 'estado',
      label: 'Estado',
      format: (value) => <StatusChip status={value} />,
    },
    {
      id: 'notas',
      label: 'Notas',
      format: (value) => value ? 'Sí' : 'No',
    },
  ];

  // Table actions definition
  const actions: DataTableAction<Appointment>[] = [
    {
      label: 'Ver/Editar Notas',
      icon: <NoteIcon />,
      onClick: handleOpenNotes,
      color: 'primary',
    },
    {
      label: 'Cancelar Cita',
      icon: <CancelIcon />,
      onClick: handleOpenCancel,
      color: 'error',
      disabled: (row) => row.estado === 'CANCELADA' || row.estado === 'COMPLETADA',
    },
  ];

  // Filter fields definition
  const filterFields: FilterField[] = [
    {
      name: 'status',
      label: 'Estado',
      type: 'select',
      options: [
        { value: 'pendiente', label: 'Pendiente' },
        { value: 'confirmada', label: 'Confirmada' },
        { value: 'completada', label: 'Completada' },
        { value: 'cancelada', label: 'Cancelada' },
      ],
    },
    {
      name: 'doctor',
      label: 'Doctor',
      type: 'text',
      placeholder: 'Buscar por doctor...',
    },
  ];

  return (
    <PageContainer
      title="Mis Citas"
      breadcrumbs={[
        { label: 'Dashboard', path: '/paciente' },
        { label: 'Mis Citas' },
      ]}
    >
      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}
      {success && <ErrorMessage message={success} variant="filled" onClose={() => setSuccess(null)} />}

      <SearchAndFilter
        searchValue={searchValue}
        onSearchChange={setSearchValue}
        searchPlaceholder="Buscar por doctor o especialidad..."
        filters={filterFields}
        filterValues={filterValues}
        onFilterChange={handleFilterChange}
        onClearFilters={handleClearFilters}
      />

      <DataTable
        columns={columns}
        data={filteredAppointments}
        loading={loading}
        error={error}
        actions={actions}
        emptyMessage="No tienes citas programadas"
        getRowId={(row) => row.id}
      />

      {/* Notes Dialog */}
      <Dialog
        open={openNotesDialog}
        onClose={() => setOpenNotesDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          {selectedAppointment && `Notas - Dr. ${selectedAppointment.medico?.user?.name}`}
        </DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Notas de la cita"
            fullWidth
            multiline
            rows={4}
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Escribe tus notas sobre esta cita..."
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenNotesDialog(false)}>
            Cancelar
          </Button>
          <Button onClick={handleSaveNotes} variant="contained">
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Cancel Confirmation Dialog */}
      <ConfirmDialog
        open={openCancelDialog}
        onClose={() => setOpenCancelDialog(false)}
        onConfirm={handleCancelAppointment}
        title="¿Cancelar cita?"
        message={
          selectedAppointment && selectedAppointment.fecha
            ? `¿Estás seguro de que deseas cancelar la cita con Dr. ${selectedAppointment.doctorName || selectedAppointment.medico?.user?.name || 'Doctor'} del ${format(new Date(selectedAppointment.fecha), 'dd/MM/yyyy', { locale: es })} a las ${selectedAppointment.horaInicio}?`
            : ''
        }
        confirmText="Cancelar Cita"
        confirmColor="error"
        loading={cancelLoading}
      />
    </PageContainer>
  );
};

export default PatientAppointments;
