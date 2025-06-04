import React from 'react';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  Grid,
  FormControlLabel,
  Switch,
  Typography,
  Divider,
} from '@mui/material';
import { TimePicker } from '@mui/x-date-pickers/TimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { es } from 'date-fns/locale';
import { WeekSchedule } from '../../types/appointment';

interface DoctorScheduleConfigProps {
  schedule: WeekSchedule;
  onScheduleChange: (newSchedule: WeekSchedule) => void;
}

const DoctorScheduleConfig: React.FC<DoctorScheduleConfigProps> = ({
  schedule,
  onScheduleChange,
}) => {
  const dayNames = {
    monday: 'Lunes',
    tuesday: 'Martes',
    wednesday: 'Miércoles',
    thursday: 'Jueves',
    friday: 'Viernes',
    saturday: 'Sábado',
    sunday: 'Domingo',
  };

  const handleDayToggle = (day: keyof WeekSchedule) => {
    const newSchedule = {
      ...schedule,
      [day]: {
        ...schedule[day],
        isAvailable: !schedule[day].isAvailable,
        startTime: !schedule[day].isAvailable ? null : schedule[day].startTime,
        endTime: !schedule[day].isAvailable ? null : schedule[day].endTime,
      },
    };
    onScheduleChange(newSchedule);
  };

  const handleTimeChange = (day: keyof WeekSchedule, timeType: 'startTime' | 'endTime', time: Date | null) => {
    const newSchedule = {
      ...schedule,
      [day]: {
        ...schedule[day],
        [timeType]: time,
      },
    };
    onScheduleChange(newSchedule);
  };

  return (
    <Card>
      <CardHeader 
        title="Configuración de Horarios"
        subheader="Define tus horarios de consulta para cada día de la semana"
      />
      <CardContent>
        <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={es}>
          <Grid container spacing={2}>
            {Object.entries(dayNames).map(([day, dayName]) => {
              const dayKey = day as keyof WeekSchedule;
              const daySchedule = schedule[dayKey];
              
              return (
                <Grid item xs={12} key={day}>
                  <Card variant="outlined">
                    <CardContent>
                      <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
                        <Typography variant="h6">{dayName}</Typography>
                        <FormControlLabel
                          control={
                            <Switch
                              checked={daySchedule.isAvailable}
                              onChange={() => handleDayToggle(dayKey)}
                              color="primary"
                            />
                          }
                          label={daySchedule.isAvailable ? 'Disponible' : 'No disponible'}
                        />
                      </Box>
                      
                      {daySchedule.isAvailable && (
                        <Grid container spacing={2}>
                          <Grid item xs={6}>
                            <TimePicker
                              label="Hora de inicio"
                              value={daySchedule.startTime}
                              onChange={(time) => handleTimeChange(dayKey, 'startTime', time)}
                              ampm={false}
                              slotProps={{
                                textField: { fullWidth: true, size: 'small' }
                              }}
                            />
                          </Grid>
                          <Grid item xs={6}>
                            <TimePicker
                              label="Hora de fin"
                              value={daySchedule.endTime}
                              onChange={(time) => handleTimeChange(dayKey, 'endTime', time)}
                              ampm={false}
                              slotProps={{
                                textField: { fullWidth: true, size: 'small' }
                              }}
                            />
                          </Grid>
                        </Grid>
                      )}
                    </CardContent>
                  </Card>
                  {day !== 'sunday' && <Divider sx={{ my: 1 }} />}
                </Grid>
              );
            })}
          </Grid>
        </LocalizationProvider>
      </CardContent>
    </Card>
  );
};

export default DoctorScheduleConfig;
