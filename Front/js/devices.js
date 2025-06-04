document.addEventListener('DOMContentLoaded', async () => {
    redirectIfNotAuthenticated();
    
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user) return;
    
    const devicesList = document.getElementById('devices-list');
    const addDeviceBtn = document.getElementById('add-device-btn');
    
    // Structure de données pour stocker appareils et utilisateurs
    async function loadData() {
        if (USE_MOCK_DATA) {
            const mockData = localStorage.getItem('mockDeviceUserData');
            return mockData ? JSON.parse(mockData) : {
                devices: [
                    { id: 1, deviceId: "Le fils", type: "fallz", userId: 1 },
                    { id: 2, deviceId: "La grand mere", type: "fallz", userId: 2 }
                ],
                users: [
                    { id: 1, firstname: "Paul", lastname: "Dupont", phone: "0612345678" },
                    { id: 2, firstname: "Marie", lastname: "Martin", phone: "0698765432" }
                ]
            };
        } else {
            try {
                const response = await fetch(`${API_BASE_URL}/device/${user.id}`, {
                    headers: {
                        'Authorization': `Bearer ${user.token}`
                    }
                });
                return await response.json();
            } catch (error) {
                console.error("Erreur chargement données:", error);
                return { devices: [], users: [] };
            }
        }
    }
    
    async function saveData(newDevice) {
        if (USE_MOCK_DATA) {
            localStorage.setItem('mockDeviceUserData', JSON.stringify(data));
        } else {
            try {
                await fetch(`${API_BASE_URL}/device/${user.id}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${user.token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(newDevice)
                });
            } catch (error) {
                console.error("Erreur sauvegarde données:", error);
                throw error;
            }
        }
    }
    
    async function displayDevices() {
        const data = await loadData();
        devicesList.innerHTML = '';
        
        data.forEach(device => {
            const deviceCard = document.createElement('div');
            deviceCard.className = 'device-card';
            deviceCard.innerHTML = `
                <button class="delete-device" data-id="${device.id}">×</button>
                <div class="user-info">
                    <h3><strong>Utilisateur:</strong> ${device.person.firstname} ${device.person.lastname}</h3>
                </div>
            `;
            
            deviceCard.addEventListener('click', (e) => {
                if (!e.target.classList.contains('delete-device')) {
                    localStorage.setItem('selectedDevice', JSON.stringify(device));
                    localStorage.setItem('selectedUser', JSON.stringify({
                        id: device.id,
                        ...device.person
                }));
                    window.location.href = 'index.html';
                }
            });
            
            devicesList.appendChild(deviceCard);
        });
        
        // Gestion suppression
        document.querySelectorAll('.delete-device').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const deviceId = btn.getAttribute('data-id');
                try {
                    const response = await fetch(`${API_BASE_URL}/device/${user.id}/${deviceId}`, {
                        method: 'DELETE',
                        headers: {
                            'Authorization': `Bearer ${user.token}`
                        }
                    });
                    if (response.ok) {
                        btn.closest('.device-card').remove();
                    } else {
                        alert("Erreur lors de la suppression de l'appareil.");
                    }
                } catch (error) {
                    console.error("Erreur suppression appareil:", error);
                    alert("Erreur lors de la suppression de l'appareil.");
                }
            });
        });
    }
    
    // Gestion ajout
    addDeviceBtn.addEventListener('click', async () => {
        const deviceId = document.getElementById('device-id').value.trim();
        const firstname = document.getElementById('user-firstname').value.trim();
        const lastname = document.getElementById('user-lastname').value.trim();
        
        if (!deviceId || !firstname || !lastname) {
            alert(`Veuillez remplir tous les champs ${deviceId}, ${firstname}, ${lastname}`);
            return;
        }
        
        const data = await loadData();
        
        // Crée le nouvel appareil
        const newDevice = {
            id: deviceId,
            person: {
                firstname,
                lastname
            }
        };
        
        await saveData(newDevice);
        
        // Réinitialise le formulaire
        document.getElementById('device-id').value = '';
        document.getElementById('user-firstname').value = '';
        document.getElementById('user-lastname').value = '';
        
        await displayDevices();
    });
    
    // Initialisation
    await displayDevices();
});