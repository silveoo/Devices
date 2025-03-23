import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Login = ({ onSuccess }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const res = await api.post('/auth/login', { username, password });
            localStorage.setItem('token', res.data.token);
            onSuccess();
            navigate('/');
        } catch (error) {
            alert('Неверные учетные данные');
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-4">
                    <form onSubmit={handleSubmit}>
                        <h3 className="text-center mb-4">Вход</h3>
                        <div className="mb-3">
                            <input
                                type="text"
                                className="form-control"
                                placeholder="Логин"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                            />
                        </div>
                        <div className="mb-3">
                            <input
                                type="password"
                                className="form-control"
                                placeholder="Пароль"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </div>
                        <button type="submit" className="btn btn-primary w-100">Войти</button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Login;