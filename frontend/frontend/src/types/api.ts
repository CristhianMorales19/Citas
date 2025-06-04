// Tipos base para las respuestas de la API
import { User } from './auth';

export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
}

export interface PaginatedResponse<T> extends ApiResponse<T[]> {
  totalElements?: number;
  totalPages?: number;
  currentPage?: number;
  pageSize?: number;
}

// Tipos de error
export interface ApiError {
  message: string;
  status?: number;
  code?: string;
}

// Tipos de autenticación
export interface LoginCredentials {
  nombreUsuario: string;
  contrasena: string;
}

export interface RegisterData {
  username: string;
  password: string;
  confirmPassword: string;
  name: string;
  role: 'paciente' | 'medico' | 'admin';
}

export interface AuthResponse extends ApiResponse {
  token?: string;
  user?: User;
  doctorStatus?: string;
}

// Re-exportar tipos existentes
export * from './appointment';
export type { User } from './auth';
