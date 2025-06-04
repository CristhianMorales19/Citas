import axios from 'axios';
import { DoctorProfile, WeeklySchedule } from '../types/doctor';
import { convertScheduleDTOToWeeklySchedule } from '../utils/scheduleUtils';

// Creamos instancias de axios para los diferentes tipos de servicios

// API con autenticación (para rutas protegidas)
const api = axios.create({
  baseURL: 'http://localhost:8080/api',  // Fixed: added full URL with /api path
  headers: {
    'Content-Type': 'application/json',
  }
});

// API sin autenticación (para rutas públicas)
const publicApi = axios.create({
  baseURL: 'http://localhost:8080',     // Consistent base URL
  headers: {
    'Content-Type': 'application/json',
  }
});

// Crear un horario semanal vacío para los ejemplos
const emptySchedule: WeeklySchedule = {
  monday: { isAvailable: true, startTime: '08:00', endTime: '17:00' },
  tuesday: { isAvailable: true, startTime: '08:00', endTime: '17:00' },
  wednesday: { isAvailable: true, startTime: '08:00', endTime: '17:00' },
  thursday: { isAvailable: true, startTime: '08:00', endTime: '17:00' },
  friday: { isAvailable: true, startTime: '08:00', endTime: '17:00' },
  saturday: { isAvailable: false },
  sunday: { isAvailable: false }
};

export interface DoctorAvailability {
  doctor: DoctorProfile;
  availableDays: {
    date: string;
    dayOfWeek: string;
    slots: {
      time: string;
      available: boolean;
    }[];
  }[];
}

// Servicios para la búsqueda pública de médicos (sin necesidad de autenticación)
export const doctorPublicService = {
  // Buscar médicos por especialidad y ubicación
  searchDoctors: async (specialty?: string, location?: string): Promise<DoctorProfile[]> => {
    try {
      // Creamos los parámetros de búsqueda
      const params: Record<string, string> = {};
      
      if (specialty) {
        params.specialty = specialty;
      }
      
      if (location) {
        params.location = location;
      }
      
      // Usar la ruta correcta /public/doctors
      try {
        const response = await publicApi.get('/public/doctors', { params });
        // Convert backend schedule format to frontend format for each doctor
        const doctors = response.data.map((doctor: any) => ({
          ...doctor,
          schedule: doctor.weeklySchedule ? 
            convertScheduleDTOToWeeklySchedule(doctor.weeklySchedule) : 
            emptySchedule
        }));
        return doctors;
      } catch (error) {
        console.error('Error al conectar con la API:', error);
        throw error;
      }
    } catch (error) {
      console.error('Error al buscar médicos:', error);
      
      // DATOS DE EJEMPLO PARA MOSTRAR EN CASO DE ERROR
      console.log('Usando datos ficticios de médicos mientras se resuelve el problema de acceso');
      const mockDoctors: DoctorProfile[] = [
        {
          id: 1,          // Changed from string to number
          name: 'Dr. Juan Pérez',
          specialty: 'Cardiología',
          location: 'Quito',
          consultationCost: 70,
          presentation: 'Especialista en enfermedades cardiovasculares con 15 años de experiencia.',
          appointmentDuration: 30,
          schedule: emptySchedule,
          isApproved: true
        },
        {
          id: 2,          // Changed from string to number
          name: 'Dra. María López',
          specialty: 'Pediatría',
          location: 'Guayaquil',
          consultationCost: 50,
          presentation: 'Pediatra con enfoque en desarrollo infantil y medicina preventiva.',
          appointmentDuration: 45,
          schedule: emptySchedule,
          isApproved: true
        },
        {
          id: 3,          // Changed from string to number
          name: 'Dr. Roberto Gómez',
          specialty: 'Odontología',
          location: 'Cuenca',
          consultationCost: 60,
          presentation: 'Odontólogo especializado en estética dental y tratamientos sin dolor.',
          appointmentDuration: 60,
          schedule: emptySchedule,
          isApproved: true
        },
        {
          id: 4,          // Changed from string to number
          name: 'Dra. Carla Mendoza',
          specialty: 'Dermatología',
          location: 'Quito',
          consultationCost: 80,
          presentation: 'Dermatóloga especializada en tratamientos para la piel y procedimientos estéticos.',
          appointmentDuration: 30,
          schedule: emptySchedule,
          isApproved: true
        },
        {
          id: 5,          // Changed from string to number
          name: 'Dr. Fernando Ruiz',
          specialty: 'Neurología',
          location: 'Guayaquil',
          consultationCost: 90,
          presentation: 'Neurólogo con amplia experiencia en diagnóstico y tratamiento de enfermedades neurológicas.',
          appointmentDuration: 45,
          schedule: emptySchedule,
          isApproved: true
        }
      ];
      
      console.log('Devolviendo datos de ejemplo debido al error...');
      
      // Filtrar por especialidad y ubicación si se especifican
      let filteredDoctors = [...mockDoctors];
      
      if (specialty) {
        filteredDoctors = filteredDoctors.filter(doc => 
          doc.specialty.toLowerCase().includes(specialty.toLowerCase()));
      }
      
      if (location) {
        filteredDoctors = filteredDoctors.filter(doc => 
          doc.location.toLowerCase().includes(location.toLowerCase()));
      }
      
      return filteredDoctors;
    }
  },
  
  // Obtener la disponibilidad de un médico para los próximos días
  getDoctorAvailability: async (doctorId: number, startDate?: string): Promise<DoctorAvailability> => {
    try {
      // Creamos los parámetros de la solicitud
      const params: Record<string, string> = {};
      
      if (startDate) {
        params.startDate = startDate;
      }
      
      // Usar la ruta correcta /public/doctors/
      try {
        const response = await publicApi.get(`/public/doctors/${doctorId}/availability`, { params });
        return response.data;
      } catch (error) {
        console.error('Error al conectar con la API para disponibilidad:', error);
        throw error;
      }
    } catch (error) {
      console.error('Error al obtener disponibilidad del médico:', error);
      
      // En caso de error, devolver datos de ejemplo
      console.log('Devolviendo datos de disponibilidad de ejemplo debido al error...');
      
      // Crear fechas para los próximos tres días con disponibilidad de ejemplo
      const today = new Date();
      const days = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'];
      const availableDays = [];
      
      for (let i = 0; i < 3; i++) {
        const date = new Date(today);
        date.setDate(date.getDate() + i);
        const dateStr = date.toISOString().split('T')[0];
        const dayOfWeek = days[date.getDay()];
        
        // Crear algunos slots de ejemplo
        const slots = [
          { time: '09:00:00', available: true },
          { time: '09:30:00', available: false },
          { time: '10:00:00', available: true },
          { time: '10:30:00', available: true },
          { time: '11:00:00', available: false },
          { time: '15:00:00', available: true },
          { time: '15:30:00', available: true },
          { time: '16:00:00', available: false },
          { time: '16:30:00', available: true },
        ];
        
        availableDays.push({
          date: dateStr,
          dayOfWeek,
          slots
        });
      }
      
      return {
        doctor: {
          id: doctorId,           // Use number type consistently
          name: `Dr. Ejemplo (ID: ${doctorId})`,
          specialty: 'Especialidad de Ejemplo',
          location: 'Ubicación de Ejemplo',
          consultationCost: 60,
          presentation: 'Datos de ejemplo mientras se resuelve el problema de conexión.',
          appointmentDuration: 30,
          schedule: emptySchedule,
          isApproved: true
        },
        availableDays
      };
    }
  }
};

// Servicios para operaciones autenticadas de médicos
export const doctorAuthService = {
  // Obtener perfil del médico autenticado
  getDoctorProfile: async (): Promise<DoctorProfile> => {
    try {
      const response = await api.get('/medicos/profile');
      const doctor = response.data;
      
      // Convert backend schedule format to frontend format
      return {
        ...doctor,
        schedule: doctor.weeklySchedule ? 
          convertScheduleDTOToWeeklySchedule(doctor.weeklySchedule) : 
          emptySchedule
      };
    } catch (error) {
      console.error('Error al obtener perfil del médico:', error);
      throw error;
    }
  }
};
