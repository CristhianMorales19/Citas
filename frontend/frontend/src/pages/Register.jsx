import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Link as RouterLink } from 'react-router-dom';
import { Link as MuiLink, Box, Button, Container, Paper, TextField, Typography, FormControl, InputLabel, Select, MenuItem, Alert } from '@mui/material';
import { useFormik } from 'formik';
import * as yup from 'yup';
import axios from 'axios';
import { useAutenticacion } from '../contexts/AuthContext';

const validationSchema = yup.object({
  username: yup.string().required('El nombre de usuario es requerido'),
  password: yup
    .string()
    .min(6, 'La contraseña debe tener al menos 6 caracteres')
    .required('La contraseña es requerida'),
  confirmPassword: yup
    .string()
    .oneOf([yup.ref('password')], 'Las contraseñas deben coincidir')
    .required('Confirme su contraseña'),
  name: yup.string().required('El nombre es requerido'),
  role: yup.string().oneOf(['paciente', 'medico']).required('El rol es requerido'),
});

const Register = () => {
  const navigate = useNavigate();
  const { registrar } = useAutenticacion();
  const [error, setError] = useState('');

  const formik = useFormik({
    initialValues: {
      username: '',
      password: '',
      confirmPassword: '',
      name: '',
      role: 'paciente',
    },
    validationSchema,
    onSubmit: async (values) => {
      try {
        const resultado = await registrar(values.username, values.password, values.confirmPassword, values.name, values.role);
        
        // Redirigir según el rol y estado
        if (resultado && resultado.isDoctor) {
          // Si es médico, enviarlo a la página de pendiente de aprobación
          navigate('/pending-approval');
        } else {
          // Si es paciente, llevarlo a la página principal
          navigate('/');
        }
      } catch (err) {
        if (axios.isAxiosError(err)) {
          setError(err.response?.data?.message || 'Error al registrar el usuario');
        } else {
          setError('Error al registrar el usuario');
        }
      }
    },
  });

  return (
    <Container component="main" maxWidth="xs">
      <Paper elevation={3} sx={{ p: 4, mt: 8, mb: 4 }}>
        <Typography component="h1" variant="h5" align="center" gutterBottom>
          Registro
        </Typography>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <Box component="form" onSubmit={formik.handleSubmit}>
          <TextField
            fullWidth
            id="username"
            name="username"
            label="Nombre de usuario"
            value={formik.values.username}
            onChange={formik.handleChange}
            error={formik.touched.username && Boolean(formik.errors.username)}
            helperText={formik.touched.username && formik.errors.username}
            margin="normal"
          />
          <TextField
            fullWidth
            id="password"
            name="password"
            label="Contraseña"
            type="password"
            value={formik.values.password}
            onChange={formik.handleChange}
            error={formik.touched.password && Boolean(formik.errors.password)}
            helperText={formik.touched.password && formik.errors.password}
            margin="normal"
          />
          <TextField
            fullWidth
            id="confirmPassword"
            name="confirmPassword"
            label="Confirmar contraseña"
            type="password"
            value={formik.values.confirmPassword}
            onChange={formik.handleChange}
            error={formik.touched.confirmPassword && Boolean(formik.errors.confirmPassword)}
            helperText={formik.touched.confirmPassword && formik.errors.confirmPassword}
            margin="normal"
          />
          <TextField
            fullWidth
            id="name"
            name="name"
            label="Nombre completo"
            value={formik.values.name}
            onChange={formik.handleChange}
            error={formik.touched.name && Boolean(formik.errors.name)}
            helperText={formik.touched.name && formik.errors.name}
            margin="normal"
          />
          <FormControl fullWidth margin="normal">
            <InputLabel>Role</InputLabel>
            <Select
              value={formik.values.role}
              onChange={formik.handleChange}
              label="Role"
              name="role"
            >
              <MenuItem value="paciente">Paciente</MenuItem>
              <MenuItem value="medico">Médico</MenuItem>
            </Select>
          </FormControl>
          <Button
            type="submit"
            fullWidth
            variant="contained"
            color="primary"
            sx={{ mt: 3, mb: 2 }}
          >
            Registrar
          </Button>
        </Box>
        <Box textAlign="center">
          <MuiLink component={RouterLink} to="/login" variant="body2">
            ¿Ya tienes una cuenta? Inicia sesión
          </MuiLink>
        </Box>
      </Paper>
    </Container>
  );
};

export default Register;
