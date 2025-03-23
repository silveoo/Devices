// src/pages/Reports.js
import { useEffect, useState } from 'react';
import api from '../services/api';

const Reports = () => {
    const [devices, setDevices] = useState([]);
    const [selectedDevice, setSelectedDevice] = useState(null);

    useEffect(() => {
        // Загружаем список устройств при монтировании
        api.get('/api/v1/device-instances')
            .then(res => setDevices(res.data))
            .catch(err => console.error(err));
    }, []);

    const handleDeviceChange = (e) => {
        const deviceId = e.target.value;
        const device = devices.find(d => d.id === parseInt(deviceId));
        setSelectedDevice(device);
    };

    const handleJsonReport = async () => {
        if (!selectedDevice) return alert('Выберите устройство');

        try {
            const res = await api.get('/api/v1/report', {
                params: {
                    instanceId: selectedDevice.id,
                    deviceName: selectedDevice.deviceType.name
                }
            });
            console.log('Отчет JSON:', res.data);
            alert('Отчет сгенерирован, смотрите консоль');
        } catch (error) {
            alert('Ошибка генерации');
        }
    };

    const handlePdfReport = async () => {
        if (!selectedDevice) return alert('Выберите устройство');

        try {
            const res = await api.get('/api/v1/report/pdf', {
                params: {
                    instanceId: selectedDevice.id,
                    deviceName: selectedDevice.deviceType.name
                },
                responseType: 'blob'
            });

            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `report_${selectedDevice.id}.pdf`);
            document.body.appendChild(link);
            link.click();
        } catch (error) {
            alert('Ошибка генерации PDF');
        }
    };

    return (
        <div className="container">
            <h2 className="mb-4">Генерация отчетов</h2>

            <div className="mb-3">
                <label className="form-label">Выберите устройство:</label>
                <select
                    className="form-select"
                    onChange={handleDeviceChange}
                    value={selectedDevice?.id || ''}
                >
                    <option value="">-- Выберите устройство --</option>
                    {devices.map(device => (
                        <option key={device.id} value={device.id}>
                            {device.id}: {device.deviceType?.name}
                        </option>
                    ))}
                </select>
            </div>

            <div className="d-grid gap-2">
                <button
                    onClick={handleJsonReport}
                    className="btn btn-info"
                    disabled={!selectedDevice}
                >
                    Сгенерировать JSON
                </button>
                <button
                    onClick={handlePdfReport}
                    className="btn btn-danger"
                    disabled={!selectedDevice}
                >
                    Скачать PDF
                </button>
            </div>
        </div>
    );
};

export default Reports;