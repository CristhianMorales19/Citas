import { DoctorSearchResult, AvailableTimeSlot } from './appointment';

export interface User {
  id: string;
  email: string;
  name: string;
  role: 'paciente' | 'Doctor' | 'medico' | 'admin';
  doctorProfile?: {
    profileConfigured: boolean;
    status?: 'PENDING' | 'APPROVED' | 'REJECTED';
  };
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterCredentials {
  username: string;
  password: string;
  confirmPassword: string;
  name: string;
  role: 'paciente' | 'Doctor';
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface AuthContextType {
  usuario: User | null;
  estaAutenticado: boolean;
  login: (credentials: LoginCredentials) => Promise<AuthResponse>;
  logout: () => void;
  registrar: (credentials: RegisterCredentials) => Promise<AuthResponse>;
  handleDoctorClick: (Doctor: DoctorSearchResult) => void;
  handleSlotClick: (slot: AvailableTimeSlot) => void;
  handleLocationChange: (e: any) => void;
  handleDateChange: (e: any) => void;
}
