document.addEventListener('DOMContentLoaded', () => {
    redirectIfAuthenticated();
});

document.getElementById('signup-form').addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = {
        firstName: document.getElementById('first-name').value.trim(),
        lastName: document.getElementById('last-name').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        email: document.getElementById('email').value.trim(),
        password: document.getElementById('password').value,
        confirmPassword: document.getElementById('confirm-password').value
    };

    const messageElement = document.getElementById('response-message');
    
    // Validation (identique à avant)
    // if (formData.password.length < 8) {
    //     messageElement.innerText = "Le mot de passe doit contenir au moins 8 caractères.";
    //     return;
    // }


    // if (!/[A-Z]/.test(password)) {
    //     messageElement.innerText = "Le mot de passe doit contenir au moins une lettre majuscule.";
    //     return;
    // }

    // if (!/[a-z]/.test(password)) {
    //     messageElement.innerText = "Le mot de passe doit contenir au moins une lettre minuscule.";
    //     return;
    // }

    // if (!/[0-9]/.test(password)) {
    //     messageElement.innerText = "Le mot de passe doit contenir au moins un chiffre.";
    //     return;
    // }

    // if (password !== confirmPassword) {
    //     messageElement.innerText = "Les mots de passe ne correspondent pas.";
    //     return;
    // }

    try {
        let user;

        if (USE_MOCK_DATA) {
            // Mode simulation
            user = {
                firstname: formData.firstName,
                lastname: formData.lastName,
                phone: formData.phone,
                mail: formData.email,
                password: formData.password,
            };
            localStorage.setItem('user', JSON.stringify(user));
        } else {
            // Mode API réelle
            const response = await fetch(`${API_BASE_URL}/login/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    firstname: formData.firstName,
                    lastname: formData.lastName,
                    phone: formData.phone,
                    mail: formData.email,
                    password: formData.password
                })
            });

            const data = await handleApiResponse(response);
            
            user = {
                email: data.mail,
                id: data.idUser,
                firstname: data.person.firstname,
                lastname: data.person.lastname
            };
            localStorage.setItem('user', JSON.stringify(user));
        }

        messageElement.style.color = "green";
        messageElement.innerText = "Inscription réussie!";
        setTimeout(() => window.location.href = 'index.html', 1500);
    } catch (error) {
        messageElement.style.color = "red";
        messageElement.innerText = `Erreur: ${error.message}`;
    }
});