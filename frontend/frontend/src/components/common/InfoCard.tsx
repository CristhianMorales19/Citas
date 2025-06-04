import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  Typography,
  Box,
  Divider,
  CardActions,
} from '@mui/material';

interface InfoCardField {
  label: string;
  value: React.ReactNode;
  icon?: React.ReactNode;
  fullWidth?: boolean;
}

interface InfoCardProps {
  title?: string;
  subtitle?: string;
  avatar?: React.ReactNode;
  headerAction?: React.ReactNode;
  fields?: InfoCardField[];
  children?: React.ReactNode;
  actions?: React.ReactNode;
  elevation?: number;
  variant?: 'outlined' | 'elevation';
  maxWidth?: number | string;
  image?: string;
  imageHeight?: number;
}

export const InfoCard: React.FC<InfoCardProps> = ({
  title,
  subtitle,
  avatar,
  headerAction,
  fields = [],
  children,
  actions,
  elevation = 1,
  variant = 'elevation',
  maxWidth,
  image,
  imageHeight = 140,
}) => {
  return (
    <Card
      elevation={variant === 'elevation' ? elevation : 0}
      variant={variant}
      sx={{ maxWidth, width: '100%' }}
    >
      {image && (
        <Box
          sx={{
            height: imageHeight,
            backgroundImage: `url(${image})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />
      )}

      {(title || subtitle || avatar || headerAction) && (
        <CardHeader
          avatar={avatar}
          action={headerAction}
          title={title}
          subheader={subtitle}
          titleTypographyProps={{
            variant: 'h6',
            component: 'h2',
          }}
          subheaderTypographyProps={{
            variant: 'body2',
            color: 'text.secondary',
          }}
        />
      )}

      <CardContent>
        {fields.length > 0 && (
          <Box sx={{ mb: children ? 2 : 0 }}>
            {fields.map((field, index) => (
              <Box
                key={index}
                sx={{
                  display: 'flex',
                  alignItems: field.fullWidth ? 'flex-start' : 'center',
                  mb: 1.5,
                  flexDirection: field.fullWidth ? 'column' : 'row',
                  gap: field.fullWidth ? 0.5 : 2,
                }}
              >
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1,
                    minWidth: field.fullWidth ? 'auto' : 120,
                  }}
                >
                  {field.icon}
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ fontWeight: 500 }}
                  >
                    {field.label}:
                  </Typography>
                </Box>
                <Typography
                  variant="body2"
                  sx={{
                    flex: 1,
                    wordBreak: 'break-word',
                  }}
                >
                  {field.value}
                </Typography>
              </Box>
            ))}
          </Box>
        )}

        {children}
      </CardContent>

      {actions && (
        <>
          <Divider />
          <CardActions sx={{ p: 2 }}>
            {actions}
          </CardActions>
        </>
      )}
    </Card>
  );
};
