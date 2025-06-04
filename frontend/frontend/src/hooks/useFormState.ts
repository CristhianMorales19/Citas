import { useState } from 'react';
import { FormikConfig, useFormik } from 'formik';

interface UseFormStateOptions<T> {
  initialValues: T;
  validationSchema?: any;
  onSubmit: (values: T) => Promise<void> | void;
}

export const useFormState = <T extends Record<string, any>>({
  initialValues,
  validationSchema,
  onSubmit,
}: UseFormStateOptions<T>) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const formik = useFormik({
    initialValues,
    validationSchema,
    onSubmit: async (values, { setSubmitting }) => {
      try {
        setLoading(true);
        setError(null);
        setSuccess(null);
        
        await onSubmit(values);
        
        setSuccess('Operación completada exitosamente');
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Error desconocido');
      } finally {
        setLoading(false);
        setSubmitting(false);
      }
    },
  });

  const clearMessages = () => {
    setError(null);
    setSuccess(null);
  };

  return {
    formik,
    loading,
    error,
    success,
    clearMessages,
  };
};
