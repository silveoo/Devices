import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Devices from './pages/Devices';
import DeviceTypes from './pages/DeviceTypes';
import Reports from './pages/Reports';
import Testers from './pages/Testers';
import 'bootstrap/dist/css/bootstrap.min.css';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import TesterCreate from "./pages/TesterCreate";
import DeviceCreate from "./pages/DeviceCreate";
import DeviceTypeCreate from "./pages/DeviceTypeCreate";

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    const checkAuth = () => {
        setIsAuthenticated(!!localStorage.getItem('token'));
    };

    const theme = createTheme({
        palette: {
            primary: { main: '#1976d2' },
            secondary: { main: '#4caf50' },
            background: { default: '#f5f5f5' }
        },
        components: {
            MuiDataGrid: {
                styleOverrides: {
                    root: {
                        border: 'none',
                        boxShadow: 'rgba(0,0,0,0.1) 0px 4px 12px'
                    }
                }
            }
        }
    });

    useEffect(() => {
        checkAuth();
    }, []);


    return (
        <ThemeProvider theme={theme}>
        <Router>
            <Routes>
                <Route path="/login" element={<Login onSuccess={checkAuth} />} />
                <Route
                    path="/"
                    element={isAuthenticated ? <Dashboard /> : <Navigate to="/login" />}
                >
                    <Route index element={<Navigate to="/devices" replace />} />
                    <Route path="devices">
                        <Route index element={<Devices />} />
                        <Route path="create" element={<DeviceCreate />} /> {/* Добавлено */}
                    </Route>
                    <Route path="device-types">
                        <Route index element={<DeviceTypes />} />
                        <Route path="create" element={<DeviceTypeCreate />} /> {/* Добавлено */}
                    </Route>
                    <Route path="testers">
                        <Route index element={<Testers />} />
                        <Route path="create" element={<TesterCreate />} /> {/* Добавлено */}
                    </Route>
                    <Route path="reports" element={<Reports />} />
                </Route>
            </Routes>
        </Router>
        </ThemeProvider>
    );
}

export default App;