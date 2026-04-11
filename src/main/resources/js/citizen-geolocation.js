/**
 * 📍 GÉOLOCALISATION AUTOMATIQUE POUR CITOYEN
 * Améliore l'expérience utilisateur avec localisation automatique
 */

class CitizenGeolocation {
    constructor() {
        this.currentPosition = null;
        this.watchId = null;
    }

    /**
     * 📍 OBTENIR LA POSITION ACTUELLE
     */
    getCurrentPosition() {
        return new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                reject(new Error("Géolocalisation non supportée par ce navigateur"));
                return;
            }

            const options = {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 300000 // 5 minutes
            };

            navigator.geolocation.getCurrentPosition(
                (position) => {
                    this.currentPosition = {
                        lat: position.coords.latitude,
                        lng: position.coords.longitude,
                        accuracy: position.coords.accuracy
                    };
                    resolve(this.currentPosition);
                },
                (error) => {
                    let message = "Erreur de géolocalisation";
                    switch(error.code) {
                        case error.PERMISSION_DENIED:
                            message = "Géolocalisation refusée par l'utilisateur";
                            break;
                        case error.POSITION_UNAVAILABLE:
                            message = "Position indisponible";
                            break;
                        case error.TIMEOUT:
                            message = "Délai de géolocalisation dépassé";
                            break;
                    }
                    reject(new Error(message));
                },
                options
            );
        });
    }

    /**
     * 🎯 CENTRER LA CARTE SUR LA POSITION UTILISATEUR
     */
    async centerMapOnUser(map) {
        try {
            const position = await this.getCurrentPosition();
            
            // Centrer la carte
            map.setView([position.lat, position.lng], 16);
            
            // Ajouter marqueur utilisateur
            const userMarker = L.marker([position.lat, position.lng], {
                icon: L.divIcon({
                    className: 'user-location-marker',
                    iconSize: [20, 20],
                    html: '📍'
                })
            }).addTo(map);
            
            userMarker.bindPopup(`
                <div style="text-align: center;">
                    <h4>📍 Votre position</h4>
                    <p>Précision: ${Math.round(position.accuracy)}m</p>
                    <button onclick="useMyLocation(${position.lat}, ${position.lng})" 
                            style="background: #4CAF50; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer;">
                        ✅ Utiliser cette position
                    </button>
                </div>
            `).openPopup();
            
            return position;
        } catch (error) {
            console.error("Erreur géolocalisation:", error);
            throw error;
        }
    }

    /**
     * 🔍 RECHERCHE D'ADRESSE (Nominatim OpenStreetMap)
     */
    async searchAddress(query, bounds = null) {
        try {
            let url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=5`;
            
            // Limiter à la région de Dakar si bounds fourni
            if (bounds) {
                url += `&viewbox=${bounds.west},${bounds.south},${bounds.east},${bounds.north}&bounded=1`;
            }
            
            const response = await fetch(url);
            const results = await response.json();
            
            return results.map(result => ({
                display_name: result.display_name,
                lat: parseFloat(result.lat),
                lng: parseFloat(result.lon),
                importance: result.importance
            }));
        } catch (error) {
            console.error("Erreur recherche adresse:", error);
            return [];
        }
    }

    /**
     * 🔄 GÉOCODAGE INVERSE (coordonnées -> adresse)
     */
    async reverseGeocode(lat, lng) {
        try {
            const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`;
            const response = await fetch(url);
            const result = await response.json();
            
            if (result && result.display_name) {
                return {
                    address: result.display_name,
                    details: result.address || {}
                };
            }
            return null;
        } catch (error) {
            console.error("Erreur géocodage inverse:", error);
            return null;
        }
    }
}

// Instance globale
const citizenGeo = new CitizenGeolocation();

// Fonctions appelées depuis Java
function enableGeolocation(map) {
    return citizenGeo.centerMapOnUser(map);
}

function searchLocation(query, bounds) {
    return citizenGeo.searchAddress(query, bounds);
}

function getAddressFromCoords(lat, lng) {
    return citizenGeo.reverseGeocode(lat, lng);
}

function useMyLocation(lat, lng) {
    if (window.javaAgent && typeof window.javaAgent.setAgentPosition === 'function') {
        window.javaAgent.setAgentPosition(lat, lng);
    }
}