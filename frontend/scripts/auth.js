document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('http://localhost:8080/auth/login', {
            method: 'POST',
            mode: 'cors',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            window.location.href = 'index.html'; // Переход на главную
            console.log('Токен сохранен:', data.token);
        } else {
            document.getElementById('errorMessage').textContent = 'Ошибка авторизации!';
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
});