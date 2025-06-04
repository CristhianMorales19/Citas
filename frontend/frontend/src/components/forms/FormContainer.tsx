import React from 'react';
import { Box, BoxProps } from '@mui/material';
import { FormikProps } from 'formik';
import { ErrorMessage, ActionButton } from '../common';

interface FormContainerProps extends BoxProps {
  formik: FormikProps<any>;
  error?: string | null;
  success?: string | null;
  loading?: boolean;
  submitText?: string;
  children: React.ReactNode;
}

const FormContainer: React.FC<FormContainerProps> = ({
  formik,
  error,
  success,
  loading = false,
  submitText = 'Enviar',
  children,
  ...boxProps
}) => {
  return (
    <Box component="form" onSubmit={formik.handleSubmit} {...boxProps}>
      {error && (
        <ErrorMessage message={error} severity="error" sx={{ mb: 2 }} />
      )}
      
      {success && (
        <ErrorMessage message={success} severity="success" sx={{ mb: 2 }} />
      )}

      {children}

      <ActionButton
        type="submit"
        variant="contained"
        fullWidth
        loading={loading}
        loadingText="Procesando..."
        sx={{ mt: 3 }}
      >
        {submitText}
      </ActionButton>
    </Box>
  );
};

export default FormContainer;
