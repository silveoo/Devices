// src/pages/Devices.js
import React from 'react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const Devices = () => {
    const [devices, setDevices] = useState([]);

    useEffect(() => {
        api.get('/api/v1/device-instances')
            .then(res => setDevices(res.data))
            .catch(err => console.error(err));
    }, []);

    const capitalize = (str) =>
        str.charAt(0).toUpperCase() + str.slice(1);

    const checkParameter = (param, template) => {
        switch(template.type) {
            case 'RANGE':
                const value = parseFloat(param.value);
                return value >= parseFloat(template.minValue) &&
                    value <= parseFloat(template.maxValue);
            case 'DEVIATION':
                const base = parseFloat(template.value);
                const tolerance = base * (template.tolerancePercent / 100);
                return Math.abs(parseFloat(param.value) - base) <= tolerance;
            case 'ENUM':
                return template.allowedValues.split(',')
                    .map(v => v.trim())
                    .includes(param.value);
            case 'BOOLEAN':
                return param.value === template.value;
            case 'EQUALS':
            case 'EQUALS_STRING':
                return param.value === template.value;
            default:
                return true;
        }
    };

    return (
        <div className="container">
            <h2 className="mb-4">Экземпляры устройств</h2>
            <Link to="/devices/create" className="btn btn-success mb-3">Создать</Link>
            <table className="table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Тип устройства</th>
                    <th>Тестировщик</th>
                    <th>Параметр</th>
                    <th>Значение</th>
                    <th>Соответствие</th>
                </tr>
                </thead>
                <tbody>
                {devices.map(device => (
                    <React.Fragment key={device.id}>
                        <tr>
                            <td rowSpan={device.parameters.length + 1}>
                                {device.id}
                            </td>
                            <td rowSpan={device.parameters.length + 1}>
                                {device.deviceType?.name}
                            </td>
                            <td rowSpan={device.parameters.length + 1}>
                                {device.tester?.name}
                            </td>
                        </tr>
                        {device.parameters.map((param, index) => {
                            const template = device.deviceType?.parameters
                                .find(p => p.name === param.name);

                            return (
                                <tr key={param.name}>
                                    <td className="parameter-name">
                                        {capitalize(param.name)}
                                    </td>
                                    <td
                                        className={`parameter-value ${
                                            template && !checkParameter(param, template)
                                                ? 'text-danger'
                                                : 'text-success'
                                        }`}
                                    >
                                        {param.value}
                                    </td>
                                    <td>
                                        {template ? (
                                            checkParameter(param, template) ? (
                                                <span className="badge bg-success">OK</span>
                                            ) : (
                                                <span className="badge bg-danger">Ошибка</span>
                                            )
                                        ) : (
                                            <span className="badge bg-warning">Не задано</span>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                    </React.Fragment>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default Devices;