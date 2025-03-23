// src/pages/DeviceTypes.js
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const DeviceTypes = () => {
    const [deviceTypes, setDeviceTypes] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [deletingId, setDeletingId] = useState(null);

    useEffect(() => {
        api.get('/api/v1/device-types')
            .then(res => setDeviceTypes(res.data))
            .catch(err => console.error(err));
    }, []);

    const handleDelete = async (id) => {
        try {
            await api.delete('/api/v1/device-types', { params: { id } });
            setDeviceTypes(deviceTypes.filter(type => type.id !== id));
        } catch (error) {
            alert('Ошибка при удалении');
        }
    };

    return (
        <div className="container">
            <h2 className="mb-4">Типы устройств</h2>
            <Link to="/device-types/create" className="btn btn-success mb-3">Создать</Link>
            <table className="table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Название</th>
                    <th>Параметры</th>
                    <th>Значения</th>
                    <th>Действия</th>
                </tr>
                </thead>
                <tbody>
                {deviceTypes.map(type => (
                    <tr key={type.id}>
                        <td>{type.id}</td>
                        <td>{type.name}</td>
                        <td>
                            {type.parameters.map(param => (
                                <div key={param.name} className="parameter-name">
                                    {param.name}
                                </div>
                            ))}
                        </td>
                        <td>
                            {type.parameters.map(param => (
                                <div key={param.name} className="parameter-value">
                                    {param.value || param.minValue || param.allowedValues}
                                </div>
                            ))}
                        </td>
                        <td>
                            <button
                                className="btn btn-danger btn-sm delete-btn"
                                onClick={() => {
                                    setDeletingId(type.id);
                                    setShowModal(true);
                                }}
                            >
                                ×
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            {/* Модальное окно подтверждения */}
            {showModal && (
                <div className="modal d-block">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">Подтверждение удаления</h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={() => setShowModal(false)}
                                ></button>
                            </div>
                            <div className="modal-body">
                                Вы действительно хотите удалить тип устройства "{deviceTypes.find(t => t.id === deletingId)?.name}"?
                            </div>
                            <div className="modal-footer">
                                <button
                                    className="btn btn-secondary"
                                    onClick={() => setShowModal(false)}
                                >
                                    Отмена
                                </button>
                                <button
                                    className="btn btn-danger"
                                    onClick={() => handleDelete(deletingId)}
                                >
                                    Удалить
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DeviceTypes;