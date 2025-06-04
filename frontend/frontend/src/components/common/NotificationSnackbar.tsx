import React from 'react';
import {
  Snackbar,
  Alert,
  AlertProps,
  IconButton,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

interface NotificationSnackbarProps {
  open: boolean;
  message: string;
  severity?: AlertProps['severity'];
  autoHideDuration?: number;
  onClose: () => void;
  action?: React.ReactNode;
  anchorOrigin?: {
    vertical: 'top' | 'bottom';
    horizontal: 'left' | 'center' | 'right';
  };
  showCloseButton?: boolean;
}

export const NotificationSnackbar: React.FC<NotificationSnackbarProps> = ({
  open,
  message,
  severity = 'info',
  autoHideDuration = 6000,
  onClose,
  action,
  anchorOrigin = { vertical: 'bottom', horizontal: 'left' },
  showCloseButton = true,
}) => {
  const handleClose = (event?: React.SyntheticEvent | Event, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }
    onClose();
  };

  const alertAction = action || (showCloseButton ? (
    <IconButton
      size="small"
      aria-label="close"
      color="inherit"
      onClick={handleClose}
    >
      <CloseIcon fontSize="small" />
    </IconButton>
  ) : undefined);

  return (
    <Snackbar
      open={open}
      autoHideDuration={autoHideDuration}
      onClose={handleClose}
      anchorOrigin={anchorOrigin}
    >
      <Alert
        onClose={handleClose}
        severity={severity}
        action={alertAction}
        variant="filled"
        sx={{ width: '100%' }}
      >
        {message}
      </Alert>
    </Snackbar>
  );
};
