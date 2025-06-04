import { ScheduleDTO, WeeklySchedule, DaySchedule } from '../types/appointment';

/**
 * Converts backend ScheduleDTO array to frontend WeeklySchedule object
 */
export function convertScheduleDTOToWeeklySchedule(scheduleList: ScheduleDTO[]): WeeklySchedule {
  const emptyDay: DaySchedule = { isAvailable: false };
  
  const weeklySchedule: WeeklySchedule = {
    monday: { ...emptyDay },
    tuesday: { ...emptyDay },
    wednesday: { ...emptyDay },
    thursday: { ...emptyDay },
    friday: { ...emptyDay },
    saturday: { ...emptyDay },
    sunday: { ...emptyDay }
  };

  if (!scheduleList || scheduleList.length === 0) {
    return weeklySchedule;
  }

  const dayMapping: { [key: string]: keyof WeeklySchedule } = {
    'MONDAY': 'monday',
    'TUESDAY': 'tuesday',
    'WEDNESDAY': 'wednesday',
    'THURSDAY': 'thursday',
    'FRIDAY': 'friday',
    'SATURDAY': 'saturday',
    'SUNDAY': 'sunday'
  };

  scheduleList.forEach(schedule => {
    const dayKey = dayMapping[schedule.day.toUpperCase()];
    if (dayKey) {
      weeklySchedule[dayKey] = {
        isAvailable: true,
        startTime: schedule.startTime,
        endTime: schedule.endTime
      };
    }
  });

  return weeklySchedule;
}

/**
 * Converts frontend WeeklySchedule object to backend ScheduleDTO array
 */
export function convertWeeklyScheduleToScheduleDTO(weeklySchedule: WeeklySchedule): ScheduleDTO[] {
  const dayMapping: { [key in keyof WeeklySchedule]: string } = {
    monday: 'MONDAY',
    tuesday: 'TUESDAY',
    wednesday: 'WEDNESDAY',
    thursday: 'THURSDAY',
    friday: 'FRIDAY',
    saturday: 'SATURDAY',
    sunday: 'SUNDAY'
  };

  const scheduleList: ScheduleDTO[] = [];

  Object.entries(weeklySchedule).forEach(([day, schedule]) => {
    if (schedule.isAvailable && schedule.startTime && schedule.endTime) {
      scheduleList.push({
        day: dayMapping[day as keyof WeeklySchedule],
        startTime: schedule.startTime,
        endTime: schedule.endTime
      });
    }
  });

  return scheduleList;
}
