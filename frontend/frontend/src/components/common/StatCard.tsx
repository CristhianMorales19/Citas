import React from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Avatar,
  useTheme,
} from '@mui/material';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: React.ReactNode;
  color?: 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';
  trend?: {
    value: number;
    isPositive: boolean;
    period?: string;
  };
  elevation?: number;
  variant?: 'outlined' | 'elevation';
  onClick?: () => void;
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  color = 'primary',
  trend,
  elevation = 1,
  variant = 'elevation',
  onClick,
}) => {
  const theme = useTheme();

  const getColorValue = (colorName: string) => {
    const paletteColor = theme.palette[colorName as keyof typeof theme.palette];
    // @ts-ignore
    return typeof paletteColor === 'object' && paletteColor && 'main' in paletteColor
      ? (paletteColor as any).main
      : theme.palette.primary.main;
  };

  const cardColor = getColorValue(color);

  return (
    <Card
      elevation={variant === 'elevation' ? elevation : 0}
      variant={variant}
      onClick={onClick}
      sx={{
        cursor: onClick ? 'pointer' : 'default',
        transition: 'transform 0.2s ease-in-out',
        '&:hover': onClick ? {
          transform: 'translateY(-2px)',
          boxShadow: theme.shadows[4],
        } : undefined,
      }}
    >
      <CardContent sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
          <Box sx={{ flex: 1 }}>
            <Typography
              variant="body2"
              color="text.secondary"
              gutterBottom
              sx={{ fontSize: '0.875rem', fontWeight: 500 }}
            >
              {title}
            </Typography>
            
            <Typography
              variant="h4"
              component="div"
              sx={{
                color: cardColor,
                fontWeight: 'bold',
                mb: 0.5,
              }}
            >
              {value}
            </Typography>

            {subtitle && (
              <Typography variant="body2" color="text.secondary">
                {subtitle}
              </Typography>
            )}

            {trend && (
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                <Typography
                  variant="body2"
                  sx={{
                    color: trend.isPositive ? 'success.main' : 'error.main',
                    fontWeight: 500,
                  }}
                >
                  {trend.isPositive ? '+' : ''}{trend.value}%
                </Typography>
                {trend.period && (
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ ml: 1 }}
                  >
                    {trend.period}
                  </Typography>
                )}
              </Box>
            )}
          </Box>

          {icon && (
            <Avatar
              sx={{
                bgcolor: `${cardColor}20`,
                color: cardColor,
                width: 56,
                height: 56,
              }}
            >
              {icon}
            </Avatar>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};
