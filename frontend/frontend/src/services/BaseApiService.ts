import axios, { AxiosInstance, AxiosError } from 'axios';
import { ApiError } from '../types/api';

// Clase base para servicios API
export class BaseApiService {
  protected api: AxiosInstance;

  constructor(baseURL: string = '') {
    this.api = axios.create({
      baseURL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
      withCredentials: true,
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.api.interceptors.request.use((config) => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Response interceptor
    this.api.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  private handleApiError(error: AxiosError): void {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
  }

  protected handleError(error: unknown): ApiError {
    if (axios.isAxiosError(error)) {
      return {
        message: error.response?.data?.message || error.message || 'Error de conexión',
        status: error.response?.status,
        code: error.code,
      };
    }
    
    return {
      message: error instanceof Error ? error.message : 'Error desconocido',
    };
  }

  protected async request<T>(
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    url: string,
    data?: any
  ): Promise<T> {
    try {
      const response = await this.api.request({
        method,
        url,
        data,
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // Métodos helper
  protected get<T>(url: string): Promise<T> {
    return this.request<T>('GET', url);
  }

  protected post<T>(url: string, data?: any): Promise<T> {
    return this.request<T>('POST', url, data);
  }

  protected put<T>(url: string, data?: any): Promise<T> {
    return this.request<T>('PUT', url, data);
  }

  protected delete<T>(url: string): Promise<T> {
    return this.request<T>('DELETE', url);
  }
}
