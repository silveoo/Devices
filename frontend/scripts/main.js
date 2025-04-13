// Проверка авторизации
const token = localStorage.getItem('token');
if (!token) window.location.href = 'auth.html';

fetch('http://localhost:8080/api/v1/device-types', {
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
})

const generateRequestId = () => Date.now().toString(36) + Math.random().toString(36).substr(2);
let isAdmin = false;
let searchTimeout;

async function deleteDeviceType(deviceTypeId) {
    if (!confirm('Удалить тип устройства и все связанные экземпляры?')) return;

    try {
        const response = await fetch(`http://localhost:8080/api/v1/device-types/${deviceTypeId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            showNotification('Тип устройства удален', 'success');
            loadDeviceTypes();
        }
    } catch (error) {
        showNotification('Ошибка удаления', 'danger');
    }
}

document.getElementById('searchInput').addEventListener('input', (e) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        loadDeviceTypes(e.target.value);
    }, 300); // Задержка 300 мс для уменьшения запросов
});


async function loadDeviceTypes(searchQuery = '') {
    let types = []; // Инициализируем переменную

    try {
        const url = new URL('http://localhost:8080/api/v1/device-types');

        if (searchQuery.trim()) {
            url.searchParams.append('search', searchQuery);
        }

        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        types = await response.json(); // Присваиваем значение

    } catch (error) {
        console.error('Ошибка:', error);
        const errorMessage = `Не удалось загрузить данные: ${error.message}`;
        document.getElementById('deviceTypesGrid').innerHTML =
            `<div class="text-danger">${errorMessage}</div>`;
        return; // Прерываем выполнение при ошибке
    }

    // После успешной загрузки
    const isSplitView = document.getElementById('splitViewContainer').style.display === 'block';
    const grid = document.getElementById('deviceTypesGrid');

    if (isSplitView) {
        // Для split view
        grid.style.display = 'none';
        renderDeviceTypes([types[0]], isAdmin, true);
    } else {
        // Для обычного режима
        grid.style.display = 'flex'; // Добавляем flex
        grid.className = 'row row-cols-1 row-cols-md-3 g-4'; // Явно задаем классы
        renderDeviceTypes(types, isAdmin);
    }
}

// Отображение типов
function renderDeviceTypes(types, isAdmin, isSplitView = false) {
    const grid = isSplitView
        ? document.getElementById('deviceTypesColumn')
        : document.getElementById('deviceTypesGrid');

    if (!types || types.length === 0) {
        grid.innerHTML = '<div class="text-muted">Нет типов устройств</div>';
        return;
    }

    console.log(isAdmin + "1313131231");

    grid.innerHTML = types.map(type => `
        <div class="col">
            <div class="card h-100 shadow-sm position-relative"  data-type-id="${type.id}">
                ${isAdmin ? `
                <button class="btn btn-danger btn-sm admin-trash-btn" 
                        onclick="deleteDeviceType('${type.id}')"
                        title="Удалить тип">
                    <i class="bi bi-trash"></i>
                </button>
                ` : ''}

                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <h5 class="card-title flex-grow-1 me-2">
                            <i class="bi bi-motherboard"></i> ${type.name}
                        </h5>
                        <span class="badge bg-primary">${type.parameters?.length || 0}</span>
                    </div>

                    <p class="card-text text-muted small">${type.description || 'Нет описания'}</p>

                    <div class="mt-3 d-grid gap-2">
                        <button class="btn btn-outline-primary" 
                                onclick="createInstance('${type.id}')">
                            <i class="bi bi-plus-lg"></i> Экземпляр
                        </button>
                        <button class="btn btn-outline-secondary" 
                                onclick="showInstances('${type.id}')">
                            <i class="bi bi-list-ul"></i> Список
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

let currentDeviceTypeName = ""; // Глобальная переменная для хранения имени типа

async function createInstance(typeId) {
    try {
        // Получаем данные типа устройства
        const typeResponse = await fetch(`http://localhost:8080/api/v1/device-types/${typeId}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const type = await typeResponse.json();
        currentDeviceTypeName = type.name; // Сохраняем название типа

        // Заполняем параметры в форме
        const container = document.getElementById('instanceParametersContainer');
        container.innerHTML = type.parameters.map(param => `
            <div class="mb-3">
                <label class="form-label">${param.name} (${param.type})</label>
                ${getParameterInputField(param)}
            </div>
        `).join('');

        // Показываем модальное окно
        new bootstrap.Modal('#createInstanceModal').show();
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

function getParameterInputField(param) {
    switch (param.type) {
        case 'BOOLEAN':
            return `
                <select class="form-select" name="parameters[${param.name}]" required>
                    <option value="true">Да</option>
                    <option value="false">Нет</option>
                </select>
            `;
        case 'RANGE':
            // Добавляем атрибуты min, max и step
            return `
                <input 
                    type="number" 
                    class="form-control" 
                    name="parameters[${param.name}]" 
                    step="any" <!-- Разрешаем дробные числа -->
            `;
        default:
            return `<input type="text" class="form-control" name="parameters[${param.name}]" required>`;
    }
}


// Логика создания типа
document.getElementById('createTypeBtn').addEventListener('click', () => {
    new bootstrap.Modal('#createTypeModal').show();
});

document.getElementById('createTypeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);

    // Собираем параметры в правильный формат
    const parameters = Array.from(document.querySelectorAll('.parameter-card')).map(card => {
        const id = card.id;
        return {
            name: formData.get(`parameters[${id}].name`),
            type: formData.get(`parameters[${id}].type`),
            value: formData.get(`parameters[${id}].value`),
            // Для специальных типов
            ...(formData.get(`parameters[${id}].type`) === 'DEVIATION' && {
                tolerancePercent: formData.get(`parameters[${id}].tolerancePercent`)
            }),
            ...(formData.get(`parameters[${id}].type`) === 'RANGE' && {
                minValue: formData.get(`parameters[${id}].minValue`),
                maxValue: formData.get(`parameters[${id}].maxValue`)
            }),
            ...(formData.get(`parameters[${id}].type`) === 'ENUM' && {
                allowedValues: formData.get(`parameters[${id}].allowedValues`)
            })
        };
    });

    const data = {
        name: formData.get('name'),
        description: formData.get('description'),
        parameters: parameters.filter(p => p.name && p.type) // Фильтр пустых
    };

    try {
        const response = await fetch('http://localhost:8080/api/v1/device-types', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showNotification('Тип устройства создан!', 'success');
            loadDeviceTypes();

            // Исправляем закрытие
            const modal = bootstrap.Modal.getInstance(document.getElementById('createTypeModal'));
            modal.hide();
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
});

document.getElementById('createInstanceForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const currentTypeId = getCurrentDeviceTypeId();

    // Получаем testerId текущего пользователя
    let testerId;
    try {
        const userResponse = await fetch('http://localhost:8080/auth/users/me', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const userData = await userResponse.json();
        testerId = userData.tester.id;
    } catch (error) {
        console.error('Ошибка получения testerId:', error);
        return;
    }

    // Формируем данные
    const data = {
        requestId: generateRequestId(), // Автогенерация
        testerId: testerId, // Автоматическое определение
        deviceName: currentDeviceTypeName, // Используем сохраненное имя типа
        parameters: Array.from(formData.entries())
            .filter(([key]) => key.startsWith('parameters'))
            .map(([key, value]) => ({
                name: key.split('[')[1].replace(']', ''),
                value: value
            }))
    };

    // Отправляем запрос
    try {
        const response = await fetch('http://localhost:8080/api/v1/device-instances', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showNotification('Экземпляр создан!', 'success');

            const isSplitViewActive = document.getElementById('splitViewContainer').style.display === 'block';

            if (isSplitViewActive) {
                // Обновляем список экземпляров для текущего типа
                await showInstances(currentTypeId);
            } else {
                // Обновляем общий список типов
                loadDeviceTypes();
            }

            // Исправляем закрытие
            const modal = bootstrap.Modal.getInstance(document.getElementById('createInstanceModal'));
            modal.hide();
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
});

function getCurrentDeviceTypeId() {
    const splitViewCard = document.querySelector('#deviceTypesColumn .card');
    return splitViewCard ? splitViewCard.dataset.typeId : null;
}

function showNotification(message, type = 'success') {
    const container = document.getElementById('notificationContainer');
    const toast = document.createElement('div');

    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                <i class="bi ${type === 'success' ? 'bi-check-circle' : 'bi-x-circle'} me-2"></i>
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    container.appendChild(toast);
    new bootstrap.Toast(toast, { autohide: true, delay: 3000 }).show();

    // Удаление Toast через 3 секунды
    setTimeout(() => toast.remove(), 3000);
}

// Выход
function logout() {
    localStorage.removeItem('token');
    window.location.href = 'auth.html';
}

// Проверка роли пользователя (пример)
async function checkUserRole() {
    try {
        const response = await fetch('http://localhost:8080/auth/users/me', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const user = await response.json();
        isAdmin = user.roles?.some(role =>
            role.name === 'ADMIN' ||  // Основная проверка
            (role.authorities && role.authorities.some(a => a.name === 'ADMIN'))) // Проверка authorities

        console.log('Is admin after check:', isAdmin); // Добавляем лог

        if (isAdmin) {
            document.getElementById('createTypeBtn').style.display = 'block';
            document.getElementById('employeesNavLink').style.display = 'block';
        }

        // Подсветка активной страницы
        const currentPage = window.location.pathname.split('/').pop();
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === currentPage) {
                link.classList.add('active');
            }
        });

        loadDeviceTypes();
    } catch (error) {
        console.error('Ошибка проверки роли:', error);
    }
}

async function showInstances(typeId) {
    try {
        const response = await fetch(`http://localhost:8080/api/v1/device-instances/by-type/?deviceTypeId=${typeId}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });

        if (response.ok) {
            const instances = await response.json();

            // Активируем разделенный вид
            document.getElementById('deviceTypesGrid').style.display = 'none';
            document.getElementById('splitViewContainer').style.display = 'block';

            // Загружаем информацию о типе устройства
            const typeResponse = await fetch(`http://localhost:8080/api/v1/device-types/${typeId}`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
            });
            const deviceType = await typeResponse.json();

            // Рендерим карточку типа в левой колонке
            renderDeviceTypes([deviceType], isAdmin, true);

            // Рендерим экземпляры в правой колонке
            renderInstancesColumn(instances);
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

function renderInstanceDetails(instance) {
    return `
        <div class="mt-3">
            <div class="alert alert-info">
                <i class="bi bi-person"></i> Тестировщик: ${instance.tester?.name || "Нет данных"}
            </div>
            <button class="btn btn-warning w-100 mb-3" 
                    onclick="generateReport(${instance.id}, '${instance.deviceType.name}')">
                <i class="bi bi-file-pdf"></i> Скачать PDF
            </button>
            <h6><i class="bi bi-clipboard-data"></i> Параметры</h6>
            <div class="table-responsive">
                ${renderParametersTable(instance)}
            </div>
        </div>
    `;
}

function renderParametersTable(instance) {
    const expectedParams = instance.deviceType.parameters || [];
    const actualParams = instance.parameters || [];

    return `
        <table class="table table-bordered">
            <thead class="table-light">
                <tr>
                    <th>Параметр</th>
                    <th>Ожидаемое</th>
                    <th>Фактическое</th>
                    <th>Соответствие</th>
                </tr>
            </thead>
            <tbody>
                ${expectedParams.map(param => {
        const actual = actualParams.find(p => p.name === param.name);
        const isMatch = checkParameterCompliance(param, actual);
        return `
                        <tr class="${isMatch ? 'table-success' : 'table-danger'}">
                            <td>${param.name}</td>
                            <td>${formatParameterValue(param)}</td>
                            <td>${actual?.value || 'N/A'}</td>
                            <td>
                                ${isMatch ?
            '<i class="bi bi-check-circle text-success"></i>' :
            '<i class="bi bi-x-circle text-danger"></i>'}
                            </td>
                        </tr>
                    `;
    }).join('')}
            </tbody>
        </table>
    `;
}

function formatParameterValue(param) {
    switch (param.type) {
        case 'RANGE':
            return `${param.minValue} - ${param.maxValue}`;
        case 'DEVIATION':
            return `${param.value} ±${param.tolerancePercent}%`;
        case 'ENUM':
            // Обработка разных форматов allowedValues
            const values = param.allowedValues;
            if (Array.isArray(values)) {
                return values.join(', ') || 'N/A';
            } else if (typeof values === 'string') {
                // Если приходит строка, разбиваем по запятым
                return values.split(',').map(v => v.trim()).join(', ') || 'N/A';
            } else {
                return 'N/A';
            }
        default:
            return param.value || 'N/A';
    }
}

function checkParameterCompliance(expected, actual) {
    if (!actual) return false;

    switch (expected.type) {
        case 'RANGE':
            const numericValue = parseFloat(actual.value); // Преобразуем в число
            return numericValue >= expected.minValue && numericValue <= expected.maxValue;
        case 'DEVIATION':
            const deviation = Math.abs(actual.value - expected.value);
            const allowedDeviation = expected.value * (expected.tolerancePercent / 100);
            return deviation <= allowedDeviation;
        case 'EQUALS_STRING':
            return actual.value === expected.value;
        case 'EQUALS':
            return parseFloat(actual.value) === parseFloat(expected.value);
        default:
            return true;
    }
}

async function exitSplitView() {
    const grid = document.getElementById('deviceTypesGrid');
    const splitContainer = document.getElementById('splitViewContainer');

    // Полностью восстанавливаем оригинальную структуру
    grid.style.display = 'flex'; // Важно: Bootstrap использует flex для row
    grid.classList.add('row', 'row-cols-1', 'row-cols-md-3', 'g-4');

    // Очищаем возможные остаточные стили
    grid.style.grid = '';
    grid.style.flex = '';

    // Скрываем split view
    splitContainer.style.display = 'none';

    // Принудительно перезагружаем сетку
    await loadDeviceTypes();
}

function renderInstanceCard(instance) {
    // Форматирование даты
    const testedDate = instance.testedAt ? new Date(instance.testedAt) : null;
    const formattedDate = testedDate
        ? testedDate.toLocaleDateString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })
        : 'Дата не указана';

    // Определение статуса дефектов
    const hasDefects = instance.hasOwnProperty('anyDefects')
        ? instance.anyDefects
        : 'unknown';

    return `
        <div class="instance-card">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="fw-bold">Экземпляр #${instance.id}</span>
                    <small class="text-muted">Тест проведен: ${formattedDate}</small>
                    <span class="badge ${hasDefects === 'unknown' ? 'bg-secondary' : hasDefects ? 'bg-danger' : 'bg-success'} ms-2">
                        ${hasDefects === 'unknown' ? 'Не проверено' : hasDefects ? 'Есть дефекты' : 'Без дефектов'}
                    </span>
                </div>
                <div>
                    <button class="btn btn-sm btn-outline-primary instance-details-toggle" 
                            data-instance-id="${instance.id}">
                        <i class="bi bi-chevron-down"></i>
                    </button>
                </div>
            </div>
            <div class="instance-details" id="details-${instance.id}" style="display: none;">
                ${renderInstanceDetails(instance)}
            </div>
        </div>
    `;
}

function renderInstancesColumn(instances) {
    const container = document.getElementById('instancesColumn');
    container.innerHTML = `
        <div class="card shadow-sm mb-4">
            <div class="card-body">
                <h5 class="card-title mb-4">
                    <i class="bi bi-list-ul"></i> Экземпляры устройства
                </h5>
                ${instances.map(instance => `
                    <div class="card mb-3">
                        ${renderInstanceCard(instance)}
                    </div>
                `).join('')}
            </div>
        </div>
    `;

    // Добавляем обработчики для раскрытия деталей
    container.querySelectorAll('.instance-details-toggle').forEach(btn => {
        btn.addEventListener('click', () => toggleDetails(btn.dataset.instanceId));
    });
}

async function generateReport(instanceId, deviceName) {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            showNotification('Требуется авторизация', 'danger');
            return;
        }

        // Формируем URL с автоматическим кодированием параметров
        const url = new URL('http://localhost:8080/api/v1/report/pdf');
        url.searchParams.append('instanceId', instanceId);
        url.searchParams.append('deviceName', deviceName);

        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Ошибка ${response.status}: ${errorText}`);
        }

        // Скачиваем PDF
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = `report_${deviceName}.pdf`; // Имя файла с названием устройства
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

    } catch (error) {
        console.error('Ошибка:', error);
        showNotification(`Не удалось скачать отчет: ${error.message}`, 'danger');
    }
}

async function loadParametersForInstance(instanceId, deviceName) {
    try {
        // 1. Получаем токен из localStorage
        const token = localStorage.getItem('token');
        if (!token) {
            throw new Error('Токен аутентификации отсутствует');
        }

        // 3. Формируем URL с закодированными параметрами
        const url = new URL(`http://localhost:8080/api/v1/report`);
        url.searchParams.append('instanceId', instanceId);
        url.searchParams.append('deviceName', deviceName);

        // 4. Отправляем запрос с токеном в заголовке
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        // 5. Проверяем статус ответа
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Ошибка ${response.status}: ${errorText}`);
        }

        // 6. Парсим JSON-ответ
        const report = await response.json();

        // 7. Формируем таблицу с параметрами
        const rows = Object.keys(report.expectedParameters).map(param => `
            <tr>
                <td>${param}</td>
                <td>${report.expectedParameters[param]}</td>
                <td>${report.actualParameters[param] || 'N/A'}</td>
                <td class="${report.discrepancies[param] ? 'table-danger' : 'table-success'}">
                    ${report.discrepancies[param] ? 'Несоответствие' : 'OK'}
                </td>
            </tr>
        `).join('');

        // 8. Обновляем DOM
        document.getElementById(`params-${instanceId}`).innerHTML = rows;

    } catch (error) {
        console.error('Ошибка загрузки параметров:', error);
        // 9. Отображаем ошибку пользователю
        document.getElementById(`params-${instanceId}`).innerHTML = `
            <tr>
                <td colspan="4" class="text-center text-danger">
                    Ошибка: ${error.message}
                </td>
            </tr>
        `;
    }
}

function toggleDetails(instanceId) {
    const details = document.getElementById(`details-${instanceId}`);
    const icon = document.querySelector(`[data-instance-id="${instanceId}"] i`);

    details.style.display = details.style.display === 'none' ? 'block' : 'none';
    icon.classList.toggle('bi-chevron-down');
    icon.classList.toggle('bi-chevron-up');
}

let parameterCount = 0;

function addParameterField() {
    const container = document.getElementById('parametersContainer');
    const id = `param-${parameterCount++}`;

    const html = `
        <div class="card mb-3 parameter-card" id="${id}">
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label">Имя параметра</label>
                        <input type="text" class="form-control" name="parameters[${id}].name" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Тип</label>
                        <select class="form-select parameter-type" 
                                name="parameters[${id}].type" 
                                onchange="updateParameterFields('${id}')" 
                                required>
                            <option value="">Выберите тип</option>
                            <option value="GREATER_THAN">GREATER_THAN</option>
                            <option value="LESS_THAN">LESS_THAN</option>
                            <option value="EQUALS">EQUALS</option>
                            <option value="EQUALS_STRING">EQUALS_STRING</option>
                            <option value="RANGE">RANGE</option>
                            <option value="DEVIATION">DEVIATION</option>
                            <option value="ENUM">ENUM</option>
                            <option value="BOOLEAN">BOOLEAN</option>
                            <option value="NOT_EQUALS">NOT_EQUALS</option>
                        </select>
                    </div>
                    <div class="col-md-4 parameter-fields" id="fields-${id}">
                        <!-- Динамические поля -->
                    </div>
                </div>
                <button type="button" class="btn btn-danger btn-sm mt-2" 
                        onclick="document.getElementById('${id}').remove()">
                    <i class="bi bi-trash"></i> Удалить
                </button>
            </div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', html);
}

function updateParameterFields(paramId) {
    const type = document.querySelector(`#${paramId} .parameter-type`).value;
    const fieldsContainer = document.querySelector(`#fields-${paramId}`);
    fieldsContainer.innerHTML = '';

    let fieldsHtml = '';
    switch (type) {
        case 'RANGE':
            fieldsHtml = `
                <label class="form-label">Min</label>
                <input type="number" class="form-control" name="parameters[${paramId}].minValue" required>
                <label class="form-label mt-2">Max</label>
                <input type="number" class="form-control" name="parameters[${paramId}].maxValue" required>
            `;
            break;
        case 'DEVIATION':
            fieldsHtml = `
                <label class="form-label">Значение</label>
                <input type="number" class="form-control" name="parameters[${paramId}].value" required>
                <label class="form-label mt-2">Допуск (%)</label>
                <input type="number" class="form-control" name="parameters[${paramId}].tolerancePercent" required>
            `;
            break;
        case 'ENUM':
            fieldsHtml = `
                <label class="form-label">Допустимые значения (через запятую)</label>
                <input type="text" class="form-control" name="parameters[${paramId}].allowedValues" required>
            `;
            break;
        case 'BOOLEAN':
        case 'EQUALS':
        case 'EQUALS_STRING':
        case 'GREATER_THAN':
        case 'LESS_THAN':
        case 'NOT_EQUALS':
            fieldsHtml = `
                <label class="form-label">Значение</label>
                <input type="${type === 'BOOLEAN' ? 'checkbox' : 'text'}" 
                       class="form-control ${type === 'BOOLEAN' ? 'form-check-input' : ''}" 
                       name="parameters[${paramId}].value" 
                       ${type === 'BOOLEAN' ? 'value="true"' : ''} 
                       required>
            `;
            break;
    }

    fieldsContainer.innerHTML = fieldsHtml;
}

async function loadCurrentUser() {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await fetch('http://localhost:8080/auth/users/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const user = await response.json();
            document.getElementById('currentUser').textContent = user.username;
        }
    } catch (error) {
        console.error('Ошибка загрузки пользователя:', error);
    }
}

// Запуск при загрузке страницы
window.addEventListener('DOMContentLoaded', () => {
    loadCurrentUser();
    loadDeviceTypes();
    checkUserRole();
});