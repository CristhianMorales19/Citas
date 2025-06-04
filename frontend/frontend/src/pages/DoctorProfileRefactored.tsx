import React, { useState, useEffect } from 'react';
import * as Yup from 'yup';
import {
  Paper,
  Box,
  Tabs,
  Tab,
} from '@mui/material';
import { DoctorService } from '../services/api';
import { DoctorProfile, WeekSchedule } from '../types/appointment';
import { PageContainer, LoadingSpinner } from '../components/common';
import { FormContainer } from '../components/forms';
import { useFormState } from '../hooks/useFormState';
import {
  DoctorBasicInfo,
  DoctorScheduleConfig,
  DoctorPhotoUpload,
  DoctorWelcomeMessage,
} from '../components/doctor';

const validationSchema = Yup.object({
  specialty: Yup.string().required('La especialidad es requerida'),
  consultationCost: Yup.number()
    .required('El costo de consulta es requerido')
    .min(1, 'El costo debe ser mayor a 0'),
  description: Yup.string(),
  phone: Yup.string(),
  email: Yup.string().email('Email inválido'),
  address: Yup.string(),
});

const initialSchedule: WeekSchedule = {
  monday: { isAvailable: false, startTime: null, endTime: null },
  tuesday: { isAvailable: false, startTime: null, endTime: null },
  wednesday: { isAvailable: false, startTime: null, endTime: null },
  thursday: { isAvailable: false, startTime: null, endTime: null },
  friday: { isAvailable: false, startTime: null, endTime: null },
  saturday: { isAvailable: false, startTime: null, endTime: null },
  sunday: { isAvailable: false, startTime: null, endTime: null },
};

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <Box
      role="tabpanel"
      hidden={value !== index}
      id={`doctor-profile-tabpanel-${index}`}
      aria-labelledby={`doctor-profile-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </Box>
  );
}

function a11yProps(index: number) {
  return {
    id: `doctor-profile-tab-${index}`,
    'aria-controls': `doctor-profile-tabpanel-${index}`,
  };
}

const DoctorProfilePage: React.FC = () => {
  const [showWelcomeMessage, setShowWelcomeMessage] = useState(false);
  const [profile, setProfile] = useState<DoctorProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [schedule, setSchedule] = useState<WeekSchedule>(initialSchedule);
  const [tabValue, setTabValue] = useState(0);
  const [photoUploading, setPhotoUploading] = useState(false);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setLoading(true);
      const profileData = await DoctorService.getProfile();
      setProfile(profileData);

      // Determinar si mostrar mensaje de bienvenida
      if (profileData.status === 'APPROVED' && !profileData.profileConfigured) {
        setShowWelcomeMessage(true);
      }

      // Cargar horarios si existen
      if (profileData.schedule) {
        setSchedule(profileData.schedule);
      }
    } catch (error) {
      console.error('Error al cargar perfil:', error);
    } finally {
      setLoading(false);
    }
  };

  const { formik, loading: saving, error, success } = useFormState({
    initialValues: {
      specialty: profile?.specialty || '',
      consultationCost: profile?.consultationCost || '',
      description: profile?.description || '',
      phone: profile?.phone || '',
      email: profile?.email || '',
      address: profile?.address || '',
    },
    validationSchema,
    onSubmit: async (values) => {
      const updateData = {
        ...values,
        consultationCost: typeof values.consultationCost === 'string' 
          ? parseFloat(values.consultationCost) 
          : values.consultationCost,
        schedule,
      };
      await DoctorService.updateProfile(updateData);
      await loadProfile(); // Recargar perfil después de guardar
    },
  });

  const handlePhotoUpload = async (file: File) => {
    setPhotoUploading(true);
    try {
      // Create FormData for file upload
      const formData = new FormData();
      formData.append('file', file);
      await DoctorService.uploadProfilePhoto(formData);
      await loadProfile(); // Recargar para obtener la nueva URL de la foto
    } finally {
      setPhotoUploading(false);
    }
  };

  const handleScheduleChange = (newSchedule: WeekSchedule) => {
    setSchedule(newSchedule);
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  // No need for useEffect to update formik values since we're handling it differently

  if (loading) {
    return <LoadingSpinner message="Cargando perfil..." />;
  }

  return (
    <PageContainer title="Perfil Profesional" paper={false}>
      {showWelcomeMessage && (
        <DoctorWelcomeMessage onDismiss={() => setShowWelcomeMessage(false)} />
      )}

      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          aria-label="doctor profile tabs"
          variant="fullWidth"
        >
          <Tab label="Información Básica" {...a11yProps(0)} />
          <Tab label="Horarios" {...a11yProps(1)} />
          <Tab label="Foto de Perfil" {...a11yProps(2)} />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
          <FormContainer
            formik={formik}
            error={error}
            success={success}
            loading={saving}
            submitText="Guardar Perfil"
          >
            <DoctorBasicInfo formik={formik} />
          </FormContainer>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <DoctorScheduleConfig
            schedule={schedule}
            onScheduleChange={handleScheduleChange}
          />
          <Box sx={{ mt: 3, textAlign: 'center' }}>
            <FormContainer
              formik={formik}
              error={error}
              success={success}
              loading={saving}
              submitText="Guardar Horarios"
            >
              <></>
            </FormContainer>
          </Box>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <DoctorPhotoUpload
            currentPhotoUrl={profile?.profilePhotoUrl}
            onPhotoUpload={handlePhotoUpload}
            loading={photoUploading}
          />
        </TabPanel>
      </Paper>
    </PageContainer>
  );
};

export default DoctorProfilePage;
