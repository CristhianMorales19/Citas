import React from 'react';
// @ts-ignore - Import useState directly
import { useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { format } from 'date-fns';
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
import { DoctorProfile, DoctorTimeSlot, WeekSchedule } from '../types/appointment';
import { useAutenticacion } from '../contexts/AuthContext';
import PhotoCamera from '@mui/icons-material/PhotoCamera';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import InfoIcon from '@mui/icons-material/Info';
import SaveIcon from '@mui/icons-material/Save';

// Handle useEffect differently due to TypeScript definition conflicts
// @ts-ignore - Fix for useEffect definition conflicts
const useEffect = React.useEffect;

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
];

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
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </Box>
  );
}

function a11yProps(index: number) {
  return {
    id: `doctor-profile-tab-${index}`,
    'aria-controls': `doctor-profile-tabpanel-${index}`,
  };
}

const DoctorProfilePage = () => {
  const { usuario } = useAutenticacion();
  
  // Estado para controlar si se muestra el mensaje de bienvenida para médicos recién aprobados
  const [showWelcomeMessage, setShowWelcomeMessage] = useState(false);
  const [profile, setProfile] = useState<DoctorProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);
  const [schedule, setSchedule] = useState<WeekSchedule>(initialSchedule);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [profileImage, setProfileImage] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  const formik = useFormik({
    initialValues: {
      specialty: '',
      consultationCost: 0,
      location: '',
      appointmentDuration: 30,
      presentation: '',
      photoUrl: '',
      schedule: initialSchedule
    },
    validationSchema,
    onSubmit: async (values) => {
      try {
        setError(null);
        setSuccess(null);
        // Si hay un archivo seleccionado, primero sube la imagen
        if (selectedFile) {
          setUploading(true);
          const formData = new FormData();
          formData.append('file', selectedFile);
          try {
            const response = await DoctorService.uploadProfilePhoto(formData);
            values.photoUrl = response.url;
          } catch (error) {
            setError('Error al subir la imagen de perfil');
            setUploading(false);
            return;
          } finally {
            setUploading(false);
          }
        }
        
        // Incluir el horario en los valores
        values.schedule = schedule;
        
        // Convertir el horario semanal al formato esperado por la API
        const weeklySchedule: Array<{
          day: string;
          startTime: string;
          endTime: string;
        }> = [];
        
        (Object.entries(schedule) as Array<[string, DoctorTimeSlot]>).forEach(([day, slot]) => {
          if (slot.isAvailable && slot.startTime && slot.endTime) {
            // Asegurarse de que el día esté en mayúsculas
            const dayOfWeek = day.toUpperCase();
            const startTime = format(slot.startTime as Date, 'HH:mm');
            const endTime = format(slot.endTime as Date, 'HH:mm');
            
            // Validar que los tiempos sean válidos
            if (startTime && endTime) {
              weeklySchedule.push({
                day: dayOfWeek,
                startTime,
                endTime,
              });
            }
          }
        });
        
        // Validar que los campos requeridos estén presentes
        if (!values.specialty || !values.location) {
          throw new Error('Por favor completa todos los campos requeridos');
        }
        
        // Combinar datos del formulario con el horario
        const profileData = {
          specialty: values.specialty,
          location: values.location,
          consultationCost: values.consultationCost || 0,
          appointmentDuration: values.appointmentDuration || 30,
          presentation: values.presentation || '',
          photoUrl: values.photoUrl || '',
          weeklySchedule,
          profileConfigured: true
        };
        
        // Enviar al servidor
        await DoctorService.updateProfile(profileData);
        
        // Si estaba mostrando el mensaje de bienvenida, ocultarlo ahora
        if (showWelcomeMessage) {
          setShowWelcomeMessage(false);
        }
        
        // Actualizar estado local y mostrar mensaje de éxito
        setSuccess('Perfil actualizado correctamente');
        loadProfile(); // Recargar los datos del perfil
      } catch (error) {
        setError('Error al actualizar el perfil');
      }
    },
  });

  // Using a workaround for useEffect to handle component initialization
  // @ts-ignore - This works at runtime even though TypeScript has definition conflicts
  React.useEffect(() => {
    console.log('DoctorProfile mounted, checking user:', usuario);
    if (usuario?.id) {
      console.log('User ID found, loading profile...');
      loadProfile();
    } else {
      console.log('No user ID found, waiting for authentication...');
      setLoading(false);
    }
  }, [usuario?.id]);

  const loadProfile = async () => {
    try {
      console.log('Starting to load profile...');
      setLoading(true);
      setError(null);
      
      try {
        const data = await DoctorService.getProfile();
        console.log('Profile data loaded successfully:', data);
        setProfile(data);
        
        // Verificar si es un médico recién aprobado que necesita configurar su perfil
        if (data.status === 'APPROVED' && !data.profileConfigured) {
          console.log('Doctor is approved but profile not configured');
          setShowWelcomeMessage(true);
        } else {
          console.log('Doctor profile is already configured');
          setShowWelcomeMessage(false);
        }
        
        // Establecer valores iniciales para el formulario usando profileData
        formik.setValues({
          specialty: data.specialty || '',
          consultationCost: data.consultationCost || 0,
          location: data.location || '',
          appointmentDuration: data.appointmentDuration || 30,
          presentation: data.presentation || '',
          photoUrl: data.photoUrl || '',
          schedule: data.schedule || initialSchedule
        });

        // Configurar el horario semanal solo si hay datos
        if (data.weeklySchedule && data.weeklySchedule.length > 0) {
          const updatedSchedule = { ...initialSchedule };

          // Procesar cada entrada de horario del médico
          data.weeklySchedule.forEach(slot => {
            const day = slot.day.toLowerCase() as keyof WeekSchedule;
            if (day in updatedSchedule) {
              // Convertir las cadenas de hora a objetos Date
              const parseTime = (timeStr: string) => {
                if (!timeStr) return null;
                const [hours, minutes] = timeStr.split(':').map(Number);
                const date = new Date();
                date.setHours(hours, minutes, 0, 0);
                return date;
              };

              updatedSchedule[day] = {
                isAvailable: true,
                startTime: parseTime(slot.startTime),
                endTime: parseTime(slot.endTime)
              };
            }
          });
          
          setSchedule(updatedSchedule);
        }
        
        return data;
      } catch (error) {
        console.error('Error loading doctor profile:', error);
        setError('Error al cargar el perfil. Por favor, recarga la página o intenta nuevamente más tarde.');
        throw error; // Re-throw to be caught by the outer try-catch
      }

    } catch (err) {
      console.error('Error al cargar el perfil:', err);
      setError('Error al cargar el perfil del médico');
    } finally {
      setLoading(false);
    }
  };

  const handleScheduleChange = (day: keyof WeekSchedule, field: 'isAvailable' | 'startTime' | 'endTime', value: any) => {
    setSchedule((prev: WeekSchedule) => {
      const newSchedule = { ...prev };
      newSchedule[day] = { ...newSchedule[day], [field]: value };
      return newSchedule;
    });
  };
  
  const handleTabChange = (event: any, newValue: number) => {
    setTabValue(newValue);
  };
  
  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      
      // Validar tipo de archivo
      const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
      if (!validTypes.includes(file.type)) {
        setError('Por favor, seleccione un archivo de imagen válido (JPG, PNG o GIF)');
        return;
      }
      
      // Validar tamaño del archivo (máx 5MB)
      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        setError('La imagen es demasiado grande. El tamaño máximo permitido es 5MB');
        return;
      }
      
      setSelectedFile(file);
      setError(null);
      
      // Crear una URL para la vista previa de la imagen
      const reader = new FileReader();
      reader.onloadend = () => {
        setProfileImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  // Función para traducir días de la semana
  const translateDay = (day: string): string => {
    const translations: Record<string, string> = {
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

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4, mb: 4 }}>
        {showWelcomeMessage && (
          <Paper elevation={3} sx={{ p: 3, mb: 4, bgcolor: '#e8f5e9', border: '1px solid #81c784' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Box sx={{ mr: 2, color: '#2e7d32' }}>
                <InfoIcon fontSize="large" />
              </Box>
              <Typography variant="h5" component="h2" color="#2e7d32">
                ¡Felicidades! Su cuenta ha sido aprobada
              </Typography>
            </Box>
            <Typography variant="body1" paragraph>
              Ahora necesita completar su perfil profesional para comenzar a recibir citas de pacientes. Por favor, complete la siguiente información:
            </Typography>
            <Box component="ul" sx={{ pl: 4 }}>
              <Typography component="li" variant="body1">
                <Typography component="span" sx={{ fontWeight: 'bold' }}>Especialidad médica</Typography> - Seleccione su especialidad
              </Typography>
              <Typography component="li" variant="body1">
                <Typography component="span" sx={{ fontWeight: 'bold' }}>Costo de consulta</Typography> - Establezca el precio de su consulta
              </Typography>
              <Typography component="li" variant="body1">
                <Typography component="span" sx={{ fontWeight: 'bold' }}>Ubicación</Typography> - Indique dónde atiende a los pacientes
              </Typography>
              <Typography component="li" variant="body1">
                <Typography component="span" sx={{ fontWeight: 'bold' }}>Horario semanal</Typography> - Configure sus días y horas de atención
              </Typography>
              <Typography component="li" variant="body1">
                <Typography component="span" sx={{ fontWeight: 'bold' }}>Presentación</Typography> - Describa su experiencia y formación profesional
              </Typography>
            </Box>
            <Typography variant="body1" sx={{ mt: 2, fontStyle: 'italic' }}>
              Una vez guardada esta información, su perfil será visible para los pacientes que busquen médicos de su especialidad.  
            </Typography>
          </Paper>
        )}
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

            <Box component="form" onSubmit={formik.handleSubmit}>
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
                        <MenuItem value=""><Typography style={{ fontStyle: 'italic' }}>Seleccionar especialidad</Typography></MenuItem>
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
                      {(Object.keys(schedule) as Array<keyof WeekSchedule>).map((day) => (
                        <Box key={day} sx={{ mb: 2, p: 2, border: '1px solid #e0e0e0', borderRadius: 1 }}>
                          <Grid container alignItems="center" spacing={2}>
                            <Grid item xs={12} sm={4}>
                              <FormControlLabel
                                control={
                                  <Switch
                                    checked={schedule[day].isAvailable}
                                    onChange={(e: any) => handleScheduleChange(day, 'isAvailable', e.target.checked)}
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
                                    onChange={(newValue: Date | null) => handleScheduleChange(day, 'startTime', newValue)}
                                    disabled={!schedule[day].isAvailable}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                  <TimePicker
                                    label="Hora fin"
                                    value={schedule[day].endTime}
                                    onChange={(newValue: Date | null) => handleScheduleChange(day, 'endTime', newValue)}
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
                        <Box sx={{ position: 'relative', mb: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                          <Avatar
                            src={profileImage || (profile?.photoUrl || '')}
                            sx={{ width: 150, height: 150, mb: 2 }}
                            alt="Foto de Perfil"
                          />
                          
                          {/* Input de tipo archivo oculto */}
                          <input
                            accept="image/*"
                            style={{ display: 'none' }}
                            id="upload-photo"
                            type="file"
                            onChange={handleFileChange}
                          />
                          
                          {/* Botón para abrir el selector de archivos */}
                          <label htmlFor="upload-photo">
                            <Button
                              variant="contained"
                              color="primary"
                              component="span"
                              startIcon={<PhotoCamera />}
                              sx={{ mt: 1 }}
                            >
                              {selectedFile ? 'Cambiar imagen' : 'Seleccionar imagen'}
                            </Button>
                          </label>
                          
                          {/* Muestra el nombre del archivo seleccionado */}
                          {selectedFile && (
                            <Typography variant="caption" display="block" sx={{ mt: 1 }}>
                              {selectedFile.name}
                            </Typography>
                          )}
                          
                          {/* Mensaje de ayuda */}
                          <Typography variant="caption" color="textSecondary" sx={{ mt: 1, textAlign: 'center' }}>
                            Formatos soportados: JPG, PNG, GIF
                          </Typography>
                        </Box>
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
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default DoctorProfilePage; 