document.addEventListener('DOMContentLoaded', () => {
    // Если это страница авторизации
    if (window.location.pathname.endsWith('auth.html')) {
        // Проверяем, не авторизован ли пользователь
        const token = localStorage.getItem('token');
        if (token) {
            window.location.href = 'index.html';
        }
        return; // Не загружаем навбар и другие проверки
    }

    // Для всех других страниц
    fetch('navbar.html')
        .then(response => response.text())
        .then(html => {
            const navbarContainer = document.createElement('div');
            navbarContainer.innerHTML = html;
            document.body.prepend(navbarContainer);
            checkUserRole();
        })
        .catch(error => console.error('Ошибка загрузки навбара:', error));
});

async function checkUserRole() {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            window.location.href = 'auth.html';
            return;
        }

        const response = await fetch('http://localhost:8080/auth/users/me', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        // Если токен невалидный
        if (response.status === 401) {
            localStorage.removeItem('token');
            window.location.href = 'auth.html';
            return;
        }

        const user = await response.json();
        const isAdmin = user.roles?.some(role => role.name === 'ADMIN');

        // Показываем элементы для админа
        if (isAdmin) {
            const createTypeBtn = document.getElementById('createTypeBtn');
            if (createTypeBtn) createTypeBtn.style.display = 'block';
            const employeesNavLink = document.getElementById('employeesNavLink');
            if (employeesNavLink) employeesNavLink.style.display = 'block';
        }

        // Подсветка активной страницы
        const currentPage = window.location.pathname.split('/').pop();
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.toggle('active', link.getAttribute('href') === currentPage);
        });

    } catch (error) {
        console.error('Ошибка:', error);
        localStorage.removeItem('token');
        window.location.href = 'auth.html';
    }
}