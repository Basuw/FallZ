package com.fallz.backend.broker;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fallz.backend.entities.Coordonates;
import com.fallz.backend.entities.Parcours;
import com.fallz.backend.entities.Sos;
import com.fallz.backend.repositories.CoordonatesRepository;
import com.fallz.backend.repositories.ParcoursRepository;
import com.fallz.backend.repositories.SosRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CoordonatesRepository coordonatesRepository;

    @Autowired
    private SosRepository sosRepository;

    @Autowired
    private ParcoursRepository parcoursRepository;

    private final String APPLICATION_ID = "detecteur-chute";
    private final String TENANT_ID = "fallz";
    private final String DEVICE_ID = "arduino-mkr-1300";
    private final String USERNAME = "detecteur-chute@fallz";
    private final String PASSWORD = "NNSXS.NXMX24ZDGYOOYOL435B2UWISKYPRVALQJMKMTPA.MYK4TUW5RDKXJG3OZSMXZTT4IWWKQYC3SJ35TFORYYT672CJLO6A";

    private final String MQTT_HOST_NAME = "eu2.cloud.thethings.industries";
    private final int MQTT_HOST_PORT = 1883;
    private final String MQTT_CLIENT_ID = "Subscribe_DashBoard_1";
    private final String TOPIC = "v3/" + APPLICATION_ID + "@" + TENANT_ID + "/devices/" + DEVICE_ID + "/up";

    private MqttClient client;

    //@PostConstruct
    public void start() {
        try {
            String brokerUri = "tcp://" + MQTT_HOST_NAME + ":" + MQTT_HOST_PORT;
            client = new MqttClient(brokerUri, MQTT_CLIENT_ID, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("❌ Connexion MQTT perdue : {}", cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    logger.info("📥 Message reçu sur {} : {}", topic, payload);

                    try {
                        // Parse le message en JSON
                        JsonNode jsonNode = objectMapper.readTree(payload);

                        // Vérifier le type de message (en fonction de la structure)
                        if (jsonNode.has("uplink_message") && jsonNode.get("uplink_message").has("decoded_payload")) {
                            JsonNode decodedPayload = jsonNode.get("uplink_message").get("decoded_payload");

                            if (decodedPayload.has("coordinates") && decodedPayload.get("coordinates").isArray()) {
                                // Traitement pour un tableau de coordonnées
                                handleCoordinatesMessage(decodedPayload);
                            } else if (decodedPayload.has("sos") || decodedPayload.has("SOS")) {
                                // Traitement pour un SOS
                                handleSosMessage(decodedPayload);
                            } else {
                                logger.warn("Type de message non reconnu: {}", payload);
                            }
                        } else {
                            logger.warn("Format de message non reconnu: {}", payload);
                        }
                    } catch (IOException e) {
                        logger.error("Erreur lors du parsing JSON: {}", e.getMessage());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Non utilisé ici car on ne publie pas
                }
            });

            client.connect(options);
            client.subscribe(TOPIC);
            logger.info("✅ Connecté à MQTT TTN et abonné à {}", TOPIC);

        } catch (MqttException e) {
            logger.error("❌ Erreur de connexion au broker MQTT : {}", e.getMessage());
        }
    }

    private void handleCoordinatesMessage(JsonNode payload) {
        logger.info("SOS Handle");
        try {
            // Extraction des coordonnées
            JsonNode coordinates = payload.get("coordinates");
            double latitude = coordinates.get(0).asDouble();
            double longitude = coordinates.get(1).asDouble();

            logger.info("Coordonnées extraites: lat={}, long={}", latitude, longitude);

            // Création d'une nouvelle entité Coordonates
            Coordonates coordonates = new Coordonates();
            coordonates.setLatitude(latitude);
            coordonates.setLongitude(longitude);

            // Récupération du parcours actif pour associer les coordonnées
            // Note: Il faudrait adapter cette logique à votre modèle de données
            Optional<Parcours> activeParcours = findActiveParcours();

            if (activeParcours.isPresent()) {
                coordonates.setParcours(activeParcours.get());

                // Sauvegarde des coordonnées dans la base de données
                coordonatesRepository.save(coordonates);
                logger.info("Coordonnées sauvegardées en base de données avec ID: {}", coordonates.getIdCoordonates());
            } else {
                logger.error("Impossible de sauvegarder les coordonnées: aucun parcours actif trouvé");
            }

        } catch (Exception e) {
            logger.error("Erreur lors du traitement des coordonnées: {}", e.getMessage());
        }
    }

    /**
     * Traitement pour un message de SOS
     */
    private void handleSosMessage(JsonNode payload) {
        logger.info("Traitement 2: Message SOS reçu");
        try {
            // Extraction des informations SOS
            boolean sosActive = payload.has("sos") ?
                payload.get("sos").asBoolean() :
                payload.get("SOS").asBoolean();

            // Extraction des coordonnées si présentes
            Coordonates coordonates = null;

            if (payload.has("coordinates") && payload.get("coordinates").isArray()) {
                double latitude = payload.get("coordinates").get(0).asDouble();
                double longitude = payload.get("coordinates").get(1).asDouble();

                // Récupération du parcours actif
                Optional<Parcours> activeParcours = findActiveParcours();

                if (activeParcours.isPresent()) {
                    // Création et sauvegarde des coordonnées
                    coordonates = new Coordonates();
                    coordonates.setLatitude(latitude);
                    coordonates.setLongitude(longitude);
                    coordonates.setParcours(activeParcours.get());
                    coordonatesRepository.save(coordonates);

                    logger.info("Coordonnées de SOS sauvegardées: lat={}, long={}", latitude, longitude);
                } else {
                    logger.error("Impossible de créer les coordonnées pour le SOS: aucun parcours actif trouvé");
                    return;
                }
            } else {
                logger.error("Le message SOS ne contient pas de coordonnées");
                return;
            }

            // Création et sauvegarde du SOS
            if (coordonates != null) {
                Sos sos = new Sos();
                sos.setCoordonates(coordonates);
                sosRepository.save(sos);

                logger.info("SOS sauvegardé en base de données avec ID: {}", sos.getIdSos());
            }

        } catch (Exception e) {
            logger.error("Erreur lors du traitement du SOS: {}", e.getMessage());
        }
    }

    /**
     * Méthode utilitaire pour trouver un parcours actif
     * À adapter selon votre logique métier
     */
    private Optional<Parcours> findActiveParcours() {
        // Exemple: récupérer le premier parcours disponible
        // Remplacer par une logique réelle de récupération du parcours actif
        return parcoursRepository.findAll().stream().findFirst();
    }

    @PreDestroy
    public void stop() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                logger.info("🛑 Déconnecté proprement du broker MQTT.");
            }
        } catch (MqttException e) {
            logger.error("❌ Erreur lors de la déconnexion : {}", e.getMessage());
        }
    }
}
