import { useEffect, useState } from 'react';
import api from '../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const checkParameter = (param, template) => {
    if (!template) return false;
    switch(template.type) {
        case 'RANGE':
            const value = parseFloat(param.value);
            return value >= parseFloat(template.minValue) && value <= parseFloat(template.maxValue);
        case 'DEVIATION': {
            const base = parseFloat(template.value);
            const tolerance = base * (parseFloat(template.tolerancePercent) / 100);
            return Math.abs(parseFloat(param.value) - base) <= tolerance;
        }
        case 'ENUM':
            return template.allowedValues.split(',')
                .map(v => v.trim())
                .includes(param.value);
        case 'BOOLEAN':
        case 'EQUALS':
        case 'EQUALS_STRING':
            return param.value === template.value;
        default:
            return true;
    }
};


const Reports = () => {
    const [devices, setDevices] = useState([]);
    const [selectedDevice, setSelectedDevice] = useState(null);
    const [chartData, setChartData] = useState([]);
    const [isLoading, setIsLoading] = useState(false);

    // Загрузка устройств
    useEffect(() => {
        const fetchDevices = async () => {
            setIsLoading(true);
            try {
                const response = await api.get('/api/v1/device-instances');
                console.log('Устройства загружены:', response.data); // Логирование данных
                setDevices(response.data);
            } catch (err) {
                console.error('Ошибка загрузки:', err);
            } finally {
                setIsLoading(false);
            }
        };
        fetchDevices();
    }, []);

    // Обновление данных графика
    useEffect(() => {
        if (selectedDevice?.parameters) {
            const data = selectedDevice.parameters.map(param => {
                const template = selectedDevice.deviceType?.parameters?.find(p => p.name === param.name);
                const isCompliant = checkParameter(param, template);
                return {
                    parameter: param.name,
                    Значение: isCompliant ? 1 : -1,
                    isCompliant: isCompliant
                };
            });
            setChartData(data);
        }
    }, [selectedDevice]);

    // Генерация PDF
    const handlePdfReport = async () => {
        if (!selectedDevice) return alert('Выберите устройство');
        try {
            const res = await api.get('/api/v1/report/pdf', {
                params: { instanceId: selectedDevice.id, deviceName: selectedDevice.deviceType.name },
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

    // Генерация JSON
    const handleJsonReport = async () => {
        if (!selectedDevice) return alert('Выберите устройство');
        try {
            const res = await api.get('/api/v1/report', {
                params: { instanceId: selectedDevice.id, deviceName: selectedDevice.deviceType.name }
            });
            console.log('JSON отчет:', res.data);
            alert('Отчет сгенерирован (см. консоль)');
        } catch (error) {
            alert('Ошибка генерации JSON');
        }
    };

    return (
        <div className="container">
            <h2 className="mb-4">Генерация отчетов</h2>

            {/* Выбор устройства */}
            <div className="mb-4 card p-3 shadow-sm">
                <div className="mb-3">
                    <label className="form-label fw-bold">Выберите устройство:</label>
                    {isLoading ? (
                        <div className="spinner-border text-primary"></div>
                    ) : (
                        <select
                            className="form-select"
                            onChange={(e) => setSelectedDevice(devices.find(d => d.id === parseInt(e.target.value)))}
                            value={selectedDevice?.id || ''}
                        >
                            <option value="">-- Не выбрано --</option>
                            {devices.map(device => (
                                <option key={device.id} value={device.id}>
                                    {device.deviceType?.name} (#{device.id})
                                </option>
                            ))}
                        </select>
                    )}
                </div>

                {/* Кнопки генерации */}
                <div className="d-flex gap-3 mt-3">
                    <button
                        onClick={handleJsonReport}
                        className="btn btn-primary flex-grow-1"
                        disabled={!selectedDevice}
                    >
                        <i className="bi bi-file-code me-2"></i>Сгенерировать JSON
                    </button>
                    <button
                        onClick={handlePdfReport}
                        className="btn btn-danger flex-grow-1"
                        disabled={!selectedDevice}
                    >
                        <i className="bi bi-file-pdf me-2"></i>Скачать PDF
                    </button>
                </div>
            </div>

            {/* График */}
            {selectedDevice && (
                <div className="card p-3 shadow-sm mt-4">
                    <h4 className="mb-4">
                        <i className="bi bi-bar-chart-line me-2"></i>
                        Соответствие параметров: {selectedDevice.deviceType?.name}
                    </h4>

                    <div style={{ height: '500px', minWidth: '800px' }}> {/* Увеличена минимальная ширина */}
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart
                                data={chartData}
                                margin={{ top: 20, right: 30, left: 20, bottom: 150 }} // Увеличен нижний отступ
                                layout="vertical" // Вертикальная ориентация
                            >
                                <CartesianGrid strokeDasharray="3 3" />
                                <YAxis
                                    type="category"
                                    dataKey="parameter"
                                    width={200}
                                    tick={{ fontSize: 12 }}
                                    angle={-30}
                                    textAnchor="end"
                                />
                                <XAxis
                                    type="number"
                                    domain={[-1, 1]}
                                    tickFormatter={value =>
                                        value === 1 ? 'Соответствует' :
                                            value === -1 ? 'Не соответствует' : ''
                                    }
                                />
                                <Tooltip
                                    formatter={(value) => [value === 1 ? 'Соответствует' : 'Не соответствует']}
                                />
                                <Bar
                                    dataKey="Значение" // Исправлено на "Значение"
                                    fill={({ isCompliant }) => isCompliant ? '#4CAF50' : '#FF5252'}
                                    barSize={20}
                                    radius={[0, 0, 0, 0]}
                                />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Reports;