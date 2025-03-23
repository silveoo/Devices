import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const DeviceTypeCreate = () => {
    const [formData, setFormData] = useState({
        name: '',
        parameters: []
    });

    const navigate = useNavigate();

    const addParameter = () => {
        setFormData({
            ...formData,
            parameters: [
                ...formData.parameters,
                { name: '', type: 'RANGE', value: '', minValue: '', maxValue: '', tolerancePercent: '', allowedValues: '' }
            ]
        });
    };

    const removeParameter = (index) => {
        setFormData({
            ...formData,
            parameters: formData.parameters.filter((_, i) => i !== index)
        });
    };

    const handleParamChange = (index, field, value) => {
        const newParams = [...formData.parameters];
        newParams[index][field] = value;

        // Очистка лишних полей при смене типа
        if (field === 'type') {
            const param = newParams[index];
            switch(value) {
                case 'RANGE':
                    param.minValue = '';
                    param.maxValue = '';
                    delete param.value;
                    delete param.tolerancePercent;
                    delete param.allowedValues;
                    break;
                case 'DEVIATION':
                    param.value = '';
                    param.tolerancePercent = '';
                    delete param.minValue;
                    delete param.maxValue;
                    delete param.allowedValues;
                    break;
                case 'ENUM':
                    param.allowedValues = '';
                    delete param.minValue;
                    delete param.maxValue;
                    delete param.value;
                    delete param.tolerancePercent;
                    break;
                case 'BOOLEAN':
                    param.value = '';
                    delete param.minValue;
                    delete param.maxValue;
                    delete param.tolerancePercent;
                    delete param.allowedValues;
                    break;
                case 'LESS_THAN':
                case 'GREATER_THAN':
                case 'NOT_EQUALS':
                case 'EQUALS':
                case 'EQUALS_STRING':
                    param.value = '';
                    delete param.minValue;
                    delete param.maxValue;
                    delete param.tolerancePercent;
                    delete param.allowedValues;
                    break;
                default:
                    break;
            }
        }

        setFormData({ ...formData, parameters: newParams });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/api/v1/device-types', formData);
            navigate('/device-types');
        } catch (error) {
            alert('Ошибка при создании');
        }
    };

    return (
        <div className="container">
            <h2 className="mb-4">Создать тип устройства</h2>
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Название типа"
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    />
                </div>

                {formData.parameters.map((param, index) => (
                    <div key={index} className="mb-3 border p-3">
                        <h5>Параметр {index + 1}</h5>

                        <div className="mb-2">
                            <input
                                type="text"
                                className="form-control"
                                placeholder="Название параметра"
                                value={param.name}
                                onChange={(e) => handleParamChange(index, 'name', e.target.value)}
                            />
                        </div>

                        <div className="mb-2">
                            <select
                                className="form-select"
                                value={param.type}
                                onChange={(e) => handleParamChange(index, 'type', e.target.value)}
                            >
                                <option value="">Выберите тип</option>
                                {['RANGE', 'DEVIATION', 'ENUM', 'BOOLEAN',
                                    'EQUALS', 'EQUALS_STRING', 'GREATER_THAN',
                                    'LESS_THAN', 'NOT_EQUALS'].map(type => (
                                    <option key={type} value={type}>{type}</option>
                                ))}
                            </select>
                        </div>

                        {(param.type === 'RANGE') && (
                            <>
                                <div className="mb-2">
                                    <input
                                        type="number"
                                        className="form-control"
                                        placeholder="Минимальное значение"
                                        value={param.minValue}
                                        onChange={(e) => handleParamChange(index, 'minValue', e.target.value)}
                                    />
                                </div>
                                <div className="mb-2">
                                    <input
                                        type="number"
                                        className="form-control"
                                        placeholder="Максимальное значение"
                                        value={param.maxValue}
                                        onChange={(e) => handleParamChange(index, 'maxValue', e.target.value)}
                                    />
                                </div>
                            </>
                        )}

                        {(param.type === 'DEVIATION') && (
                            <>
                                <div className="mb-2">
                                    <input
                                        type="number"
                                        className="form-control"
                                        placeholder="Базовое значение"
                                        value={param.value}
                                        onChange={(e) => handleParamChange(index, 'value', e.target.value)}
                                    />
                                </div>
                                <div className="mb-2">
                                    <input
                                        type="number"
                                        className="form-control"
                                        placeholder="Допуск (%)"
                                        value={param.tolerancePercent}
                                        onChange={(e) => handleParamChange(index, 'tolerancePercent', e.target.value)}
                                    />
                                </div>
                            </>
                        )}

                        {(param.type === 'ENUM') && (
                            <div className="mb-2">
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="Допустимые значения (через запятую)"
                                    value={param.allowedValues}
                                    onChange={(e) => handleParamChange(index, 'allowedValues', e.target.value)}
                                />
                            </div>
                        )}

                        {(param.type === 'BOOLEAN') && (
                            <div className="mb-2">
                                <select
                                    className="form-select"
                                    value={param.value}
                                    onChange={(e) => handleParamChange(index, 'value', e.target.value)}
                                >
                                    <option value="">Выберите значение</option>
                                    <option value="true">Да</option>
                                    <option value="false">Нет</option>
                                </select>
                            </div>
                        )}

                        {(['LESS_THAN', 'GREATER_THAN', 'NOT_EQUALS', 'EQUALS', 'EQUALS_STRING'].includes(param.type)) && (
                            <div className="mb-2">
                                <input
                                    type={param.type === 'EQUALS_STRING' ? 'text' : 'number'}
                                    className="form-control"
                                    placeholder={`Значение для ${param.type}`}
                                    value={param.value}
                                    onChange={(e) => handleParamChange(index, 'value', e.target.value)}
                                />
                            </div>
                        )}

                        <button
                            type="button"
                            className="btn btn-danger btn-sm"
                            onClick={() => removeParameter(index)}
                        >
                            Удалить параметр
                        </button>
                    </div>
                ))}

                <div className="d-flex gap-2 mb-3">
                    <button
                        type="button"
                        className="btn btn-secondary"
                        onClick={addParameter}
                    >
                        Добавить параметр
                    </button>
                    <button type="submit" className="btn btn-primary">
                        Создать тип
                    </button>
                </div>
            </form>
        </div>
    );
};

export default DeviceTypeCreate;