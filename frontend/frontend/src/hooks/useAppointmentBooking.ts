import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { appointmentService } from '../services/api';
import { useAutenticacion } from '../contexts/AuthContext';
import { DoctorProfile } from '../types/appointment';

interface UseAppointmentBookingProps {
  doctorId: string;
  date: string;
  time: string;
  redirectPath?: string;
}

export const useAppointmentBooking = ({
  doctorId,
  date,
  time,
  redirectPath = '/paciente/appointments'
}: UseAppointmentBookingProps) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [doctorInfo, setDoctorInfo] = useState<DoctorProfile | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const { estaAutenticado } = useAutenticacion();

  const loadDoctorInfo = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await appointmentService.getDoctorProfile(doctorId);
      setDoctorInfo(data);
    } catch (error) {
      setError('Error al cargar la información del Doctor');
    } finally {
      setLoading(false);
    }
  }, [doctorId]);

  const confirmAppointment = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      await appointmentService.bookAppointment(doctorId, date, time);
      setSuccess('Cita agendada correctamente');
      setTimeout(() => {
        navigate(redirectPath);
      }, 2000);
    } catch (error) {
      setError('Error al agendar la cita');
    } finally {
      setLoading(false);
    }
  }, [doctorId, date, time, navigate, redirectPath]);

  const cancelBooking = useCallback(() => {
    navigate('/');
  }, [navigate]);

  useEffect(() => {
    if (!estaAutenticado) {
      navigate('/login', { state: { from: location.pathname + location.search } });
      return;
    }

    if (!doctorId || !date || !time) {
      navigate('/');
      return;
    }

    loadDoctorInfo();
  }, [doctorId, date, time, estaAutenticado, loadDoctorInfo, location.pathname, location.search, navigate]);

  return {
    loading,
    error,
    success,
    doctorInfo,
    confirmAppointment,
    cancelBooking,
  };
};
