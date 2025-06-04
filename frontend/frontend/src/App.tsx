import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme, Box } from '@mui/material';
import { ProveedorAutenticacion } from './contexts/AuthContext';
import Header from './components/Header';
import Footer from './components/Footer';
import PacienteSearch from './pages/PatientSearch';
import Login from './pages/Login';
import Register from './pages/Register';
import DoctorProfile from './pages/DoctorProfile';
import DoctorAppointments from './pages/DoctorAppointments';
import PacienteAppointments from './pages/PatientAppointments';
import DoctorSchedulePage from './pages/DoctorExtendedSchedule';
import AdminDashboard from './pages/AdminDashboard';
import BookAppointment from './pages/BookAppointment';
import About from './pages/About';
import PublicDoctorSearch from './pages/PublicDoctorSearch.jsx';
import DoctorAvailability from './pages/DoctorAvailability.jsx';
import PendingApproval from './pages/PendingApproval.jsx';
import RequireProfileConfig from './components/RequireProfileConfig.jsx';

const theme = createTheme({
  palette: {
    primary: {
      main: '#4a90e2',
    },
  },
});

const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <ProveedorAutenticacion>
        <Router>
          <Box sx={{ 
            display: 'flex', 
            flexDirection: 'column',
            minHeight: '100vh'
          }}>
            <Header />
            <Box sx={{ flexGrow: 1 }}>
              <Routes>
                {/* Ruta para redirigir a la página de configuración de perfil si es necesario */}
                <Route element={<RequireProfileConfig />}>
                  {/* Rutas públicas (no requieren redirección) */}
                  <Route path="/" element={<PublicDoctorSearch />} />
                  <Route path="/doctors" element={<PublicDoctorSearch />} />
                  <Route path="/public/doctors/:doctorId/availability" element={<DoctorAvailability />} />
                  <Route path="/login" element={<Login />} />
                  <Route path="/register" element={<Register />} />
                  <Route path="/pending-approval" element={<PendingApproval />} />
                  
                  {/* Rutas que pueden requerir redirección para configurar perfil */}
                  <Route path="/Doctors/:DoctorId/schedule" element={<DoctorSchedulePage />} />
                  <Route path="/appointments/book/:DoctorId" element={<BookAppointment />} />
                  <Route path="/paciente/appointments" element={<PacienteAppointments />} />
                  <Route path="/admin/dashboard" element={<AdminDashboard />} />
                  <Route path="/about" element={<About />} />
                </Route>
                
                {/* La ruta de perfil de doctor siempre debe ser accesible */}
                <Route path="/Doctor/profile" element={<DoctorProfile />} />
                <Route path="/Doctor/appointments" element={<DoctorAppointments />} />
              </Routes>
            </Box>
            <Footer />
          </Box>
        </Router>
      </ProveedorAutenticacion>
    </ThemeProvider>
  );
};

export default App;
