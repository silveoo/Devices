import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const TesterCreate = () => {
    const [formData, setFormData] = useState({
        name: '',
        username: '',
        password: ''
    });

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/api/v1/testers', formData);
            navigate('/testers');
        } catch (error) {
            alert('Ошибка при создании');
        }
    };

    return (
        <div className="container">
            <h2 className="mb-4">Создать тестировщика</h2>
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Имя"
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    />
                </div>
                <div className="mb-3">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Логин"
                        value={formData.username}
                        onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                    />
                </div>
                <div className="mb-3">
                    <input
                        type="password"
                        className="form-control"
                        placeholder="Пароль"
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    />
                </div>
                <button type="submit" className="btn btn-primary">Создать</button>
            </form>
        </div>
    );
};

export default TesterCreate;