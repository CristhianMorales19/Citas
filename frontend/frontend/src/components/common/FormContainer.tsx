import React from 'react';
import { Formik, Form, FormikProps } from 'formik';
import { Box, Paper, Typography } from '@mui/material';
import * as Yup from 'yup';
import ActionButton from './ActionButton';
import ErrorMessage from './ErrorMessage';

interface FormContainerProps<T = any> {
  initialValues: T;
  validationSchema?: Yup.ObjectSchema<any>;
  onSubmit: (values: T) => Promise<void> | void;
  children: React.ReactNode | ((formikProps: FormikProps<T>) => React.ReactNode);
  title?: string;
  subtitle?: string;
  submitButtonText?: string;
  cancelButtonText?: string;
  onCancel?: () => void;
  loading?: boolean;
  error?: string;
  paper?: boolean;
  fullWidth?: boolean;
  sx?: object;
}

const FormContainer = <T extends Record<string, any>>({
  initialValues,
  validationSchema,
  onSubmit,
  children,
  title,
  subtitle,
  submitButtonText = 'Guardar',
  cancelButtonText = 'Cancelar',
  onCancel,
  loading = false,
  error,
  paper = true,
  fullWidth = true,
  sx = {}
}: FormContainerProps<T>) => {
  const handleSubmit = async (values: T) => {
    try {
      await onSubmit(values);
    } catch (error) {
      console.error('Error al enviar formulario:', error);
    }
  };

  const formContent = (
    <Formik
      initialValues={initialValues}
      validationSchema={validationSchema}
      onSubmit={handleSubmit}
      enableReinitialize
    >
      {(formikProps) => (
        <Form>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {/* Header */}
            {(title || subtitle) && (
              <Box mb={2}>
                {title && (
                  <Typography variant="h5" component="h2" gutterBottom>
                    {title}
                  </Typography>
                )}
                {subtitle && (
                  <Typography variant="body2" color="text.secondary">
                    {subtitle}
                  </Typography>
                )}
              </Box>
            )}

            {/* Error Message */}
            {error && (
              <ErrorMessage 
                message={error} 
                severity="error" 
              />
            )}

            {/* Form Fields */}
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {typeof children === 'function' ? children(formikProps) : children}
            </Box>

            {/* Action Buttons */}
            <Box 
              sx={{ 
                display: 'flex', 
                gap: 2, 
                justifyContent: 'flex-end',
                mt: 3 
              }}
            >
              {onCancel && (
                <ActionButton
                  variant="outlined"
                  onClick={onCancel}
                  disabled={loading}
                >
                  {cancelButtonText}
                </ActionButton>
              )}
              <ActionButton
                type="submit"
                variant="contained"
                loading={loading}
                loadingText="Guardando..."
              >
                {submitButtonText}
              </ActionButton>
            </Box>
          </Box>
        </Form>
      )}
    </Formik>
  );

  if (paper) {
    return (
      <Paper 
        elevation={2} 
        sx={{ 
          p: 3, 
          width: fullWidth ? '100%' : 'auto',
          ...sx 
        }}
      >
        {formContent}
      </Paper>
    );
  }

  return (
    <Box 
      sx={{ 
        width: fullWidth ? '100%' : 'auto',
        ...sx 
      }}
    >
      {formContent}
    </Box>
  );
};

export default FormContainer;
