/**
 * 🗺️ CARTE GPS TEMPS RÉEL pour SmartCity
 * Comme Google Maps - Dynamique et Interactive
 */
class SmartCityGPSMap {
    constructor() {
        this.map = null;
        this.agentMarker = null;
        this.missionMarkers = [];
        this.routeLayer = null;
        this.selectedMission = null;
        this.missions = [];
        this.agentPosition = null;
        
        this.init();
    }
    
    init() {
        // Initialiser la carte centrée sur Dakar
        this.map = L.map('gps-map').setView([14.7646, -17.3920], 13);
        
        // Couche de base OpenStreetMap
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(this.map);
        
        // Style de la carte
        this.setupMapStyles();
        
        // Events
        this.map.on('click', (e) => this.onMapClick(e));
    }
    
    setupMapStyles() {
        // Ajouter du CSS personnalisé
        const style = document.createElement('style');
        style.textContent = `
            .agent-marker {
                background: #2196F3;
                width: 20px;
                height: 20px;
                border-radius: 50%;
                border: 3px solid white;
                box-shadow: 0 0 10px rgba(33, 150, 243, 0.7);
                animation: pulse 2s infinite;
            }
            
            @keyframes pulse {
                0% { box-shadow: 0 0 0 0 rgba(33, 150, 243, 0.7); }
                70% { box-shadow: 0 0 0 10px rgba(33, 150, 243, 0); }
                100% { box-shadow: 0 0 0 0 rgba(33, 150, 243, 0); }
            }
            
            .mission-marker-new {
                background: #FF5722;
                color: white;
                border-radius: 50%;
                text-align: center;
                font-weight: bold;
                font-size: 12px;
                line-height: 25px;
                border: 2px solid white;
                box-shadow: 0 2px 8px rgba(0,0,0,0.3);
            }
            
            .mission-marker-progress {
                background: #FF9800;
                color: white;
                border-radius: 50%;
                text-align: center;
                font-weight: bold;
                font-size: 12px;
                line-height: 25px;
                border: 2px solid white;
                box-shadow: 0 2px 8px rgba(0,0,0,0.3);
            }
            
            .mission-marker-done {
                background: #4CAF50;
                color: white;
                border-radius: 50%;
                text-align: center;
                font-weight: bold;
                font-size: 12px;
                line-height: 25px;
                border: 2px solid white;
                box-shadow: 0 2px 8px rgba(0,0,0,0.3);
            }
            
            .mission-marker-closest {
                animation: glow 1s ease-in-out infinite alternate;
            }
            
            @keyframes glow {
                from { box-shadow: 0 0 5px #fff, 0 0 10px #fff, 0 0 15px #FF5722; }
                to { box-shadow: 0 0 10px #fff, 0 0 20px #FF5722, 0 0 30px #FF5722; }
            }
        `;
        document.head.appendChild(style);
    }
    
    /**
     * 📍 METTRE À JOUR LA POSITION DE L'AGENT
     */
    updateAgentPosition(lat, lon) {
        this.agentPosition = { lat, lon };
        
        if (this.agentMarker) {
            this.agentMarker.setLatLng([lat, lon]);
        } else {
            this.agentMarker = L.marker([lat, lon], {
                icon: L.divIcon({
                    className: 'agent-marker',
                    iconSize: [20, 20],
                    html: '🚶'
                })
            }).addTo(this.map);
            
            this.agentMarker.bindPopup(`
                <div style="text-align: center;">
                    <h4>🎯 Votre Position</h4>
                    <p>Vous êtes ici!</p>
                    <small>Mis à jour en temps réel</small>
                </div>
            `);
        }
    }
    
    /**
     * 🎯 METTRE À JOUR TOUTES LES MISSIONS
     */
    updateMissions(missions, distances = []) {
        this.missions = missions;
        
        // Supprimer anciens marqueurs
        this.missionMarkers.forEach(marker => this.map.removeLayer(marker));
        this.missionMarkers = [];
        
        // Ajouter nouveaux marqueurs
        missions.forEach((mission, index) => {
            const distanceInfo = distances.find(d => d.mission.idSignalement === mission.idSignalement);
            const isClosest = distanceInfo ? distanceInfo.isClosest : false;
            
            const marker = this.createMissionMarker(mission, index, distanceInfo, isClosest);
            this.missionMarkers.push(marker);
            marker.addTo(this.map);
        });
        
        // Ajuster la vue pour inclure toutes les missions
        if (missions.length > 0) {
            this.fitMapToMissions();
        }
    }
    
    createMissionMarker(mission, index, distanceInfo, isClosest) {
        const statusClass = this.getMissionStatusClass(mission.statut);
        const markerClass = `mission-marker-${statusClass}${isClosest ? ' mission-marker-closest' : ''}`;
        
        const distanceText = distanceInfo ? 
            `📏 ${distanceInfo.distanceKm.toFixed(1)}km (${distanceInfo.estimatedMinutes} min)` : '';
        
        const marker = L.marker([mission.latitude, mission.longitude], {
            icon: L.divIcon({
                className: markerClass,
                iconSize: [30, 30],
                html: mission.idSignalement
            })
        });
        
        marker.bindPopup(`
            <div style="min-width: 200px;">
                <h4>${isClosest ? '⭐ ' : ''}Mission #${mission.idSignalement}</h4>
                <p><strong>📍 Zone:</strong> ${mission.zoneNom}</p>
                <p><strong>🗑️ Type:</strong> ${mission.categorie}</p>
                <p><strong>📊 Statut:</strong> ${this.getStatusEmoji(mission.statut)} ${mission.statut}</p>
                ${distanceText ? `<p><strong>${distanceText}</strong></p>` : ''}
                <hr>
                <button onclick="selectMission(${mission.idSignalement})" 
                        style="background: #2196F3; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer;">
                    ${isClosest ? '🎯 Mission la plus proche!' : '📍 Sélectionner'}
                </button>
                <button onclick="navigateToMission(${mission.idSignalement})" 
                        style="background: #4CAF50; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; margin-left: 5px;">
                    🧭 Naviguer
                </button>
            </div>
        `);
        
        // Click event
        marker.on('click', () => {
            this.selectMission(mission);
        });
        
        return marker;
    }
    
    getMissionStatusClass(status) {
        switch (status.toLowerCase()) {
            case 'en attente': return 'new';
            case 'en cours': return 'progress';
            case 'collecte': case 'terminé': return 'done';
            default: return 'new';
        }
    }
    
    getStatusEmoji(status) {
        switch (status.toLowerCase()) {
            case 'en attente': return '🔴';
            case 'en cours': return '🟠';
            case 'collecte': case 'terminé': return '✅';
            default: return '⚪';
        }
    }
    
    /**
     * 🎯 SÉLECTIONNER UNE MISSION
     */
    selectMission(mission) {
        this.selectedMission = mission;
        
        // Zoom sur la mission
        this.map.setView([mission.latitude, mission.longitude], 16);
        
        // Tracer l'itinéraire si on a la position de l'agent
        if (this.agentPosition) {
            this.showRoute(this.agentPosition, mission);
        }
        
        // Notifier Java
        if (window.missionSelectionCallback) {
            window.missionSelectionCallback(mission.idSignalement);
        }
    }
    
    /**
     * 🛣️ AFFICHER L'ITINÉRAIRE
     */
    showRoute(from, to) {
        // Supprimer ancien itinéraire
        if (this.routeLayer) {
            this.map.removeLayer(this.routeLayer);
        }
        
        // Créer nouvelle route
        const routePoints = [
            [from.lat, from.lon],
            [to.latitude, to.longitude]
        ];
        
        this.routeLayer = L.polyline(routePoints, {
            color: '#2196F3',
            weight: 4,
            opacity: 0.7,
            dashArray: '10, 10'
        }).addTo(this.map);
        
        // Ajuster la vue pour inclure la route
        this.map.fitBounds(this.routeLayer.getBounds(), { padding: [20, 20] });
        
        // Ajouter marqueur de départ et d'arrivée
        this.addRouteMarkers(from, to);
    }
    
    addRouteMarkers(from, to) {
        L.marker([from.lat, from.lon], {
            icon: L.divIcon({
                className: '',
                iconSize: [30, 30],
                html: '🏁'
            })
        }).addTo(this.map).bindPopup('🏁 Départ (Votre position)');
        
        L.marker([to.latitude, to.longitude], {
            icon: L.divIcon({
                className: '',
                iconSize: [30, 30],
                html: '🎯'
            })
        }).addTo(this.map).bindPopup('🎯 Arrivée (Mission #' + to.idSignalement + ')');
    }
    
    /**
     * 📐 AJUSTER LA VUE POUR INCLURE TOUTES LES MISSIONS
     */
    fitMapToMissions() {
        if (this.missions.length === 0) return;
        
        const group = new L.featureGroup();
        
        // Ajouter agent si présent
        if (this.agentMarker) {
            group.addLayer(this.agentMarker);
        }
        
        // Ajouter toutes les missions
        this.missionMarkers.forEach(marker => group.addLayer(marker));
        
        this.map.fitBounds(group.getBounds(), { padding: [20, 20] });
    }
    
    /**
     * 🔄 ACTUALISER LA CARTE
     */
    refresh() {
        this.map.invalidateSize();
    }
    
    onMapClick(e) {
        // Optionnel : actions sur click de carte
        console.log('Map clicked at:', e.latlng);
    }
}

// Instance globale
let gpsMap = null;

// Initialisation
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('gps-map')) {
        gpsMap = new SmartCityGPSMap();
    }
});

// Fonctions appelées depuis Java
function updateAgentPosition(lat, lon) {
    if (gpsMap) gpsMap.updateAgentPosition(lat, lon);
}

function updateMissions(missions, distances) {
    if (gpsMap) gpsMap.updateMissions(missions, distances);
}

function selectMission(missionId) {
    if (gpsMap && window.missionSelectionCallback) {
        window.missionSelectionCallback(missionId);
    }
}

function navigateToMission(missionId) {
    if (window.navigationCallback) {
        window.navigationCallback(missionId);
    }
}