import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const DeviceCreate = () => {
    const [formData, setFormData] = useState({
        requestId: '',
        deviceTypeId: '',
        deviceName: '',
        testerId: '',
        parameters: []
    });

    const [deviceTypes, setDeviceTypes] = useState([]);
    const [testers, setTesters] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        // Загрузка типов устройств и тестировщиков
        api.get('/api/v1/device-types')
            .then(res => setDeviceTypes(res.data))
            .catch(err => console.error(err));

        api.get('/api/v1/testers')
            .then(res => setTesters(res.data))
            .catch(err => console.error(err));
    }, []);

    const handleTypeChange = (e) => {
        const selectedTypeId = e.target.value;
        const selectedType = deviceTypes.find(t => t.id === parseInt(selectedTypeId));

        if (selectedType) {
            setFormData({
                ...formData,
                deviceTypeId: selectedTypeId,
                deviceName: selectedType.name,
                parameters: selectedType.parameters.map(param => ({
                    name: param.name,
                    value: ''
                }))
            });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/api/v1/device-instances', {
                ...formData,
                deviceTypeId: parseInt(formData.deviceTypeId),
                testerId: parseInt(formData.testerId)
            });
            navigate('/devices');
        } catch (error) {
            alert('Ошибка при создании');
        }
    };

    const renderParameterConstraints = (template) => {
        if (!template) return '';
        switch(template.type) {
            case 'RANGE':
                return `Диапазон: ${template.minValue} - ${template.maxValue}`;
            case 'DEVIATION':
                return `База: ${template.value} ±${template.tolerancePercent}%`;
            case 'ENUM':
                return `Допустимые: ${template.allowedValues}`;
            case 'BOOLEAN':
                return `Допустимые: Да/Нет`;
            default:
                return `Значение: ${template.value}`;
        }
    };

    return (
        <div className="container">
            <h2 className="mb-4">Создать экземпляр</h2>
            <form onSubmit={handleSubmit}>
                {/* Поле Request ID */}
                <div className="mb-3">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Request ID"
                        value={formData.requestId}
                        onChange={(e) => setFormData({ ...formData, requestId: e.target.value })}
                        required
                    />
                </div>

                {/* Выбор типа устройства */}
                <div className="mb-3">
                    <select
                        className="form-select"
                        value={formData.deviceTypeId}
                        onChange={handleTypeChange}
                        required
                    >
                        <option value="">Выберите тип устройства</option>
                        {deviceTypes.map(type => (
                            <option key={type.id} value={type.id}>
                                {type.name}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Выбор тестировщика */}
                <div className="mb-3">
                    <select
                        className="form-select"
                        value={formData.testerId}
                        onChange={(e) => setFormData({ ...formData, testerId: e.target.value })}
                        required
                    >
                        <option value="">Выберите тестировщика</option>
                        {testers.map(tester => (
                            <option key={tester.id} value={tester.id}>
                                {tester.id} - {tester.name} ({tester.username})
                            </option>
                        ))}
                    </select>
                </div>

                {/* Параметры устройства */}
                {formData.parameters.map((param, index) => {
                    const template = deviceTypes
                        .find(t => t.id === parseInt(formData.deviceTypeId))
                        ?.parameters.find(p => p.name === param.name);

                    return (
                        <div key={index} className="mb-3 border p-3">
                            <h5>{param.name.charAt(0).toUpperCase() + param.name.slice(1)}</h5>
                            <div className="mb-2 position-relative">
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder={`Значение для ${param.name}`}
                                    value={param.value}
                                    onChange={(e) => {
                                        const newParams = [...formData.parameters];
                                        newParams[index].value = e.target.value;
                                        setFormData({ ...formData, parameters: newParams });
                                    }}
                                    required
                                />
                                {template && (
                                    <small className="text-muted form-text">
                                        {renderParameterConstraints(template)}
                                    </small>
                                )}
                            </div>
                        </div>
                    );
                })}

                {/* Кнопка отправки */}
                <button type="submit" className="btn btn-primary">
                    Создать экземпляр
                </button>
            </form>
        </div>
    );
};

export default DeviceCreate;