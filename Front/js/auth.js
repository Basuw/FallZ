const API_BASE_URL = 'http://localhost:8080';
const USE_MOCK_DATA = false; // Basculer à false pour utiliser l'API réelle

// Fonction pour gérer les erreurs API
async function handleApiResponse(response) {
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Erreur API');
    }
    return response.json();
}

// Fonctions d'authentification
function checkAuth() {
    const user = JSON.parse(localStorage.getItem('user'));
    return user !== null;
}

function redirectIfNotAuthenticated() {
    if (!checkAuth() && !window.location.pathname.includes('connection.html') && !window.location.pathname.includes('signup.html')) {
        window.location.href = 'connection.html';
    }
}

function redirectIfAuthenticated() {
    if (checkAuth()) {
        if (window.location.pathname.includes('connection.html') || 
            window.location.pathname.includes('signup.html')) {
            // Si l'utilisateur est déjà connecté mais n'a pas sélectionné d'appareil
            if (!localStorage.getItem('selectedDevice')) {
                window.location.href = 'devices.html';
            } else {
                window.location.href = 'index.html';
            }
        }
    }
}

function logout() {
    localStorage.removeItem('user');
    localStorage.removeItem('selectedDevice'); // Ajoutez cette ligne
    window.location.href = 'connection.html';
}

// Fonction pour récupérer le token (si vous utilisez JWT)
function getAuthToken() {
    const user = JSON.parse(localStorage.getItem('user'));
    return user?.token;
}

function goToDevices() {
    window.location.href = 'devices.html';
}