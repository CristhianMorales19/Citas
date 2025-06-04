import React from 'react';
import { Grid, TextField, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { FormikProps } from 'formik';
import { FormField } from '../forms';

interface DoctorBasicInfoProps {
  formik: FormikProps<any>;
}

const DoctorBasicInfo: React.FC<DoctorBasicInfoProps> = ({ formik }) => {
  return (
    <Grid container spacing={3}>
      <Grid item xs={12} sm={6}>
        <FormField
          name="specialty"
          label="Especialidad *"
          formik={formik}
          placeholder="Ej: Cardiología, Dermatología, etc."
        />
      </Grid>
      
      <Grid item xs={12} sm={6}>
        <FormField
          name="consultationCost"
          label="Costo de Consulta (MXN) *"
          type="number"
          formik={formik}
          placeholder="Ej: 500"
        />
      </Grid>
      
      <Grid item xs={12}>
        <FormField
          name="description"
          label="Descripción Profesional"
          formik={formik}
          multiline
          rows={4}
          placeholder="Describe tu experiencia, certificaciones y enfoque médico..."
        />
      </Grid>
      
      <Grid item xs={12} sm={6}>
        <FormField
          name="phone"
          label="Teléfono de Contacto"
          formik={formik}
          placeholder="Ej: +52 55 1234 5678"
        />
      </Grid>
      
      <Grid item xs={12} sm={6}>
        <FormField
          name="email"
          label="Email de Contacto"
          type="email"
          formik={formik}
          placeholder="tu.email@ejemplo.com"
        />
      </Grid>
      
      <Grid item xs={12}>
        <FormField
          name="address"
          label="Dirección del Consultorio"
          formik={formik}
          placeholder="Calle, número, colonia, ciudad..."
        />
      </Grid>
    </Grid>
  );
};

export default DoctorBasicInfo;
