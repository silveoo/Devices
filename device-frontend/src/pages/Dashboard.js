// src/pages/Dashboard.js
import {Link, Outlet, useLocation, useNavigate} from 'react-router-dom';
import {useEffect} from "react";

const Dashboard = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    useEffect(() => {
        let pageTitle = 'Devices';

        if (location.pathname.startsWith('/devices')) {
            pageTitle = 'Устройства';
        } else if (location.pathname.startsWith('/device-types')) {
            pageTitle = 'Типы устройств';
        } else if (location.pathname.startsWith('/testers')) {
            pageTitle = 'Тестировщики';
        } else if (location.pathname.startsWith('/reports')) {
            pageTitle = 'Отчеты';
        }

        document.title = pageTitle;

        // Сбрасываем заголовок при размонтировании
        return () => {
            document.title = 'Devices';
        };
    }, [location]);

    return (
        <div className="container-fluid">
            {/* Навигационная панель */}
            <nav className="navbar navbar-expand-lg navbar-light bg-light">
                <div className="container-fluid">
                    <button
                        className="navbar-toggler"
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#navbarNav"
                    >
                        <span className="navbar-toggler-icon"></span>
                    </button>
                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav">
                            <li className="nav-item">
                                <Link to="/devices" className="nav-link">Устройства</Link>
                            </li>
                            <li className="nav-item">
                                <Link to="/device-types" className="nav-link">Типы устройств</Link>
                            </li>
                            <li className="nav-item">
                                <Link to="/reports" className="nav-link">Отчеты</Link>
                            </li>
                            <li className="nav-item">
                                <Link to="/testers" className="nav-link">Тестировщики</Link>
                            </li>
                        </ul>
                        <button
                            onClick={handleLogout}
                            className="btn btn-danger ms-auto"
                        >
                            Выйти
                        </button>
                    </div>
                </div>
            </nav>

            {/* Здесь отображаются дочерние компоненты (Devices, Reports и т.д.) */}
            <div className="mt-4 p-3">
                <Outlet />
            </div>
        </div>
    );
};

export default Dashboard;