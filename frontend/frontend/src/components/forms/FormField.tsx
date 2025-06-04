import React from 'react';
import { TextField, TextFieldProps } from '@mui/material';
import { FormikProps } from 'formik';

interface FormFieldProps extends Omit<TextFieldProps, 'error' | 'helperText'> {
  name: string;
  formik: FormikProps<any>;
  label: string;
}

const FormField: React.FC<FormFieldProps> = ({
  name,
  formik,
  label,
  ...props
}) => {
  const hasError = formik.touched[name] && Boolean(formik.errors[name]);
  const errorMessage = formik.touched[name] && formik.errors[name] 
    ? String(formik.errors[name]) 
    : undefined;

  return (
    <TextField
      fullWidth
      id={name}
      name={name}
      label={label}
      value={formik.values[name]}
      onChange={formik.handleChange}
      onBlur={formik.handleBlur}
      error={hasError}
      helperText={errorMessage}
      margin="normal"
      {...props}
    />
  );
};

export default FormField;
