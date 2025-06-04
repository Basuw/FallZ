document.addEventListener('DOMContentLoaded', async () => {
    redirectIfNotAuthenticated();
    
    const user = JSON.parse(localStorage.getItem('user'));
    const selectedDevice = JSON.parse(localStorage.getItem('selectedDevice'));
    const selectedUser = JSON.parse(localStorage.getItem('selectedUser'));

    if (!user) {
        window.location.href = 'connection.html';
        return;
    }
    
    if (!selectedDevice || !selectedUser) {
        window.location.href = 'devices.html';
        return;
    }

    if (user) {
        // Affiche les infos utilisateur
        document.getElementById('username').textContent = user.username || user.email;
        // Récupère le montant de la facture depuis l'API
        try {
            const response = await fetch(`${API_BASE_URL}/paiement/${user.id}`, {
            headers: {
                'Authorization': `Bearer ${user.token}`
            }
            });
            const bill = await response.json();
            document.getElementById('bill').textContent = bill.toFixed(2);
        } catch (error) {
            console.error("Erreur lors de la récupération du bill:", error);
            document.getElementById('bill').textContent = "Erreur";
        }
        
        console.log("Appareil sélectionné:", selectedDevice.id);
        console.log("Utilisateur associé:", selectedUser.firstname, selectedUser.lastname);

        // Récupère les randonnées (simulées ou depuis l'API)
        let parcours = await loadHikes(selectedUser);
        
        const parcoursList = document.getElementById('hikes-list');
        let map;
        let currentHikeLayer;
        let markers = [];

        // Initialisation de la carte
        function initMap() {
            map = L.map('hike-map').setView([46.0, 6.0], 8);
            
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
                maxZoom: 18
            }).addTo(map);
        }

        function parseDate(dateString) {
            const cleanInput = dateString.split('.')[0].replace(' ', 'T');
            const date = new Date(cleanInput);

            const options = { day: '2-digit', month: 'long', year: 'numeric' };
            const formatted = new Intl.DateTimeFormat('fr-FR', options).format(date);

            console.log(formatted); 
            return formatted;
        }

        // Affiche un tracé sur la carte
        async function showHikeOnMap(parcour) {
            // Nettoie les éléments précédents
            if (currentHikeLayer) map.removeLayer(currentHikeLayer);
            markers.forEach(marker => map.removeLayer(marker));
            markers = [];
            
            if (USE_MOCK_DATA) {
                coordonnees = [
                    {
                        id: 1,
                        latitude: 45.8990,
                        longitude: 6.1295
                    },
                    {
                        id: 2,
                        latitude: 45.8998,
                        longitude: 6.1532
                    },
                    {
                        id: 3,
                        latitude: 45.8912,
                        longitude: 6.1655
                    },
                    {
                        id: 4,
                        latitude: 45.8610,
                        longitude: 6.1860
                    },
                    {
                        id: 5,
                        latitude: 45.8375,
                        longitude: 6.2090
                    },
                    {
                        id: 6,
                        latitude: 45.8220,
                        longitude: 6.2150
                    },
                    {
                        id: 7,
                        latitude: 45.8010,
                        longitude: 6.2170
                    },
                    {
                        id: 8,
                        latitude: 45.7940,
                        longitude: 6.2070
                    },
                    {
                        id: 9,
                        latitude: 45.7980,
                        longitude: 6.1900
                    },
                    {
                        id: 10,
                        latitude: 45.8130,
                        longitude: 6.1630
                    },
                    {
                        id: 11,
                        latitude: 45.8270,
                        longitude: 6.1450
                    },
                    {
                        id: 12,
                        latitude: 45.8590,
                        longitude: 6.1340
                    }
                ]
            }
            else {
                // le path
                const response = await fetch(`${API_BASE_URL}/parcours/${parcour.id}/coordonates`, {
                            headers: {
                                'Authorization': `Bearer ${user.token}`
                            }
                        });

                var coordonnees = await response.json();
                    
                // Transforme les données API au format attendu
                coordonnees = coordonnees.map(coordonne => ({
                    id: coordonne.idCoordonates,
                    latitude: coordonne.latitude,
                    longitude: coordonne.longitude
                }));
            }

            if (coordonnees.length > 0) {
                currentHikeLayer = L.polyline(coordonnees.map(c => [c.latitude, c.longitude]), {
                    color: '#3498db',
                    weight: 5,
                    opacity: 0.8
                }).addTo(map);

                // Ajoute marqueur de départ
                markers.push(L.marker([coordonnees[0].latitude, coordonnees[0].longitude], {
                    icon: L.divIcon({
                        className: 'start-marker',
                        html: '🚩',
                        iconSize: [30, 30]
                    })
                }).addTo(map).bindPopup(`Départ: ${parcour.id}`));

                // Centre la carte sur le tracé
                map.fitBounds(currentHikeLayer.getBounds());
            }
        }

        // Charge les randonnées depuis l'API ou mock
        async function loadHikes(user) {
            if (USE_MOCK_DATA) {
                // Données simulées
                return [
                    {
                        id: 1,
                        startDate: parseDate("2025-05-20 14:42:39.248105"),
                        endDate: parseDate("2025-05-20 14:42:39.248105")
                    },
                    {
                        id: 2,
                        startDate: parseDate("2025-05-20 14:42:39.248105"),
                        endDate: parseDate("2025-05-20 14:42:39.248105")
                    }

                ];
            } else {
                try {
                    const response = await fetch(`${API_BASE_URL}/parcours/${user.id}`, {
                        headers: {
                            'Authorization': `Bearer ${user.token}`
                        }
                    });
                    
                    const data = await response.json();
                    
                    // Transforme les données API au format attendu
                    return data.map(parcour => ({
                        id: parcour.id,
                        startDate: parseDate(parcour.startDate),
                        endDate: parseDate(parcour.endDate)
                    }));
                    
                } catch (error) {
                    console.error("Erreur chargement randonnées:", error);
                    return [];
                }
            }
        }

        // Initialise la carte
        initMap();

        // Affiche les randonnées
        parcours.forEach(parcour => {
            const listItem = document.createElement('li');
            listItem.innerHTML = `
                <strong>Start Date</strong> : <strong>${parcour.startDate}</strong><br>
                <strong>End Date</strong> : <strong>${parcour.endDate}</strong><br>
            `;
            
            listItem.addEventListener('click', () => {
                document.querySelectorAll('#hikes-list li').forEach(li => {
                    li.classList.remove('active');
                });
                listItem.classList.add('active');
                showHikeOnMap(parcour);
            });
            
            parcoursList.appendChild(listItem);
        });

        // Affiche la première randonnée si disponible
        if (parcours.length > 0) {
            const firstListItem = parcoursList.querySelector('li');
            if (firstListItem) {
                firstListItem.click();
            }
        }
    }
});