import React from 'react';
import { Field, FieldProps, ErrorMessage as FormikErrorMessage } from 'formik';
import { 
  TextField, 
  Select, 
  MenuItem, 
  FormControl, 
  InputLabel, 
  FormHelperText,
  Checkbox,
  FormControlLabel,
  RadioGroup,
  Radio,
  Box
} from '@mui/material';

interface SelectOption {
  value: string | number;
  label: string;
}

interface FormFieldProps {
  name: string;
  label?: string;
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url' | 'select' | 'checkbox' | 'radio' | 'textarea';
  placeholder?: string;
  options?: SelectOption[]; // Para select y radio
  rows?: number; // Para textarea
  fullWidth?: boolean;
  required?: boolean;
  disabled?: boolean;
  helperText?: string;
  sx?: object;
}

const FormField: React.FC<FormFieldProps> = ({
  name,
  label,
  type = 'text',
  placeholder,
  options = [],
  rows = 4,
  fullWidth = true,
  required = false,
  disabled = false,
  helperText,
  sx = {}
}) => {
  return (
    <Field name={name}>
      {({ field, meta }: FieldProps) => {
        const hasError = meta.touched && !!meta.error;
        const errorMessage = meta.touched && meta.error ? meta.error : '';

        // Checkbox
        if (type === 'checkbox') {
          return (
            <Box sx={sx}>
              <FormControlLabel
                control={
                  <Checkbox
                    {...field}
                    checked={field.value || false}
                    disabled={disabled}
                    color={hasError ? 'error' : 'primary'}
                  />
                }
                label={label}
              />
              {(hasError || helperText) && (
                <FormHelperText error={hasError}>
                  {errorMessage || helperText}
                </FormHelperText>
              )}
            </Box>
          );
        }

        // Radio Group
        if (type === 'radio') {
          return (
            <FormControl 
              component="fieldset" 
              error={hasError} 
              fullWidth={fullWidth}
              sx={sx}
            >
              {label && <InputLabel component="legend">{label}</InputLabel>}
              <RadioGroup {...field} value={field.value || ''}>
                {options.map((option) => (
                  <FormControlLabel
                    key={option.value}
                    value={option.value}
                    control={<Radio disabled={disabled} />}
                    label={option.label}
                  />
                ))}
              </RadioGroup>
              {(hasError || helperText) && (
                <FormHelperText>
                  {errorMessage || helperText}
                </FormHelperText>
              )}
            </FormControl>
          );
        }

        // Select
        if (type === 'select') {
          return (
            <FormControl 
              fullWidth={fullWidth} 
              error={hasError}
              sx={sx}
            >
              {label && <InputLabel>{label}</InputLabel>}
              <Select
                {...field}
                value={field.value || ''}
                label={label}
                disabled={disabled}
              >
                {options.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
              {(hasError || helperText) && (
                <FormHelperText>
                  {errorMessage || helperText}
                </FormHelperText>
              )}
            </FormControl>
          );
        }

        // TextField (text, email, password, number, tel, url, textarea)
        return (
          <TextField
            {...field}
            label={label}
            type={type}
            placeholder={placeholder}
            multiline={type === 'textarea'}
            rows={type === 'textarea' ? rows : undefined}
            fullWidth={fullWidth}
            required={required}
            disabled={disabled}
            error={hasError}
            helperText={errorMessage || helperText}
            value={field.value || ''}
            sx={sx}
          />
        );
      }}
    </Field>
  );
};

export default FormField;
