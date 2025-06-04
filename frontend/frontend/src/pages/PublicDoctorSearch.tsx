// PublicDoctorSearch.tsx - Wrapper para el componente JSX
import React from 'react';

// Creo un componente que simplemente hace de wrapper para el jsx
const PublicDoctorSearchWrapper = (): JSX.Element => {
  // Uso un require para evitar la referencia circular en tiempo de compilación
  const Component = require('./PublicDoctorSearch.jsx').default;
  return React.createElement(Component);
};

export default PublicDoctorSearchWrapper;
