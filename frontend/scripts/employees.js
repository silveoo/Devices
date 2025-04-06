// Проверка авторизации
const token = localStorage.getItem('token');
if (!token) window.location.href = 'auth.html';
let addEmployeeModalInstance = null;

// Загрузка сотрудников
async function loadEmployees() {
    try {
        const response = await fetch('http://localhost:8080/api/v1/testers', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const employees = await response.json();
        renderEmployees(employees);
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Отображение карточек
function renderEmployees(employees) {
    const grid = document.getElementById('employeesGrid');
    grid.innerHTML = employees.map(emp => `
        <div class="col">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h5 class="card-title">${emp.name}</h5>
                    <p class="card-text text-muted small">
                        Логин: ${emp.username}<br>
                        Роль: ${emp.roles.map(r => r.name).join(', ')}<br>
                        Права: ${emp.roles.flatMap(r => r.authorities).join(', ')}
                    </p>
                </div>
            </div>
        </div>
    `).join('');
}

// Открытие модалки
function showAddEmployeeModal() {
    const modal = new bootstrap.Modal('#addEmployeeModal');
    modal.show();
}

// Отправка формы
document.getElementById('addEmployeeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const submitButton = e.target.querySelector('button[type="submit"]');
    submitButton.disabled = true; // Блокируем кнопку

    try {
        const response = await fetch('http://localhost:8080/api/v1/testers', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: formData.get('name'),
                username: formData.get('username'),
                password: formData.get('password')
            })
        });

        if (response.ok) {
            const modal = bootstrap.Modal.getInstance(document.getElementById('addEmployeeModal'));
            loadEmployees();
            modal.hide();
        } else {
            const error = await response.json();
            throw new Error(error.message || "Ошибка сервера");
        }
    } catch (error) {
        showNotification(`Ошибка: ${error.message}`, 'danger');
    } finally {
        submitButton.disabled = false; // Разблокируем кнопку
    }
});

// Инициализация
document.addEventListener('DOMContentLoaded', () => {
    const modalElement = document.getElementById('addEmployeeModal');
    addEmployeeModalInstance = new bootstrap.Modal(modalElement);
    loadEmployees();
});


