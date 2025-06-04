// DoctorAvailability.tsx - Wrapper para el componente JSX
import React from 'react';

// Creo un componente que simplemente hace de wrapper para el jsx
const DoctorAvailabilityWrapper = (): JSX.Element => {
  // Uso un require para evitar la referencia circular en tiempo de compilación
  const Component = require('./DoctorAvailability.jsx').default;
  return React.createElement(Component);
};

export default DoctorAvailabilityWrapper;
