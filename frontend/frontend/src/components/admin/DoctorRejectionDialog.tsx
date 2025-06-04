import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Typography,
} from '@mui/material';
import { PendingDoctor } from '../../types/appointment';
import { ActionButton } from '../common';

interface DoctorRejectionDialogProps {
  open: boolean;
  doctor: PendingDoctor | null;
  onClose: () => void;
  onConfirm: (doctorId: string, reason: string) => Promise<void>;
}

const DoctorRejectionDialog: React.FC<DoctorRejectionDialogProps> = ({
  open,
  doctor,
  onClose,
  onConfirm,
}) => {
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!doctor || !reason.trim()) return;

    try {
      setLoading(true);
      await onConfirm(doctor.id, reason);
      setReason('');
      onClose();
    } catch (error) {
      console.error('Error al rechazar médico:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setReason('');
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        Rechazar Solicitud de Médico
      </DialogTitle>
      
      <DialogContent>
        {doctor && (
          <>
            <Typography variant="body1" gutterBottom>
              ¿Estás seguro de que deseas rechazar la solicitud de <strong>{doctor.name}</strong>?
            </Typography>
            
            <TextField
              fullWidth
              multiline
              rows={4}
              label="Motivo del rechazo *"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Proporciona una explicación del motivo del rechazo..."
              margin="normal"
              helperText="Este mensaje será enviado al médico"
            />
          </>
        )}
      </DialogContent>
      
      <DialogActions>
        <Button 
          onClick={handleClose} 
          disabled={loading}
        >
          Cancelar
        </Button>
        <ActionButton
          onClick={handleSubmit}
          loading={loading}
          disabled={!reason.trim()}
          color="error"
          variant="contained"
        >
          Rechazar
        </ActionButton>
      </DialogActions>
    </Dialog>
  );
};

export default DoctorRejectionDialog;
