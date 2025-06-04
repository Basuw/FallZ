document.addEventListener('DOMContentLoaded', () => {
    redirectIfAuthenticated();
});

document.getElementById('login-form').addEventListener('submit', async (event) => {
    event.preventDefault();

    const mail = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const messageElement = document.getElementById('response-message');

    try {
        let user;

        if (USE_MOCK_DATA) {
            // Mode simulation
            user = {
                email : mail,
                id : 18635303,
                firstname : "Test_firstname",
                lastname : "Test_lastname",
            };
            localStorage.setItem('user', JSON.stringify(user));
        } else {
            // Mode API réelle
            const response = await fetch(`${API_BASE_URL}/login/connect`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ mail, password })
            });

            const data = await handleApiResponse(response);
            
            // Stocke les infos utilisateur et le token
            user = {
                email: data.mail,
                id: data.idUser
            };
            localStorage.setItem('user', JSON.stringify(user));
        }

        window.location.href = 'index.html';
    } catch (error) {
        messageElement.textContent = `Erreur: ${error.message}`;
        messageElement.style.color = "red";
    }
});