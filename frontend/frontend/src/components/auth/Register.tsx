import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAutenticacion } from '../../contexts/AuthContext';
import * as Yup from 'yup';
import { 
  PageContainer, 
  FormContainer, 
  FormField
} from '../common';
import { ROUTES } from '../../constants/routes';

// Esquema de validación
const validationSchema = Yup.object({
  username: Yup.string()
    .min(3, 'El nombre de usuario debe tener al menos 3 caracteres')
    .required('El nombre de usuario es obligatorio'),
  password: Yup.string()
    .min(6, 'La contraseña debe tener al menos 6 caracteres')
    .required('La contraseña es obligatoria'),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref('password')], 'Las contraseñas deben coincidir')
    .required('Confirme su contraseña'),
  name: Yup.string()
    .min(2, 'El nombre debe tener al menos 2 caracteres')
    .required('El nombre es obligatorio'),
  role: Yup.string()
    .oneOf(['paciente', 'medico', 'admin'], 'Seleccione un rol válido')
    .required('El rol es obligatorio')
});

// Valores iniciales
const initialValues = {
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  role: 'paciente'
};

const Register = () => {
  const navigate = useNavigate();
  const { registrar } = useAutenticacion();

  const roleOptions = [
    { value: 'paciente', label: 'Paciente' },
    { value: 'medico', label: 'Médico' },
    { value: 'admin', label: 'Administrador' }
  ];

  return (
    <PageContainer 
      title="Crear una cuenta" 
      maxWidth="sm" 
      paper={false}
      sx={{ 
        minHeight: '100vh', 
        display: 'flex', 
        alignItems: 'center', 
        bgcolor: 'grey.50' 
      }}
    >
      <FormContainer
        initialValues={initialValues}
        validationSchema={validationSchema}
        onSubmit={async (values) => {
          await registrar(
            values.username,
            values.password,
            values.confirmPassword,
            values.name,
            values.role as 'paciente' | 'medico' | 'admin'
          );
          navigate(ROUTES.LOGIN);
        }}
        title="Registro de Usuario"
        subtitle="Complete los siguientes campos para crear su cuenta"
        submitButtonText="Registrarse"
        cancelButtonText="Ya tengo cuenta"
        onCancel={() => navigate(ROUTES.LOGIN)}
        sx={{ maxWidth: 500, mx: 'auto' }}
      >
        <FormField
          name="username"
          label="Nombre de usuario"
          type="text"
          placeholder="Ingrese su nombre de usuario"
          required
        />
        
        <FormField
          name="name"
          label="Nombre completo"
          type="text"
          placeholder="Ingrese su nombre completo"
          required
        />
        
        <FormField
          name="role"
          label="Tipo de usuario"
          type="select"
          options={roleOptions}
          required
        />
        
        <FormField
          name="password"
          label="Contraseña"
          type="password"
          placeholder="Ingrese su contraseña"
          required
          helperText="Mínimo 6 caracteres"
        />
        
        <FormField
          name="confirmPassword"
          label="Confirmar contraseña"
          type="password"
          placeholder="Confirme su contraseña"
          required
        />
      </FormContainer>
    </PageContainer>
  );
};

export default Register;
