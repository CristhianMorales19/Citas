import React, { useState, useEffect } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Grid,
  FormControlLabel,
  Switch,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress,
  Card,
  CardContent,
  CardHeader,
  Divider,
  Tooltip,
  IconButton,
  Avatar,
  Tabs,
  Tab,
} from '@mui/material';
import { TimePicker } from '@mui/x-date-pickers/TimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { es } from 'date-fns/locale';
import { DoctorService } from '../services/api';
import { useAutenticacion } from '../contexts/AuthContext';
import PhotoCamera from '@mui/icons-material/PhotoCamera';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import InfoIcon from '@mui/icons-material/Info';
import SaveIcon from '@mui/icons-material/Save';

const validationSchema = Yup.object({
  specialty: Yup.string().required('La especialidad es requerida'),
  consultationCost: Yup.number()
    .required('El costo de consulta es requerido')
    .min(0, 'El costo debe ser mayor o igual a 0'),
  location: Yup.string().required('La ubicación es requerida'),
  appointmentDuration: Yup.number()
    .required('La duración de la cita es requerida')
    .min(15, 'La duración mínima es de 15 minutos')
    .max(120, 'La duración máxima es de 120 minutos'),
  presentation: Yup.string().required('La presentación es requerida'),
  photoUrl: Yup.string().nullable(),
});

// Duración de citas predefinidas en minutos
const appointmentDurations = [
  { value: 15, label: '15 minutos' },
  { value: 20, label: '20 minutos' },
  { value: 30, label: '30 minutos' },
  { value: 45, label: '45 minutos' },
  { value: 60, label: '1 hora' },
  { value: 90, label: '1 hora 30 minutos' },
  { value: 120, label: '2 horas' },
];

// Especialidades médicas comunes
const medicalSpecialties = [
  'Cardiología',
  'Dermatología',
  'Endocrinología',
  'Gastroenterología',
  'Geriatría',
  'Ginecología',
  'Hematología',
  'Medicina Familiar',
  'Medicina General',
  'Medicina Interna',
  'Nefrología',
  'Neumología',
  'Neurología',
  'Nutrición',
  'Oftalmología',
  'Oncología',
  'Otorrinolaringología',
  'Pediatría',
  'Psicología',
  'Psiquiatría',
  'Reumatología',
  'Traumatología',
  'Urología',
  // Puedes agregar más especialidades aquí
];

const initialSchedule = {
  monday: { isAvailable: false, startTime: null, endTime: null },
  tuesday: { isAvailable: false, startTime: null, endTime: null },
  wednesday: { isAvailable: false, startTime: null, endTime: null },
  thursday: { isAvailable: false, startTime: null, endTime: null },
  friday: { isAvailable: false, startTime: null, endTime: null },
  saturday: { isAvailable: false, startTime: null, endTime: null },
  sunday: { isAvailable: false, startTime: null, endTime: null },
};

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <Box
      role="tabpanel"
      hidden={value !== index}
      id={`doctor-profile-tabpanel-${index}`}
      aria-labelledby={`doctor-profile-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </Box>
  );
}

function a11yProps(index) {
  return {
    id: `doctor-profile-tab-${index}`,
    'aria-controls': `doctor-profile-tabpanel-${index}`,
  };
}

const DoctorProfilePage = () => {
  // Get user from authentication context
  const { usuario } = useAutenticacion();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [tabValue, setTabValue] = useState(0);
  const [schedule, setSchedule] = useState(initialSchedule);
  const [selectedFile, setSelectedFile] = useState(null);
  const [profileImage, setProfileImage] = useState(null);
  const [uploading, setUploading] = useState(false);

  const formik = useFormik({
    initialValues: {
      specialty: '',
      consultationCost: 0,
      location: '',
      appointmentDuration: 30,
      presentation: '',
      photoUrl: '',
      // weeklySchedule se usa en lugar de schedule para compatibilidad con la API actual
      weeklySchedule: []
    },
    validationSchema,
    onSubmit: async (values) => {
      try {
        setError(null);
        setSuccess(null);
        
        // Si hay un archivo seleccionado, primero sube la imagen
        if (selectedFile) {
          setUploading(true);
          try {
            const formData = new FormData();
            formData.append('file', selectedFile);
            
            // Usar el servicio DoctorService para subir la foto de perfil
            const response = await DoctorService.uploadProfilePhoto(formData);
            values.photoUrl = response.url;
          } catch (uploadError) {
            console.error('Error en la subida:', uploadError);
            // Si falla, usar URL temporal solo para la vista previa
            values.photoUrl = URL.createObjectURL(selectedFile);
            setError('Error al subir la imagen de perfil');
            setUploading(false);
            return;
          } finally {
            setUploading(false);
          }
        }
        
        // Convertir el horario al formato que espera la API actual
        // La API espera una lista de objetos con día, hora inicio y hora fin
        const weeklyScheduleArray = Object.entries(schedule)
          .filter(([_, slot]) => slot.isAvailable)
          .map(([day, slot]) => ({
            day: day,
            startTime: slot.startTime ? new Date(slot.startTime).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }) : '',
            endTime: slot.endTime ? new Date(slot.endTime).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }) : ''
          }));
        
        values.weeklySchedule = weeklyScheduleArray;
        
        // Establecer profileConfigured a true para indicar que el perfil ha sido configurado
        values.profileConfigured = true;
        
        // Actualizar el perfil
        await DoctorService.updateProfile(values);
        setSuccess('Perfil actualizado correctamente');
        loadProfile(); // Recargar el perfil para mostrar los cambios actualizados
      } catch (error) {
        console.error('Error en actualización de perfil:', error);
        setError('Error al actualizar el perfil');
      }
    },
  });

  // Effect hook to load profile data when user ID changes
  useEffect(() => {
    if (usuario?.id) {
      loadProfile();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario?.id]);

  const loadProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await DoctorService.getProfile();
      setProfile(data);
      
      // Configurar el horario si existe
      if (data.weeklySchedule && data.weeklySchedule.length > 0) {
        // Convertir el formato de la API (array de objetos) al formato de nuestro componente (objeto)
        const scheduleObj = { ...initialSchedule };
        
        data.weeklySchedule.forEach(slot => {
          const day = slot.day.toLowerCase();
          if (scheduleObj[day]) {
            scheduleObj[day] = {
              isAvailable: true,
              startTime: slot.startTime ? new Date(`2023-01-01T${slot.startTime}`) : null,
              endTime: slot.endTime ? new Date(`2023-01-01T${slot.endTime}`) : null
            };
          }
        });
        
        setSchedule(scheduleObj);
      }
      
      // Configurar la imagen de perfil si existe
      if (data.photoUrl) {
        setProfileImage(data.photoUrl);
      }
      
      formik.setValues({
        specialty: data.specialty || '',
        consultationCost: data.consultationCost || 0,
        location: data.location || '',
        appointmentDuration: data.appointmentDuration || 30,
        presentation: data.presentation || '',
        photoUrl: data.photoUrl || '',
        weeklySchedule: data.weeklySchedule || []
      });
    } catch (error) {
      console.error('Error al cargar el perfil:', error);
      setError('Error al cargar el perfil');
    } finally {
      setLoading(false);
    }
  };

  const handleScheduleChange = (day, field, value) => {
    setSchedule((prev) => {
      const newSchedule = { ...prev };
      newSchedule[day] = { ...newSchedule[day], [field]: value };
      return newSchedule;
    });
  };
  
  const handleFileChange = (event) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      setSelectedFile(file);
      
      // Mostrar vista previa
      const reader = new FileReader();
      reader.onloadend = () => {
        setProfileImage(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };
  
  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  // Función para traducir días de la semana
  const translateDay = (day) => {
    const translations = {
      'monday': 'Lunes',
      'tuesday': 'Martes',
      'wednesday': 'Miércoles',
      'thursday': 'Jueves',
      'friday': 'Viernes',
      'saturday': 'Sábado',
      'sunday': 'Domingo'
    };
    return translations[day] || day;
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#4a90e2', mb: 3 }}>
          Perfil del Doctor
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

        <Paper elevation={3} sx={{ p: 3 }}>
          <Box sx={{ width: '100%' }}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs value={tabValue} onChange={handleTabChange} aria-label="doctor profile tabs">
                <Tab label="Información Básica" {...a11yProps(0)} />
                <Tab label="Horario Semanal" {...a11yProps(1)} />
                <Tab label="Foto y Presentación" {...a11yProps(2)} />
              </Tabs>
            </Box>

            <form onSubmit={formik.handleSubmit}>
              <TabPanel value={tabValue} index={0}>
                <Card variant="outlined" sx={{ mb: 3 }}>
                  <CardHeader 
                    title="Información Profesional" 
                    titleTypographyProps={{ variant: 'h6' }} 
                    sx={{ backgroundColor: '#f5f5f5' }}
                  />
                  <CardContent>
                    <FormControl fullWidth margin="normal">
                      <InputLabel id="specialty-label">Especialidad</InputLabel>
                      <Select
                        labelId="specialty-label"
                        name="specialty"
                        value={formik.values.specialty}
                        onChange={formik.handleChange}
                        error={formik.touched.specialty && Boolean(formik.errors.specialty)}
                        label="Especialidad"
                      >
                        <MenuItem value=""><em>Seleccionar especialidad</em></MenuItem>
                        {medicalSpecialties.map((specialty) => (
                          <MenuItem key={specialty} value={specialty}>{specialty}</MenuItem>
                        ))}
                      </Select>
                      {formik.touched.specialty && formik.errors.specialty && (
                        <Typography color="error" variant="caption">
                          {formik.errors.specialty}
                        </Typography>
                      )}
                    </FormControl>

                    <TextField
                      fullWidth
                      margin="normal"
                      name="consultationCost"
                      label="Costo de Consulta"
                      type="number"
                      value={formik.values.consultationCost}
                      onChange={formik.handleChange}
                      error={formik.touched.consultationCost && Boolean(formik.errors.consultationCost)}
                      helperText={formik.touched.consultationCost && formik.errors.consultationCost}
                      InputProps={{
                        startAdornment: (
                          <Typography variant="subtitle2" sx={{ mr: 1 }}>$</Typography>
                        ),
                      }}
                    />

                    <TextField
                      fullWidth
                      margin="normal"
                      name="location"
                      label="Ubicación"
                      value={formik.values.location}
                      onChange={formik.handleChange}
                      error={formik.touched.location && Boolean(formik.errors.location)}
                      helperText={formik.touched.location && formik.errors.location}
                    />

                    <FormControl fullWidth margin="normal">
                      <InputLabel id="appointment-duration-label">Duración de Cita</InputLabel>
                      <Select
                        labelId="appointment-duration-label"
                        name="appointmentDuration"
                        value={formik.values.appointmentDuration}
                        onChange={formik.handleChange}
                        error={formik.touched.appointmentDuration && Boolean(formik.errors.appointmentDuration)}
                        label="Duración de Cita"
                      >
                        {appointmentDurations.map((duration) => (
                          <MenuItem key={duration.value} value={duration.value}>
                            {duration.label}
                          </MenuItem>
                        ))}
                      </Select>
                      {formik.touched.appointmentDuration && formik.errors.appointmentDuration && (
                        <Typography color="error" variant="caption">
                          {formik.errors.appointmentDuration}
                        </Typography>
                      )}
                    </FormControl>
                  </CardContent>
                </Card>
              </TabPanel>

              <TabPanel value={tabValue} index={1}>
                <Card variant="outlined" sx={{ mb: 3 }}>
                  <CardHeader 
                    title="Configuración de Horario Semanal" 
                    titleTypographyProps={{ variant: 'h6' }} 
                    sx={{ backgroundColor: '#f5f5f5' }}
                    action={
                      <Tooltip title="Configure su horario para cada día de la semana. Active los días que atiende y establezca el horario de inicio y fin.">
                        <IconButton size="small">
                          <HelpOutlineIcon />
                        </IconButton>
                      </Tooltip>
                    }
                  />
                  <CardContent>
                    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={es}>
                      {Object.keys(schedule).map((day) => (
                        <Box key={day} sx={{ mb: 2, p: 2, border: '1px solid #e0e0e0', borderRadius: 1 }}>
                          <Grid container alignItems="center" spacing={2}>
                            <Grid item xs={12} sm={4}>
                              <FormControlLabel
                                control={
                                  <Switch
                                    checked={schedule[day].isAvailable}
                                    onChange={(e) => handleScheduleChange(day, 'isAvailable', e.target.checked)}
                                    color="primary"
                                  />
                                }
                                label={<Typography fontWeight={schedule[day].isAvailable ? 'bold' : 'normal'}>
                                  {translateDay(day)}
                                </Typography>}
                              />
                            </Grid>
                            
                            {schedule[day].isAvailable && (
                              <>
                                <Grid item xs={12} sm={4}>
                                  <TimePicker
                                    label="Hora inicio"
                                    value={schedule[day].startTime}
                                    onChange={(newValue) => handleScheduleChange(day, 'startTime', newValue)}
                                    disabled={!schedule[day].isAvailable}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                  <TimePicker
                                    label="Hora fin"
                                    value={schedule[day].endTime}
                                    onChange={(newValue) => handleScheduleChange(day, 'endTime', newValue)}
                                    disabled={!schedule[day].isAvailable}
                                  />
                                </Grid>
                              </>
                            )}
                          </Grid>
                        </Box>
                      ))}
                    </LocalizationProvider>
                  </CardContent>
                </Card>
              </TabPanel>

              <TabPanel value={tabValue} index={2}>
                <Grid container spacing={3}>
                  <Grid item xs={12} md={4}>
                    <Card variant="outlined">
                      <CardHeader 
                        title="Foto de Perfil" 
                        titleTypographyProps={{ variant: 'h6' }} 
                        sx={{ backgroundColor: '#f5f5f5' }}
                      />
                      <CardContent sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        <Box sx={{ position: 'relative', mb: 2 }}>
                          <Avatar
                            src={profileImage || undefined}
                            sx={{ width: 150, height: 150, mb: 1 }}
                            alt="Foto de Perfil"
                          />
                          <label htmlFor="upload-photo">
                            <input
                              accept="image/*"
                              id="upload-photo"
                              type="file"
                              style={{ display: 'none' }}
                              onChange={handleFileChange}
                            />
                            <IconButton 
                              color="primary" 
                              aria-label="upload picture" 
                              component="span"
                              sx={{ 
                                position: 'absolute', 
                                bottom: 0, 
                                right: 0, 
                                backgroundColor: 'white',
                                '&:hover': { backgroundColor: '#f5f5f5' }
                              }}
                            >
                              <PhotoCamera />
                            </IconButton>
                          </label>
                        </Box>
                        <Typography variant="caption" color="textSecondary">
                          Haga clic en el ícono de cámara para cambiar su foto de perfil
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  
                  <Grid item xs={12} md={8}>
                    <Card variant="outlined">
                      <CardHeader 
                        title="Presentación Profesional" 
                        titleTypographyProps={{ variant: 'h6' }} 
                        sx={{ backgroundColor: '#f5f5f5' }}
                        action={
                          <Tooltip title="Describa su experiencia, formación, áreas de especialización y filosofía de trabajo. Esta información ayudará a los pacientes a conocerle mejor.">
                            <IconButton size="small">
                              <InfoIcon />
                            </IconButton>
                          </Tooltip>
                        }
                      />
                      <CardContent>
                        <TextField
                          fullWidth
                          name="presentation"
                          label="Presentación"
                          multiline
                          rows={8}
                          value={formik.values.presentation}
                          onChange={formik.handleChange}
                          error={formik.touched.presentation && Boolean(formik.errors.presentation)}
                          helperText={formik.touched.presentation && formik.errors.presentation}
                          placeholder="Escriba aquí su presentación profesional..."
                        />
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </TabPanel>

              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  size="large"
                  disabled={formik.isSubmitting || uploading}
                  startIcon={<SaveIcon />}
                  sx={{ minWidth: 200 }}
                >
                  {uploading ? 'Guardando...' : 'Guardar Cambios'}
                </Button>
              </Box>
            </form>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default DoctorProfilePage;
