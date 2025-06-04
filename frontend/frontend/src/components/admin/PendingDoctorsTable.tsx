import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  Paper,
  Box,
} from '@mui/material';
import { PendingDoctor } from '../../types/appointment';

interface PendingDoctorsTableProps {
  doctors: PendingDoctor[];
  onApprove: (doctorId: string) => void;
  onReject: (doctor: PendingDoctor) => void;
  loading?: boolean;
}

const PendingDoctorsTable: React.FC<PendingDoctorsTableProps> = ({
  doctors,
  onApprove,
  onReject,
  loading = false,
}) => {
  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return 'warning';
      case 'approved':
        return 'success';
      case 'rejected':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return 'Pendiente';
      case 'approved':
        return 'Aprobado';
      case 'rejected':
        return 'Rechazado';
      default:
        return status;
    }
  };

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Nombre</TableCell>
            <TableCell>Usuario</TableCell>
            <TableCell>Email</TableCell>
            <TableCell>Especialidad</TableCell>
            <TableCell>Estado</TableCell>
            <TableCell align="center">Acciones</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {doctors.map((doctor) => (
            <TableRow key={doctor.id}>
              <TableCell>{doctor.name}</TableCell>
              <TableCell>{doctor.name}</TableCell>
              <TableCell>{doctor.email || 'No especificado'}</TableCell>
              <TableCell>{doctor.specialty || 'No especificada'}</TableCell>
              <TableCell>
                <Chip
                  label={getStatusLabel(doctor.status)}
                  color={getStatusColor(doctor.status)}
                  size="small"
                />
              </TableCell>
              <TableCell align="center">
                {doctor.status === 'PENDING' && (
                  <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center' }}>
                    <Button
                      variant="contained"
                      color="success"
                      size="small"
                      onClick={() => onApprove(doctor.id)}
                      disabled={loading}
                    >
                      Aprobar
                    </Button>
                    <Button
                      variant="outlined"
                      color="error"
                      size="small"
                      onClick={() => onReject(doctor)}
                      disabled={loading}
                    >
                      Rechazar
                    </Button>
                  </Box>
                )}
              </TableCell>
            </TableRow>
          ))}
          {doctors.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} align="center">
                No hay médicos pendientes de aprobación
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default PendingDoctorsTable;
