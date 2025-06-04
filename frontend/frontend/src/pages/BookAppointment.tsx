import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import {
  Box,
  Grid,
  Button,
} from '@mui/material';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { appointmentService } from '../services/api';
import { useAutenticacion } from '../contexts/AuthContext';
import {
  PageContainer,
  InfoCard,
  ActionButton,
  ErrorMessage,
  LoadingSpinner,
} from '../components/common';
import PersonIcon from '@mui/icons-material/Person';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn';
import MedicalServicesIcon from '@mui/icons-material/MedicalServices';
import EventIcon from '@mui/icons-material/Event';
import AccessTimeIcon from '@mui/icons-material/AccessTime';

const BookAppointment: React.FC = () => {
  const { DoctorId } = useParams<{ DoctorId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const { estaAutenticado } = useAutenticacion();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [DoctorInfo, setDoctorInfo] = useState<any>(null);

  const searchParams = new URLSearchParams(location.search);
  const date = searchParams.get('date');
  const time = searchParams.get('time');

  const loadDoctorInfo = React.useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await appointmentService.getDoctorProfile(DoctorId!);
      setDoctorInfo(data);
    } catch (error) {
      setError('Error al cargar la información del Doctor');
    } finally {
      setLoading(false);
    }
  }, [DoctorId]);

  useEffect(() => {
    if (!estaAutenticado) {
      navigate('/login', { state: { from: location.pathname + location.search } });
      return;
    }

    if (!DoctorId || !date || !time) {
      navigate('/');
      return;
    }

    loadDoctorInfo();
  }, [DoctorId, date, time, estaAutenticado, navigate, location.pathname, location.search, loadDoctorInfo]);

  const handleConfirmAppointment = async () => {
    try {
      setLoading(true);
      setError(null);
      await appointmentService.bookAppointment(DoctorId!, date!, time!);
      setSuccess('Cita agendada correctamente');
      // No redirect. Show confirmation message and stay on page.
    } catch (error) {
      setError('Error al agendar la cita');
    } finally {
      setLoading(false);
    }
  };

  if (loading && !DoctorInfo) {
    return (
      <PageContainer title="Confirmar Cita">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
          <LoadingSpinner size={48} />
        </Box>
      </PageContainer>
    );
  }

  if (!DoctorInfo) {
    return (
      <PageContainer title="Confirmar Cita">
        <ErrorMessage message="No se encontró la información del Doctor" />
      </PageContainer>
    );
  }

  const doctorFields = [
    {
      label: 'Doctor',
      value: `Dr. ${DoctorInfo.name}`,
      icon: <PersonIcon />,
    },
    {
      label: 'Especialidad',
      value: DoctorInfo.specialty,
      icon: <MedicalServicesIcon />,
    },
    {
      label: 'Ubicación',
      value: DoctorInfo.location,
      icon: <LocationOnIcon />,
    },
    {
      label: 'Costo de consulta',
      value: `$${DoctorInfo.consultationCost}`,
      icon: <MonetizationOnIcon />,
    },
  ];

  const appointmentFields = [
    {
      label: 'Fecha',
      value: format(new Date(date!), 'EEEE dd/MM/yyyy', { locale: es }),
      icon: <EventIcon />,
    },
    {
      label: 'Hora',
      value: time,
      icon: <AccessTimeIcon />,
    },
  ];

  return (
    <PageContainer 
      title="Confirmar Cita"
      breadcrumbs={[
        { label: 'Inicio', path: '/' },
        { label: 'Confirmar Cita' },
      ]}
    >
      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}
      {success && (
        <>
          <ErrorMessage message={success} />
          <Box mt={2}>
            <Button variant="contained" color="primary" onClick={() => navigate('/paciente/appointments')}>
              Ir a Mis Citas
            </Button>
          </Box>
        </>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <InfoCard
            title="Información del Doctor"
            fields={doctorFields}
            elevation={2}
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <InfoCard
            title="Detalles de la Cita"
            fields={appointmentFields}
            elevation={2}
            actions={
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', width: '100%' }}>
                <Button
                  variant="outlined"
                  onClick={() => navigate('/')}
                  disabled={loading}
                >
                  Cancelar
                </Button>
                <ActionButton
                  variant="contained"
                  onClick={handleConfirmAppointment}
                  loading={loading}
                  color="primary"
                >
                  Confirmar Cita
                </ActionButton>
              </Box>
            }
          />
        </Grid>
      </Grid>
    </PageContainer>
  );
};

export default BookAppointment;