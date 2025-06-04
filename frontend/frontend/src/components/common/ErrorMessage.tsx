import React from 'react';
import { Alert, AlertTitle } from '@mui/material';

interface ErrorMessageProps {
  message: string;
  title?: string;
  severity?: 'error' | 'warning' | 'info' | 'success';
  onClose?: () => void;
  variant?: 'filled' | 'outlined' | 'standard';
  sx?: object;
}

const ErrorMessage: React.FC<ErrorMessageProps> = ({
  message,
  title,
  severity = 'error',
  onClose,
  variant = 'filled',
  sx = {}
}) => {
  return (
    <Alert 
      severity={severity} 
      onClose={onClose}
      variant={variant}
      sx={{ 
        marginBottom: 2,
        '& .MuiAlert-message': {
          width: '100%'
        },
        ...sx
      }}
    >
      {title && <AlertTitle>{title}</AlertTitle>}
      {message}
    </Alert>
  );
};

export default ErrorMessage;
