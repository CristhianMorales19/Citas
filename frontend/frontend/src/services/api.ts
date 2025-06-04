import axios from 'axios';
import type { 
  Appointment, 
  DoctorProfile, 
  DoctorExtendedSchedule,
  SearchParams,
  DoctorSearchResult,
  PendingDoctor
} from '../types/appointment';
import type { User } from '../types/auth';

const api = axios.create({
  baseURL: 'http://localhost:8080', // Fixed: use full URL instead of empty string
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // Add timeout to prevent hanging requests
  withCredentials: true // Include credentials (cookies) with requests
});

// Interceptor para agregar el token a las peticiones
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para manejar errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const servicioAutenticacion = {
  iniciarSesion: async (credenciales: { nombreUsuario: string; contrasena: string }) => {
    try {
      console.log('Iniciando sesión con credenciales:', { 
        nombreUsuario: credenciales.nombreUsuario,
        contrasena: '[PROTECTED]' // Don't log actual password
      });
      
      const response = await api.post('/auth/login', {
        nombreUsuario: credenciales.nombreUsuario,
        contrasena: credenciales.contrasena
      });

      console.log('Respuesta del servidor:', response.data);
      
      if (!response.data.success) {
        const errorMessage = response.data.message || 'Error en el inicio de sesión';
        console.error('Error en la lógica de negocio:', errorMessage);
        throw new Error(errorMessage);
      }

      // Store the token if present
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
      }
      
      return response.data;
    } catch (error) {
      console.error('Error en inicio de sesión:', error);
      throw new Error(error instanceof Error ? error.message : 'Error en el proceso de inicio de sesión');
    }
  },

  registrar: async (datos: any) => {
    try {
      if (datos.password !== datos.confirmPassword) {
        throw new Error('Las contraseñas no coinciden');
      }

      // Crear un objeto con los campos necesarios para el registro
      const registerRequest = {
        username: datos.username,
        password: datos.password,
        confirmPassword: datos.confirmPassword, // Asegurarse de incluir confirmPassword
        name: datos.name,
        role: datos.role || 'paciente' // Usar 'paciente' como valor por defecto si no se especifica
      };
      
      console.log('Datos de registro completos:', registerRequest);

      console.log('Enviando datos de registro:', registerRequest);
      
      // Usar el endpoint alternativo para registro
      console.log('Usando endpoint alternativo para registro');
      const response = await api.post('/api/users/register-alternative', registerRequest);
      
      console.log('Respuesta del servidor:', response.data);
      
      return response.data;
    } catch (error) {
      console.error('Error durante el registro:', error);
      throw new Error(error instanceof Error ? error.message : 'Error en el proceso de registro');
    }
  },

  obtenerUsuarioActual: async () => {
    const response = await api.get<User>('/auth/me');
    return response.data;
  },
};

export const appointmentService = {
  getAppointments: async () => {
    const response = await api.get<Appointment[]>('/api/appointments');
    return response.data;
  },

  getAppointment: async (id: string) => {
    const response = await api.get<Appointment>(`/api/appointments/${id}`);
    return response.data;
  },

  createAppointment: async (data: Omit<Appointment, 'id' | 'status'>) => {
    const response = await api.post<Appointment>('/api/appointments', data);
    return response.data;
  },

  updateAppointment: async (id: string, data: Partial<Appointment>) => {
    const response = await api.put<Appointment>(`/api/appointments/${id}`, data);
    return response.data;
  },

  deleteAppointment: async (id: string) => {
    await api.delete(`/api/appointments/${id}`);
  },

  getDoctorProfile: async (DoctorId: string) => {
    const response = await api.get<DoctorProfile>(`/api/medicos/${DoctorId}`);
    return response.data;
  },

  updateDoctorProfile: async (DoctorId: string, data: Partial<DoctorProfile>) => {
    const response = await api.put<DoctorProfile>(`/api/medicos/${DoctorId}`, data);
    return response.data;
  },

  getDoctorExtendedSchedule: async (DoctorId: string, startDate: string, endDate: string) => {
    const response = await api.get<DoctorExtendedSchedule>(`/api/medicos/${DoctorId}/schedule`, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  searchDoctors: async (params: SearchParams) => {
    console.log('Llamando a searchDoctors con parámetros:', params);
    const response = await api.get<DoctorSearchResult[]>('/public/doctors', { params });
    console.log('Respuesta de searchDoctors:', response.data);
    return response.data;
  },

  // Métodos temporales de depuración
  getAllDoctorsDebug: async () => {
    console.log('Llamando a getAllDoctorsDebug');
    const response = await api.get<DoctorSearchResult[]>('/public/doctors/debug');
    console.log('Respuesta de getAllDoctorsDebug:', response.data);
    return response.data;
  },

  getApprovedDoctorsDebug: async () => {
    console.log('Llamando a getApprovedDoctorsDebug');
    const response = await api.get<DoctorSearchResult[]>('/public/doctors/approved');
    console.log('Respuesta de getApprovedDoctorsDebug:', response.data);
    return response.data;
  },

  bookAppointment: async (DoctorId: string, date: string, time: string, notes?: string) => {
    const response = await api.post<Appointment>('/api/appointments', {
      doctorId: parseInt(DoctorId),
      date,
      time,
      notes
    });
    return response.data;
  },

  getpacienteAppointments: async (): Promise<Appointment[]> => {
    try {
      const response = await api.get<Appointment[]>('/pacientes/citas');
      return response.data;
    } catch (error) {
      console.error('Error al obtener las citas del paciente:', error);
      throw error;
    }
  },

  getAvailableSlots: async (doctorId: string, startDate: string, endDate: string) => {
    try {
      console.log(`Buscando horarios disponibles para doctor ${doctorId} desde ${startDate} hasta ${endDate}`);
      const response = await api.get<Record<string, Array<{time: string, isAvailable: boolean}>>>(`/api/appointments/available`, {
        params: { 
          doctorId,
          startDate,
          endDate
        }
      });
      console.log('Respuesta de horarios disponibles:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching available slots:', error);
      return {}; // Retornar un objeto vacío en caso de error
    }
  },

  cancelAppointment: async (appointmentId: string) => {
    await api.delete(`/api/appointments/${appointmentId}`);
  },
};

export const DoctorService = {
  getProfile: async () => {
    try {
      console.log('Fetching doctor profile...');
      const token = localStorage.getItem('token');
      console.log('Using token:', token ? 'Token exists' : 'No token found');
      
      const response = await api.get<DoctorProfile>('/api/medicos/profile', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      console.log('Profile data received:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error fetching doctor profile:', error);
      
      if (axios.isAxiosError(error)) {
        // The request was made and the server responded with a status code
        // that falls out of the range of 2xx
        if (error.response) {
          console.error('Response status:', error.response.status);
          console.error('Response data:', error.response.data);
          console.error('Response headers:', error.response.headers);
        } else if (error.request) {
          // The request was made but no response was received
          console.error('No response received from server');
          console.error('Request:', error.request);
        } else {
          // Something happened in setting up the request that triggered an Error
          console.error('Error message:', error.message);
        }
      } else {
        // Non-Axios error
        console.error('Unexpected error:', error);
      }
      
      throw error;
    }
  },

  updateProfile: async (data: Partial<DoctorProfile>) => {
    const response = await api.put<DoctorProfile>('/api/medicos/profile', data);
    return response.data;
  },

  uploadProfilePhoto: async (formData: FormData) => {
    // Crear un cliente axios dedicado para esta petición multipart
    const uploadClient = axios.create({
      baseURL: 'http://localhost:8080/api',
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
    });
    
    const response = await uploadClient.post<{url: string}>('/medicos/upload-photo', formData);
    return response.data;
  },

  getAppointments: async (): Promise<Appointment[]> => {
    try {
      console.log('Obteniendo citas del médico...');
      const response = await api.get('/api/medicos/citas');
      console.log('Respuesta de la API:', response.data);
      
      // Transformar la respuesta de la API para que coincida con la interfaz Appointment
      const citas = Array.isArray(response.data) ? response.data.map((cita: any) => ({
        id: cita.id,
        fecha: cita.fecha || cita.date,
        horaInicio: cita.horaInicio || cita.time || '00:00',
        horaFin: cita.horaFin || cita.endTime || '00:00',
        estado: (cita.estado || cita.status || 'PENDIENTE').toUpperCase(),
        motivoConsulta: cita.motivoConsulta || cita.reason || '',
        notas: cita.notas || cita.notes || '',
        fechaCreacion: cita.fechaCreacion || new Date().toISOString(),
        fechaActualizacion: cita.fechaActualizacion || new Date().toISOString(),
        motivoCancelacion: cita.motivoCancelacion || '',
        paciente: cita.paciente ? {
          id: cita.paciente.id || 'unknown',
          user: {
            id: cita.paciente.usuario?.id || cita.paciente.id || 'unknown',
            name: `${cita.paciente.usuario?.nombre || ''} ${cita.paciente.usuario?.apellido || ''}`.trim() || 'Paciente',
            email: cita.paciente.usuario?.email || '',
            role: 'paciente' as const
          },
          fechaNacimiento: cita.paciente.fechaNacimiento || '',
          telefono: cita.paciente.telefono || '',
          direccion: cita.paciente.direccion || ''
        } : {
          id: 'unknown',
          user: {
            id: 'unknown',
            name: 'Paciente',
            email: '',
            role: 'paciente' as const
          },
          fechaNacimiento: '',
          telefono: '',
          direccion: ''
        },
        medico: cita.medico ? {
          id: cita.medico.id || 'unknown',
          user: {
            id: cita.medico.usuario?.id || cita.medico.id || 'unknown',
            name: `${cita.medico.usuario?.nombre || ''} ${cita.medico.usuario?.apellido || ''}`.trim() || 'Médico',
            email: cita.medico.usuario?.email || '',
            role: 'Doctor' as const
          },
          especialidad: cita.medico.especialidad || 'Especialidad no especificada',
          cedulaProfesional: cita.medico.cedulaProfesional || '',
          descripcion: cita.medico.descripcion || '',
          costoConsulta: cita.medico.costoConsulta || 0,
          calificacion: cita.medico.calificacion || 0,
          activo: cita.medico.activo !== undefined ? cita.medico.activo : true
        } : {
          id: 'unknown',
          user: {
            id: 'unknown',
            name: 'Médico',
            email: '',
            role: 'Doctor' as const
          },
          especialidad: 'Especialidad no especificada',
          cedulaProfesional: '',
          descripcion: '',
          costoConsulta: 0,
          calificacion: 0,
          activo: true
        },
        // Incluir cualquier campo adicional que pueda ser necesario
        ...(cita.horario && { horario: cita.horario })
      })) : [];
      
      console.log('Citas procesadas:', citas);
      return citas;
    } catch (error: any) {
      console.error('Error al obtener las citas del médico:', error);
      if (error.response) {
        console.error('Detalles del error:', error.response.data);
        console.error('Estado HTTP:', error.response.status);
      }
      throw new Error('No se pudieron cargar las citas. Por favor, intente de nuevo más tarde.');
    }
  },

  updateAppointment: async (id: string, data: Partial<Appointment>) => {
    try {
      // Create a copy of the data to avoid mutating the original
      const updateData: Partial<Appointment> = { ...data };
      
      // Ensure we're using the correct property names for the backend
      const backendData: any = {
        ...updateData,
        // Map any frontend property names to backend property names if needed
        estado: updateData.estado,
        notas: updateData.notas,
        motivoConsulta: updateData.motivoConsulta,
        fecha: updateData.fecha,
        horaInicio: updateData.horaInicio,
        horaFin: updateData.horaFin
      };
      
      // Remove undefined values to avoid sending them to the backend
      Object.keys(backendData).forEach(key => {
        if (backendData[key] === undefined) {
          delete backendData[key];
        }
      });
      
      const response = await api.put(`/api/appointments/${id}`, backendData);
      
      // Transform the response to match our frontend interface
      const updatedAppointment = response.data;
      return {
        ...updatedAppointment,
        estado: updatedAppointment.estado || updatedAppointment.status,
        notas: updatedAppointment.notas || updatedAppointment.notes,
        fecha: updatedAppointment.fecha || updatedAppointment.date,
        horaInicio: updatedAppointment.horaInicio || updatedAppointment.time,
        horaFin: updatedAppointment.horaFin || updatedAppointment.endTime,
        motivoConsulta: updatedAppointment.motivoConsulta || updatedAppointment.reason
      };
    } catch (error) {
      console.error('Error al actualizar la cita:', error);
      throw error;
    }
  },
  
  // Método específico para actualizar el horario semanal
  updateSchedule: async (schedule: any) => {
    const response = await api.put<DoctorProfile>('/api/medicos/schedule', { schedule });
    return response.data;
  }
};

export const adminService = {
  getPendingDoctors: async () => {
    const response = await api.get<PendingDoctor[]>('/admin/medicos/pendientes');
    return response.data;
  },

  approveDoctor: async (DoctorId: string) => {
    const response = await api.post<PendingDoctor>(`/admin/medicos/${DoctorId}/aprobar`);
    return response.data;
  },

  rejectDoctor: async (DoctorId: string) => {
    const response = await api.post<PendingDoctor>(`/admin/medicos/${DoctorId}/rechazar`);
    return response.data;
  },
};

export default api;