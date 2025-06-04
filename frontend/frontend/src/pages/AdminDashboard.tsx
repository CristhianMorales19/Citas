import React, { useState, useEffect, useCallback } from 'react';
import { Typography, Paper } from '@mui/material';
import { adminService } from '../services/api';
import { PendingDoctor } from '../types/appointment';
import { LoadingSpinner, ErrorMessage, PageContainer } from '../components/common';
import { PendingDoctorsTable, DoctorRejectionDialog } from '../components/admin';

const AdminDashboard: React.FC = () => {
  const [pendingDoctors, setPendingDoctors] = useState<PendingDoctor[]>([]);
  const [selectedDoctor, setSelectedDoctor] = useState<PendingDoctor | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadPendingDoctors = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await adminService.getPendingDoctors();
      setPendingDoctors(data);
    } catch (error) {
      setError('Error al cargar los médicos pendientes');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPendingDoctors();
  }, [loadPendingDoctors]);

  const handleApprove = async (doctorId: string) => {
    try {
      await adminService.approveDoctor(doctorId);
      await loadPendingDoctors();
    } catch (error) {
      setError('Error al aprobar el médico');
    }
  };

  const handleReject = async (doctorId: string, reason: string) => {
    try {
      await adminService.rejectDoctor(doctorId);
      await loadPendingDoctors();
    } catch (error) {
      setError('Error al rechazar el médico');
    }
  };

  const handleOpenRejectDialog = (doctor: PendingDoctor) => {
    setSelectedDoctor(doctor);
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setSelectedDoctor(null);
    setDialogOpen(false);
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <PageContainer>
      <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#4a90e2' }}>
        Panel de Administración
      </Typography>

      {error && <ErrorMessage message={error} />}

      <Paper elevation={3} sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Médicos Pendientes de Aprobación
        </Typography>

        <PendingDoctorsTable
          doctors={pendingDoctors}
          onApprove={handleApprove}
          onReject={handleOpenRejectDialog}
        />
      </Paper>

      <DoctorRejectionDialog
        open={dialogOpen}
        doctor={selectedDoctor}
        onClose={handleCloseDialog}
        onConfirm={handleReject}
      />
    </PageContainer>
  );
};

export default AdminDashboard; 