package com.fallz.backend.broker;
import com.fallz.backend.entities.Fall;
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
import com.fallz.backend.repositories.CoordonatesRepository;
import com.fallz.backend.repositories.ParcoursRepository;
import com.fallz.backend.repositories.FallRepository;
import com.fallz.backend.repositories.DeviceRepository;
import com.fallz.backend.entities.Person;
import com.fallz.backend.repositories.PersonRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CoordonatesRepository coordonatesRepository;

    @Autowired
    private ParcoursRepository parcoursRepository;

    @Autowired
    private FallRepository fallRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PersonRepository personRepository;

    private final String APPLICATION_ID = "detecteur-chute";
    private final String TENANT_ID = "fallz";
    private final String DEVICE_ID = "arduino-mkr-1300";
    private final String USERNAME = "Back";
    private final String PASSWORD = "FallZ25*";

    private final String MQTT_HOST_NAME = "localhost";
    private final int MQTT_HOST_PORT = 1883;
    private final String MQTT_CLIENT_ID = "Back";
    private final String FALL_TOPIC = "fallz/fall";
    private final String ROUTE_TOPIC = "fallz/route";

    private MqttClient client;

    @PostConstruct
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

                        // Déterminer le type de message en fonction du topic
                        if (topic.equals(FALL_TOPIC)) {
                            handleFallMessage(jsonNode);
                        } else if (topic.equals(ROUTE_TOPIC)) {
                            handleRouteMessage(jsonNode);
                        } else if (jsonNode.has("uplink_message") && jsonNode.get("uplink_message").has("decoded_payload")) {
                            // Format TTN - analyser le contenu pour déterminer le type
                            JsonNode decodedPayload = jsonNode.get("uplink_message").get("decoded_payload");

                            if (decodedPayload.has("sos") || decodedPayload.has("SOS") ||
                                decodedPayload.has("fall") || decodedPayload.has("FALL")) {
                                handleFallMessage(decodedPayload);
                            } else if (decodedPayload.has("coordinates") || decodedPayload.has("position")) {
                                handleRouteMessage(decodedPayload);
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
            client.subscribe(FALL_TOPIC);
            client.subscribe(ROUTE_TOPIC);
            logger.info("✅ Connecté à MQTT et abonné à {} et {}", FALL_TOPIC, ROUTE_TOPIC);

        } catch (MqttException e) {
            logger.error("❌ Erreur de connexion au broker MQTT : {}", e.getMessage());
        }
    }

    /**
     * Traitement des données de parcours (positions GPS)
     */
    private void handleRouteMessage(JsonNode payload) {
        logger.info("Traitement des données de parcours");
        try {
            // Extraction des coordonnées
            JsonNode coordinates = null;
            double latitude = 0.0;
            double longitude = 0.0;

            // Différentes possibilités de format pour les coordonnées
            if (payload.has("coordinates") && payload.get("coordinates").isArray()) {
                coordinates = payload.get("coordinates");
                latitude = coordinates.get(0).asDouble();
                longitude = coordinates.get(1).asDouble();
            } else if (payload.has("position")) {
                JsonNode position = payload.get("position");
                latitude = position.has("lat") ? position.get("lat").asDouble() : 0.0;
                longitude = position.has("lon") ? position.get("lon").asDouble() : 0.0;
            } else if (payload.has("latitude") && payload.has("longitude")) {
                latitude = payload.get("latitude").asDouble();
                longitude = payload.get("longitude").asDouble();
            } else {
                logger.warn("Format de coordonnées non reconnu dans le message");
                return;
            }

            logger.info("Coordonnées extraites: lat={}, long={}", latitude, longitude);

            String deviceId = payload.has("device_id") ? payload.get("device_id").asText() : null;

            // Récupération du parcours actif pour associer les coordonnées
            Optional<Parcours> activeParcours = findActiveParcours(deviceId);

            // Création d'une nouvelle entité Coordonates
            Coordonates coordonates = new Coordonates();
            coordonates.setIdCoordonates(UUID.randomUUID());
            coordonates.setLatitude(latitude);
            coordonates.setLongitude(longitude);
            coordonates.setDate(LocalDateTime.now());

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
     * Traitement des données de chute
     */
    private void handleFallMessage(JsonNode payload) {
        logger.info("Traitement d'une notification de chute");
        try {
            // Vérifier si c'est le nouveau format de payload
            if (payload.has("type") && payload.get("type").asText().equals("fall")) {
                handleNewFormatFallMessage(payload);
                return;
            }

            // Le reste du code existant pour les formats précédents
            boolean fallDetected = payload.has("fall") ?
                payload.get("fall").asBoolean() :
                (payload.has("FALL") ? payload.get("FALL").asBoolean() :
                (payload.has("sos") ? payload.get("sos").asBoolean() :
                (payload.has("SOS") ? payload.get("SOS").asBoolean() : false)));

            if (!fallDetected) {
                logger.info("Pas de chute détectée dans ce message");
                return;
            }

            // Extraction des coordonnées de la chute
            double latitude = 0.0;
            double longitude = 0.0;
            String deviceId = null;

            if (payload.has("device_id")) {
                deviceId = payload.get("device_id").asText();
            }

            // Différentes possibilités de format pour les coordonnées
            if (payload.has("coordinates") && payload.get("coordinates").isArray()) {
                latitude = payload.get("coordinates").get(0).asDouble();
                longitude = payload.get("coordinates").get(1).asDouble();
            } else if (payload.has("position")) {
                JsonNode position = payload.get("position");
                latitude = position.has("lat") ? position.get("lat").asDouble() : 0.0;
                longitude = position.has("lon") ? position.get("lon").asDouble() : 0.0;
            } else if (payload.has("latitude") && payload.has("longitude")) {
                latitude = payload.get("latitude").asDouble();
                longitude = payload.get("longitude").asDouble();
            } else {
                logger.warn("Données de position manquantes dans l'alerte de chute");
                return;
            }

            logger.info("Chute détectée aux coordonnées: lat={}, long={}", latitude, longitude);

            // Création de l'entité Coordonnées
            Coordonates coordonates = new Coordonates();
            coordonates.setIdCoordonates(UUID.randomUUID());
            coordonates.setLatitude(latitude);
            coordonates.setLongitude(longitude);
            coordonates.setDate(LocalDateTime.now());

            // Récupération du parcours actif si possible
            Optional<Parcours> activeParcours = findActiveParcours(deviceId);
            if (activeParcours.isPresent()) {
                coordonates.setParcours(activeParcours.get());
            } else {
                logger.warn("Pas de parcours actif trouvé pour cette chute");
            }

            // Sauvegarde des coordonnées
            coordonatesRepository.save(coordonates);

            // Création et sauvegarde de l'entité Fall
            Fall fall = new Fall();
            fall.setId(UUID.randomUUID());
            fall.setCoordonates(coordonates);

            // Si les informations du device/personne sont disponibles, les associer
            if (activeParcours.isPresent() && activeParcours.get().getDevice() != null
                    && activeParcours.get().getDevice().getPerson() != null) {
                fall.setPerson(activeParcours.get().getDevice().getPerson());
            }

            // Sauvegarde de la chute
            fallRepository.save(fall);
            logger.info("Chute sauvegardée en base de données avec ID: {}", fall.getId());

        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la chute: {}", e.getMessage(), e);
        }
    }

    /**
     * Traitement du nouveau format de message de chute
     */
    private void handleNewFormatFallMessage(JsonNode payload) {
        logger.info("Traitement d'une chute avec le nouveau format");
        try {
            // Extraction des coordonnées
            if (!payload.has("coordonate")) {
                logger.error("Le message ne contient pas de coordonnées");
                return;
            }

            JsonNode coordonateNode = payload.get("coordonate");
            double latitude = coordonateNode.get("latitude").asDouble();
            double longitude = coordonateNode.get("longitude").asDouble();

            // Parsing de la date si fournie
            LocalDateTime dateTime = LocalDateTime.now();
            if (coordonateNode.has("date") && !coordonateNode.get("date").asText().isEmpty()) {
                try {
                    String dateStr = coordonateNode.get("date").asText();
                    // Essayer différents formats de date possibles
                    try {
                        // Si format timestamp ou format numérique
                        long timestamp = Long.parseLong(dateStr);
                        dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC);
                    } catch (NumberFormatException e) {
                        // Si format ISO ou autre format texte
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                        dateTime = LocalDateTime.parse(dateStr, formatter);
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("Format de date non reconnu, utilisation de la date actuelle: {}", e.getMessage());
                }
            }

            logger.info("Coordonnées extraites: lat={}, long={}, date={}", latitude, longitude, dateTime);

            // Création de l'entité Coordonnées
            Coordonates coordonates = new Coordonates();
            coordonates.setIdCoordonates(UUID.randomUUID());
            coordonates.setLatitude(latitude);
            coordonates.setLongitude(longitude);
            coordonates.setDate(dateTime);

            // Récupération de la personne si ID fourni
            Person person = null;
            if (payload.has("person") && payload.get("person").has("id")) {
                String personId = payload.get("person").get("id").asText();
                try {
                    UUID personUuid = UUID.fromString(personId);
                    Optional<Person> optionalPerson = personRepository.findById(personUuid);
                    if (optionalPerson.isPresent()) {
                        person = optionalPerson.get();

                        // Si la personne a un appareil associé et que l'appareil a un parcours actif
                        if (person.getDevice() != null) {
                            Optional<Parcours> activeParcours = parcoursRepository.findByDeviceIdAndEndDateIsNull(person.getDevice().getId());
                            if (activeParcours.isPresent()) {
                                coordonates.setParcours(activeParcours.get());
                            } else {
                                logger.warn("Pas de parcours actif trouvé pour la personne {}", personId);
                                // On peut éventuellement créer un nouveau parcours pour cette coordonnée
                                Parcours newParcours = new Parcours();
                                newParcours.setId(UUID.randomUUID());
                                newParcours.setDevice(person.getDevice());
                                newParcours.setStartDate(LocalDateTime.now());
                                parcoursRepository.save(newParcours);
                                coordonates.setParcours(newParcours);
                            }
                        } else {
                            logger.error("La personne {} n'a pas d'appareil associé", personId);
                        }
                    } else {
                        logger.error("Personne non trouvée avec ID: {}", personId);
                    }
                } catch (IllegalArgumentException e) {
                    logger.error("ID de personne invalide: {}", personId);
                }
            } else {
                logger.warn("Pas d'ID de personne fourni dans le message");
            }

            // Si aucun parcours n'a pu être associé à cette coordonnée, on ne peut pas la sauvegarder
            if (coordonates.getParcours() == null) {
                logger.error("Impossible de sauvegarder les coordonnées: aucun parcours disponible");
                return;
            }

            // Sauvegarde des coordonnées
            coordonatesRepository.save(coordonates);

            // Création et sauvegarde de l'entité Fall
            Fall fall = new Fall();
            fall.setId(UUID.randomUUID());
            fall.setCoordonates(coordonates);

            // Associer la personne à la chute si disponible
            if (person != null) {
                fall.setPerson(person);
                // Sauvegarde de la chute
                fallRepository.save(fall);
                logger.info("Chute sauvegardée en base de données avec ID: {}", fall.getId());
            } else {
                logger.error("Impossible de sauvegarder la chute: aucune personne associée");
            }

        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la chute avec nouveau format: {}", e.getMessage(), e);
        }
    }

    /**
     * Méthode utilitaire pour trouver un parcours actif
     * Soit en utilisant l'ID de l'appareil, soit en prenant le dernier parcours sans date de fin
     */
    private Optional<Parcours> findActiveParcours(String deviceId) {
        if (deviceId != null && !deviceId.isEmpty()) {
            // Rechercher le device par son ID et récupérer son parcours actif
            return parcoursRepository.findByDeviceIdAndEndDateIsNull(UUID.fromString(deviceId));
        }

        // Sinon, prendre le premier parcours actif trouvé (à adapter selon votre logique métier)
        return parcoursRepository.findByEndDateIsNull().stream().findFirst();
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
