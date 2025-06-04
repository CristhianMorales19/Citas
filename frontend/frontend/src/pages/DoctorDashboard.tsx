import React, { useState, useEffect } from 'react';
import { Grid, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import {
  PageContainer,
  StatCard,
  InfoCard,
  ActionButton,
  LoadingSpinner,
  ErrorMessage,
  FlexBox,
} from '../components/common';
import { useAutenticacion } from '../contexts/AuthContext';
import PersonIcon from '@mui/icons-material/Person';
import EventIcon from '@mui/icons-material/Event';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import ScheduleIcon from '@mui/icons-material/Schedule';

interface DashboardStats {
  totalAppointments: number;
  todayAppointments: number;
  weeklyAppointments: number;
  pendingAppointments: number;
}

const DoctorDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { usuario } = useAutenticacion();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboardStats();
  }, []);

  const loadDashboardStats = async () => {
    try {
      setLoading(true);
      setError(null);
      // Simulación de datos de estadísticas
      setStats({
        totalAppointments: 45,
        todayAppointments: 8,
        weeklyAppointments: 23,
        pendingAppointments: 3,
      });
    } catch (error) {
      console.error('Error loading dashboard stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <PageContainer title="Panel de Doctor">
        <FlexBox justify="center" align="center" style={{ minHeight: '200px' }}>
          <LoadingSpinner size={48} />
        </FlexBox>
      </PageContainer>
    );
  }

  if (error) {
    return (
      <PageContainer title="Panel de Doctor">
        <ErrorMessage message={error} onClose={() => setError(null)} />
      </PageContainer>
    );
  }

  const quickActions = [
    {
      title: 'Gestionar Perfil',
      description: 'Actualiza tu información profesional, especialidad y disponibilidad',
      action: () => navigate('/Doctor/profile'),
      icon: <PersonIcon />,
      color: 'primary' as const,
    },
    {
      title: 'Ver Citas',
      description: 'Gestiona tus citas programadas y revisa el calendario',
      action: () => navigate('/Doctor/appointments'),
      icon: <EventIcon />,
      color: 'secondary' as const,
    },
    {
      title: 'Configurar Horarios',
      description: 'Establece tu disponibilidad y horarios de atención',
      action: () => navigate('/Doctor/availability'),
      icon: <ScheduleIcon />,
      color: 'info' as const,
    },
  ];

  return (
    <PageContainer
      title={`¡Bienvenido, Dr. ${usuario?.name || 'Doctor'}!`}
      subtitle="Panel de control para gestionar tu práctica médica"
      breadcrumbs={[
        { label: 'Dashboard', path: '/Doctor' },
      ]}
    >
      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Citas Hoy"
            value={stats?.todayAppointments || 0}
            icon={<CalendarTodayIcon />}
            color="primary"
            onClick={() => navigate('/Doctor/appointments?filter=today')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Esta Semana"
            value={stats?.weeklyAppointments || 0}
            icon={<EventIcon />}
            color="secondary"
            trend={{
              value: 12,
              isPositive: true,
              period: 'vs semana anterior',
            }}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Pendientes"
            value={stats?.pendingAppointments || 0}
            icon={<ScheduleIcon />}
            color="warning"
            onClick={() => navigate('/Doctor/appointments?filter=pending')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total del Mes"
            value={stats?.totalAppointments || 0}
            icon={<TrendingUpIcon />}
            color="success"
            trend={{
              value: 8,
              isPositive: true,
              period: 'vs mes anterior',
            }}
          />
        </Grid>
      </Grid>

      {/* Quick Actions */}
      <Grid container spacing={3}>
        {quickActions.map((action, index) => (
          <Grid item xs={12} md={4} key={index}>
            <InfoCard
              title={action.title}
              fields={[
                {
                  label: 'Descripción',
                  value: action.description,
                  fullWidth: true,
                },
              ]}
              avatar={action.icon}
              elevation={2}
              actions={
                <Box sx={{ width: '100%' }}>
                  <ActionButton
                    fullWidth
                    variant="contained"
                    color={action.color}
                    onClick={action.action}
                    startIcon={action.icon}
                  >
                    Acceder
                  </ActionButton>
                </Box>
              }
            />
          </Grid>
        ))}
      </Grid>
    </PageContainer>
  );
};

export default DoctorDashboard;