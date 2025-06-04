import React from 'react';
import { Button, ButtonProps, CircularProgress } from '@mui/material';

interface ActionButtonProps extends ButtonProps {
  loading?: boolean;
  loadingText?: string;
}

const ActionButton: React.FC<ActionButtonProps> = ({
  loading = false,
  loadingText,
  children,
  disabled,
  ...props
}) => {
  return (
    <Button
      {...props}
      disabled={disabled || loading}
      startIcon={loading ? (
        <CircularProgress 
          size={16} 
          sx={{ color: 'inherit' }} 
        />
      ) : props.startIcon}
      sx={{
        position: 'relative',
        ...props.sx
      }}
    >
      {loading && loadingText ? loadingText : children}
    </Button>
  );
};

export default ActionButton;
