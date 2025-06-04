import { User } from './auth';

export interface SearchParams {
  specialty?: string;
  location?: string;
  date?: string;
  time?: string;
}

export interface AvailableTimeSlot {
  time?: string;
  isAvailable: boolean;
}

export interface DoctorSearchResult {
  id: number;        // Changed from string to number to match backend Long
  name: string;
  specialty: string;
  location: string;
  consultationCost: number;
  photo?: string;
  photoUrl?: string; // Added to match backend
  presentation: string;
  availableSlots: {
    [date: string]: AvailableTimeSlot[];
  };
}

// Interfaz para un slot de tiempo en el horario semanal del médico
export interface DoctorTimeSlot {
  startTime: Date | null;
  endTime: Date | null;
  isAvailable: boolean;
  time?: string; // Propiedad opcional para mantener compatibilidad con otros componentes
}

// DTO para representar una entrada de horario en la API (matches backend)
export interface ScheduleDTO {
  id?: number;
  day: string;
  startTime: string;
  endTime: string;
}

// Frontend-friendly schedule structure
export interface WeeklySchedule {
  monday: DaySchedule;
  tuesday: DaySchedule;
  wednesday: DaySchedule;
  thursday: DaySchedule;
  friday: DaySchedule;
  saturday: DaySchedule;
  sunday: DaySchedule;
}

export interface DaySchedule {
  isAvailable: boolean;
  startTime?: string;
  endTime?: string;
}

// Interfaz para el horario semanal completo
export interface WeekSchedule {
  monday: DoctorTimeSlot;
  tuesday: DoctorTimeSlot;
  wednesday: DoctorTimeSlot;
  thursday: DoctorTimeSlot;
  friday: DoctorTimeSlot;
  saturday: DoctorTimeSlot;
  sunday: DoctorTimeSlot;
}

export interface DoctorProfile {
  id: string;
  userId: string;
  name: string;
  email: string;
  description: string;
  phone: string;
  address: string;
  specialty: string;
  consultationCost: number;
  location: string;
  schedule: WeekSchedule;
  weeklySchedule?: ScheduleDTO[];
  appointmentDuration: number;
  presentation: string;
  photoUrl?: string;
  profilePhotoUrl?: string;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
  profileConfigured?: boolean;
}

// Using the existing User interface from the file

export interface Doctor {
  id: string;
  name: string;
  user: User;
  especialidad: string;
  cedulaProfesional: string;
  descripcion: string;
  costoConsulta: number;
  calificacion: number;
  activo: boolean;
}

export interface Patient {
  id: string;
  user: User;
  fechaNacimiento?: string;
  telefono?: string;
  direccion?: string;
}

export interface Appointment {
  id: string;
  paciente: Patient;
  medico: Doctor;
  horario?: {
    id: string;
    dia: string;
    horaInicio: string;
    horaFin: string;
  };
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: 'PENDIENTE' | 'AGENDADA' | 'CONFIRMADA' | 'COMPLETADA' | 'CANCELADA' | 'NO_ASISTIO';
  motivoConsulta?: string;
  notas?: string;
  fechaCreacion?: string;
  fechaActualizacion?: string;
}

export interface AppointmentTimeSlot {
  time: string;
  isAvailable: boolean;
}

export interface ExtendedSchedule {
  date: string;
  timeSlots: AppointmentTimeSlot[];
}

export interface DoctorExtendedSchedule {
  id: string;
  name: string;
  specialty: string;
  schedule: ExtendedSchedule[];
}

export interface PendingDoctor {
  id: string;
  userId: string;
  name: string;
  email: string;
  specialty: string;
  presentation: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
} 