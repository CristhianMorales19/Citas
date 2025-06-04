import React from 'react';
import { Grid, Box } from '@mui/material';
import { ActionButton } from '../common';

interface AppointmentActionsProps {
  onCancel: () => void;
  onConfirm: () => void;
  loading?: boolean;
}

const AppointmentActions: React.FC<AppointmentActionsProps> = ({
  onCancel,
  onConfirm,
  loading = false,
}) => {
  return (
    <Grid item xs={12}>
      <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
        <ActionButton
          variant="outlined"
          onClick={onCancel}
          disabled={loading}
        >
          Cancelar
        </ActionButton>
        <ActionButton
          variant="contained"
          onClick={onConfirm}
          loading={loading}
        >
          Confirmar Cita
        </ActionButton>
      </Box>
    </Grid>
  );
};

export default AppointmentActions;
