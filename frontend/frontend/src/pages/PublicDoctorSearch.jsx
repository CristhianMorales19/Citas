import React, { useState, useEffect } from 'react';
import { 
  Container, 
  Typography, 
  Grid, 
  Card, 
  CardContent, 
  CardActions, 
  Button, 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  Box, 
  CircularProgress,
  Avatar,
  CardMedia
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { doctorPublicService } from '../services/doctorService';

// Lista de especialidades médicas comunes
const ESPECIALIDADES = [
  'Medicina General',
  'Cardiología',
  'Dermatología',
  'Ginecología',
  'Pediatría',
  'Neurología',
  'Oftalmología',
  'Psiquiatría',
  'Odontología',
  'Traumatología'
];

// Lista de ubicaciones (ciudades)
const UBICACIONES = [
  'Quito',
  'Guayaquil',
  'Cuenca',
  'Ambato',
  'Loja',
  'Ibarra',
  'Riobamba'
];

const PublicDoctorSearch = () => {
  const [especialidad, setEspecialidad] = useState('');
  const [ubicacion, setUbicacion] = useState('');
  const [doctores, setDoctores] = useState([]);
  const [cargando, setCargando] = useState(true); // Inicialmente cargando
  const [error, setError] = useState('');
  const [busquedaRealizada, setBusquedaRealizada] = useState(true); // Inicialmente consideramos que hay búsqueda
  
  const navigate = useNavigate();
  
  // Cargar todos los médicos al iniciar el componente
  useEffect(() => {
    cargarTodosLosMedicos();
  }, []);

  const handleChangeEspecialidad = (event) => {
    setEspecialidad(event.target.value);
  };

  const handleChangeUbicacion = (event) => {
    setUbicacion(event.target.value);
  };

  // Función para cargar todos los médicos sin filtros
  const cargarTodosLosMedicos = async () => {
    try {
      setCargando(true);
      setError('');
      
      // Llamar al servicio sin filtros específicos
      const resultados = await doctorPublicService.searchDoctors('', '');
      setDoctores(resultados);
      
      if (resultados.length === 0) {
        setError('No hay médicos disponibles en el sistema.');
      }
    } catch (err) {
      console.error('Error al cargar médicos:', err);
      setError('Ocurrió un error al cargar los médicos. Por favor intente nuevamente.');
    } finally {
      setCargando(false);
    }
  };

  // Función para buscar médicos con filtros
  const handleBuscar = async () => {
    try {
      setCargando(true);
      setError('');
      
      const resultados = await doctorPublicService.searchDoctors(especialidad, ubicacion);
      setDoctores(resultados);
      setBusquedaRealizada(true);
      
      if (resultados.length === 0) {
        setError('No se encontraron médicos con los criterios de búsqueda seleccionados.');
      }
    } catch (err) {
      console.error('Error al buscar médicos:', err);
      setError('Ocurrió un error al buscar médicos. Por favor intente nuevamente.');
    } finally {
      setCargando(false);
    }
  };

  const handleVerDisponibilidad = (doctorId) => {
    navigate(`/public/doctors/${doctorId}/availability`);
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom align="center">
          Búsqueda de Médicos
        </Typography>
        
        <Typography variant="subtitle1" gutterBottom align="center">
          Encuentra médicos por especialidad y ubicación
        </Typography>
        
        <Box sx={{ mt: 4, p: 3, bgcolor: '#f5f5f5', borderRadius: 2 }}>
          <Grid container spacing={3} alignItems="center">
            <Grid item xs={12} md={5}>
              <FormControl fullWidth>
                <InputLabel id="especialidad-label">Especialidad</InputLabel>
                <Select
                  labelId="especialidad-label"
                  id="especialidad-select"
                  value={especialidad}
                  label="Especialidad"
                  onChange={handleChangeEspecialidad}
                >
                  <MenuItem value="">
                    Cualquier especialidad
                  </MenuItem>
                  {ESPECIALIDADES.map((esp) => (
                    <MenuItem key={esp} value={esp}>{esp}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={5}>
              <FormControl fullWidth>
                <InputLabel id="ubicacion-label">Ubicación</InputLabel>
                <Select
                  labelId="ubicacion-label"
                  id="ubicacion-select"
                  value={ubicacion}
                  label="Ubicación"
                  onChange={handleChangeUbicacion}
                >
                  <MenuItem value="">
                    Cualquier ubicación
                  </MenuItem>
                  {UBICACIONES.map((ubi) => (
                    <MenuItem key={ubi} value={ubi}>{ubi}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={2}>
              <Button 
                variant="contained" 
                color="primary" 
                fullWidth 
                size="large"
                onClick={handleBuscar}
                disabled={cargando}
              >
                {cargando ? <CircularProgress size={24} /> : 'Buscar'}
              </Button>
            </Grid>
          </Grid>
        </Box>
        
        {error && (
          <Typography color="error" align="center" sx={{ mt: 2 }}>
            {error}
          </Typography>
        )}
        
        {cargando && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <CircularProgress />
          </Box>
        )}
        
        {busquedaRealizada && !cargando && !error && (
          <Box sx={{ mt: 4 }}>
            <Typography variant="h5" gutterBottom>
              {especialidad || ubicacion ? `Resultados de búsqueda (${doctores.length})` : `Todos los médicos (${doctores.length})`}
            </Typography>
            
            <Grid container spacing={3}>
              {doctores.map((doctor) => (
                <Grid item xs={12} sm={6} md={4} key={doctor.id}>
                  <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                    <CardContent sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                      <Avatar 
                        src={doctor.photoUrl} 
                        alt={`Foto de ${doctor.name}`}
                        sx={{ 
                          width: 120, 
                          height: 120, 
                          mb: 2,
                          border: '2px solid #f0f0f0',
                          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                        }}
                      />
                      <Typography variant="h6" component="h2" gutterBottom align="center">
                        Dr. {doctor.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        <Typography component="span" fontWeight="bold">Especialidad:</Typography> {doctor.specialty}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        <Typography component="span" fontWeight="bold">Ubicación:</Typography> {doctor.location}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        <Typography component="span" fontWeight="bold">Costo de Consulta:</Typography> ${doctor.consultationCost}
                      </Typography>
                      {doctor.presentation && (
                        <Typography variant="body2" sx={{ mt: 1 }}>
                          {doctor.presentation.length > 100 
                            ? `${doctor.presentation.substring(0, 100)}...` 
                            : doctor.presentation}
                        </Typography>
                      )}
                    </CardContent>
                    <CardActions>
                      <Button 
                        size="small" 
                        color="primary" 
                        onClick={() => handleVerDisponibilidad(doctor.id)}
                        fullWidth
                      >
                        Ver Disponibilidad
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Box>
        )}
      </Box>
    </Container>
  );
};

export default PublicDoctorSearch;
