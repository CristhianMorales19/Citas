// Constantes de rutas para evitar hardcodeo
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DOCTORS: '/doctors',
  PENDING_APPROVAL: '/pending-approval',
  
  // Rutas de paciente
  PATIENT: {
    APPOINTMENTS: '/paciente/appointments',
    SEARCH: '/buscar-Doctor',
  },
  
  // Rutas de doctor
  DOCTOR: {
    PROFILE: '/Doctor/profile',
    APPOINTMENTS: '/Doctor/appointments',
    SCHEDULE: (doctorId: string) => `/Doctors/${doctorId}/schedule`,
    AVAILABILITY: (doctorId: string) => `/public/doctors/${doctorId}/availability`,
  },
  
  // Rutas de admin
  ADMIN: {
    DASHBOARD: '/admin/dashboard',
    PENDING_DOCTORS: '/Doctores-pendientes',
  },
  
  // Rutas públicas
  PUBLIC: {
    BOOK_APPOINTMENT: (doctorId: string) => `/appointments/book/${doctorId}`,
  },
  
  // Páginas adicionales
  ABOUT: '/about',
} as const;

// Función helper para obtener la ruta de redirección según el rol
export const getDefaultRouteForRole = (role: string): string => {
  const normalizedRole = role.toLowerCase();
  
  switch (normalizedRole) {
    case 'medico':
    case 'doctor':
      return ROUTES.DOCTOR.PROFILE;
    case 'admin':
      return ROUTES.ADMIN.DASHBOARD;
    case 'paciente':
    case 'patient':
      return ROUTES.PATIENT.APPOINTMENTS;
    default:
      return ROUTES.HOME;
  }
};
