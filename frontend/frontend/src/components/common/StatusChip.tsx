import React from 'react';
import { Chip, ChipProps } from '@mui/material';

export type StatusVariant = 
  | 'success' 
  | 'warning' 
  | 'error' 
  | 'info' 
  | 'default'
  | 'pending'
  | 'confirmed'
  | 'cancelled'
  | 'completed';

interface StatusChipProps extends Omit<ChipProps, 'color' | 'variant'> {
  status: StatusVariant | string;
  variant?: 'filled' | 'outlined';
}

const statusConfig: Record<string, { color: ChipProps['color']; label?: string }> = {
  // Appointment statuses
  agendada: { color: 'primary', label: 'Agendada' },
  confirmada: { color: 'success', label: 'Confirmada' },
  pendiente: { color: 'warning', label: 'Pendiente' },
  cancelada: { color: 'error', label: 'Cancelada' },
  completada: { color: 'info', label: 'Completada' },
  
  // Generic statuses
  success: { color: 'success' },
  warning: { color: 'warning' },
  error: { color: 'error' },
  info: { color: 'info' },
  default: { color: 'default' },
  pending: { color: 'warning', label: 'Pendiente' },
  confirmed: { color: 'success', label: 'Confirmado' },
  cancelled: { color: 'error', label: 'Cancelado' },
  completed: { color: 'info', label: 'Completado' },
  
  // Common boolean states
  active: { color: 'success', label: 'Activo' },
  inactive: { color: 'error', label: 'Inactivo' },
  enabled: { color: 'success', label: 'Habilitado' },
  disabled: { color: 'error', label: 'Deshabilitado' },
};

export const StatusChip: React.FC<StatusChipProps> = ({
  status,
  variant = 'filled',
  label,
  size = 'small',
  ...props
}) => {
  const config = statusConfig[status.toLowerCase()] || statusConfig.default;
  const displayLabel = label || config.label || status;

  return (
    <Chip
      label={displayLabel}
      color={config.color}
      variant={variant}
      size={size}
      {...props}
    />
  );
};
