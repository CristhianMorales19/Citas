export interface DoctorProfile {
  id: number;        // Changed from string to number to match backend Long
  name: string;
  specialty: string;
  consultationCost: number;
  location: string;
  schedule: WeeklySchedule;
  appointmentDuration: number; // en minutos
  photo?: string;
  photoUrl?: string;  // Added to match backend
  presentation: string;
  isApproved: boolean;
}

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
  startTime?: string; // formato "HH:mm"
  endTime?: string; // formato "HH:mm"
}

export interface UpdateDoctorProfileData {
  specialty: string;
  consultationCost: number;
  location: string;
  schedule: WeeklySchedule;
  appointmentDuration: number;
  photo?: string;
  presentation: string;
} 