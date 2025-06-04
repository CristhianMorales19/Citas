import React from 'react';
import * as yup from 'yup';
import { Link, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAutenticacion } from '../contexts/AuthContext';
import { useFormState } from '../hooks/useFormState';
import { PageContainer } from '../components/common';
import { FormField, FormContainer } from '../components/forms';
import { ROUTES, getDefaultRouteForRole } from '../constants/routes';

interface LoginFormValues {
  username: string;
  password: string;
}

const validationSchema = yup.object({
  username: yup.string().required('El nombre de usuario es requerido'),
  password: yup.string().required('La contraseña es requerida'),
});

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { iniciarSesion } = useAutenticacion();

  const { formik, loading, error, success } = useFormState<LoginFormValues>({
    initialValues: {
      username: '',
      password: '',
    },
    validationSchema,
    onSubmit: async (values) => {
      const response = await iniciarSesion(values.username, values.password);
      
      if (response?.user?.role) {
        const redirectRoute = getDefaultRouteForRole(response.user.role);
        navigate(redirectRoute);
      } else {
        throw new Error('No se pudo determinar el rol del usuario');
      }
    },
  });

  return (
    <PageContainer title="Iniciar Sesión" maxWidth="sm">
      <FormContainer
        formik={formik}
        error={error}
        success={success}
        loading={loading}
        submitText="Iniciar Sesión"
      >
        <FormField
          name="username"
          label="Nombre de Usuario"
          formik={formik}
          autoComplete="username"
          autoFocus
        />
        
        <FormField
          name="password"
          label="Contraseña"
          type="password"
          formik={formik}
          autoComplete="current-password"
        />
      </FormContainer>

      <Box sx={{ mt: 2, textAlign: 'center' }}>
        <Link
          component="button"
          variant="body2"
          onClick={() => navigate(ROUTES.REGISTER)}
        >
          ¿No tienes cuenta? Regístrate aquí
        </Link>
      </Box>
    </PageContainer>
  );
};

export default Login; 