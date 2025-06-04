import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  Avatar,
  IconButton,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import { PhotoCamera } from '@mui/icons-material';

interface DoctorPhotoUploadProps {
  currentPhotoUrl?: string;
  onPhotoUpload: (file: File) => Promise<void>;
  loading?: boolean;
}

const DoctorPhotoUpload: React.FC<DoctorPhotoUploadProps> = ({
  currentPhotoUrl,
  onPhotoUpload,
  loading = false,
}) => {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validaciones
    if (file.size > 5 * 1024 * 1024) { // 5MB
      setError('El archivo debe ser menor a 5MB');
      return;
    }

    if (!file.type.startsWith('image/')) {
      setError('Solo se permiten archivos de imagen');
      return;
    }

    try {
      setUploading(true);
      setError(null);
      await onPhotoUpload(file);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al subir la foto');
    } finally {
      setUploading(false);
    }
  };

  return (
    <Card>
      <CardHeader 
        title="Foto de Perfil"
        subheader="Agrega una foto profesional para tu perfil"
      />
      <CardContent>
        <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
          <Box position="relative">
            <Avatar
              src={currentPhotoUrl}
              sx={{ width: 120, height: 120 }}
            />
            <IconButton
              component="label"
              disabled={loading || uploading}
              sx={{
                position: 'absolute',
                bottom: 0,
                right: 0,
                backgroundColor: 'primary.main',
                color: 'white',
                '&:hover': {
                  backgroundColor: 'primary.dark',
                },
              }}
            >
              {uploading ? (
                <CircularProgress size={20} color="inherit" />
              ) : (
                <PhotoCamera />
              )}
              <input
                type="file"
                hidden
                accept="image/*"
                onChange={handleFileSelect}
              />
            </IconButton>
          </Box>

          {error && (
            <Alert severity="error" sx={{ width: '100%' }}>
              {error}
            </Alert>
          )}

          <Typography variant="body2" color="textSecondary" textAlign="center">
            Formatos permitidos: JPG, PNG<br />
            Tamaño máximo: 5MB
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
};

export default DoctorPhotoUpload;
