package com.fallz.backend.broker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fallz.backend.entities.Coordonates;
import com.fallz.backend.entities.Device;
import com.fallz.backend.entities.Fall;
import com.fallz.backend.entities.Parcours;
import com.fallz.backend.entities.Person;
import com.fallz.backend.repositories.CoordonatesRepository;
import com.fallz.backend.repositories.DeviceRepository;
import com.fallz.backend.repositories.FallRepository;
import com.fallz.backend.repositories.ParcoursRepository;
import com.fallz.backend.repositories.PersonRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

	// Local MQTT
//	private final String MQTT_HOST_NAME = "localhost";
//	private final int MQTT_HOST_PORT = 1883;
//	private final String MQTT_CLIENT_ID = "Back";
//	private final String FALL_TOPIC = "fallz/fall";
//	private final String USERNAME = "Back";
//	private final String PASSWORD = "FallZ25*";

	//TTN MQTT
    private final String MQTT_HOST_NAME = "eu2.cloud.thethings.industries";
    private final int MQTT_HOST_PORT = 1883;
    private final String MQTT_CLIENT_ID = "Subscribe_DashBoard_1";
    private final String FALL_TOPIC = "v3/" + APPLICATION_ID + "@" + TENANT_ID + "/devices/" + DEVICE_ID + "/up";
    private final String USERNAME = "detecteur-chute@fallz";
    private final String PASSWORD = "NNSXS.NXMX24ZDGYOOYOL435B2UWISKYPRVALQJMKMTPA.MYK4TUW5RDKXJG3OZSMXZTT4IWWKQYC3SJ35TFORYYT672CJLO6A";

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
				public void messageArrived(String topic, MqttMessage message) throws JsonProcessingException {
					String payload = new String(message.getPayload());
					logger.info("📥 Message reçu sur {} : {}", topic, payload);

					// try {
					// Parse the message as JSON


					// Debug logging to help diagnose issues
					// logger.debug("Structure JSON reçue: {}", jsonNode.toString());

					// Parse as CSV :
					String[] csvValues = payload.split(",");

					if (csvValues.length > 0) {

		                String firstValue = csvValues[0].trim(); // Récupérer la première valeur et supprimer les espaces blancs

		                logger.debug("Première valeur CSV extraite: {}", firstValue);

		                if ("fall".equalsIgnoreCase(firstValue)) {

						logger.debug("Première valeur CSV extraite: {}", firstValue);

						ObjectNode rootNode = objectMapper.createObjectNode();

				        rootNode.put("type", csvValues[0].trim()); // Should be "fall"

				        // Coordonate object
				        ObjectNode coordonateNode = objectMapper.createObjectNode();
				        coordonateNode.put("latitude", Double.parseDouble(csvValues[1].trim()));
				        coordonateNode.put("longitude", Double.parseDouble(csvValues[2].trim()));
				        coordonateNode.put("date", csvValues[3].trim());

				        rootNode.set("coordonate", coordonateNode);

				        // Person object
				        ObjectNode personNode = objectMapper.createObjectNode();
				        personNode.put("id", csvValues[4].trim());

				        rootNode.set("person", personNode);

				        handleFallMessage(rootNode);
		                }else{
							JsonNode jsonNode = objectMapper.readTree(payload);
							handleRouteMessage(jsonNode);
						}
						}

					// Check the "type" field to determine the message type
					// if (jsonNode.has("type")) {
					// String type = jsonNode.get("type").asText();
					// if ("fall".equals(type)) {
					// handleFallMessage(jsonNode);
					// } else if ("parcours".equals(type)) {
					// handleRouteMessage(jsonNode);
					// } else {
					// logger.warn("Type de message non reconnu: {}", type);
					// }
					// } else {
					// logger.warn("Champ 'type' manquant dans le message: {}", payload);
					// }
					// } catch (IOException e) {
					// logger.error("Erreur lors du parsing JSON: {}", e.getMessage());
					// logger.debug("Contenu JSON invalide: {}", payload);
					// }
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// Non utilisé ici car on ne publie pas
				}
			});

			client.connect(options);
			client.subscribe(FALL_TOPIC);
			logger.info("✅ Connecté à MQTT et abonné à {}", FALL_TOPIC);

		} catch (MqttException e) {
			logger.error("❌ Erreur de connexion au broker MQTT : {}", e.getMessage());
		}
	}

	/**
	 * Traitement des données de parcours (positions GPS)
	 */
	@Transactional
	public void handleRouteMessage(JsonNode payload) {
		logger.info("Traitement des données de parcours");
		try {
			// Extract device ID from the payload
			UUID deviceId = null;
			if (payload.has("device_id")) {
				try {
					deviceId = UUID.fromString(payload.get("device_id").asText());
					logger.info("Device ID trouvé dans le payload: {}", deviceId);
				} catch (IllegalArgumentException e) {
					logger.error("ID de device invalide dans le payload: {}", payload.get("device_id").asText());
				}
			} else if (payload.has("person") && payload.get("person").has("id")) {
				// If no device_id but person id is provided, try to get the device from the
				// person
				String personId = payload.get("person").get("id").asText();
				Optional<Person> person = personRepository.findById(UUID.fromString(personId));
				if (person.isPresent() && person.get().getDevice() != null) {
					deviceId = person.get().getDevice().getId();
					logger.info("Device ID trouvé via person ID: {}", deviceId);
				} else {
					logger.warn("Personne ou appareil non trouvé pour l'ID: {}", personId);
				}
			}

			if (deviceId == null) {
				logger.error("Pas de device_id valide trouvé dans le message, impossible de créer un parcours");
				return;
			}

			// Now that we have a device ID, get the device
			var optionalDevice = deviceRepository.findById(deviceId);
			if (optionalDevice.isEmpty()) {
				logger.error("Device avec ID {} non trouvé dans la base de données", deviceId);
				return;
			}
			Device device = optionalDevice.get();

			// Check for coordinates in different possible fields
			List<JsonNode> coordinateNodes = new ArrayList<>();
			if (payload.has("coordonates") && payload.get("coordonates").isArray()) {
				JsonNode coordonatesArray = payload.get("coordonates");
				for (JsonNode node : coordonatesArray) {
					coordinateNodes.add(node);
				}
			} else if (payload.has("coordonate") && payload.get("coordonate").isArray()) {
				JsonNode coordonateArray = payload.get("coordonate");
				for (JsonNode node : coordonateArray) {
					coordinateNodes.add(node);
				}
			} else if (payload.has("coordonate") && payload.get("coordonate").isObject()) {
				coordinateNodes.add(payload.get("coordonate"));
			}

			if (coordinateNodes.isEmpty()) {
				logger.warn("Aucune coordonnée trouvée dans le message. Format attendu: 'coordonates' ou 'coordonate'");
				return;
			}

			// Process all coordinates and find min/max dates
			LocalDateTime minDate = null;
			LocalDateTime maxDate = null;
			List<CoordinateData> processedCoordinates = new ArrayList<>();

			for (JsonNode coordNode : coordinateNodes) {
				if (!coordNode.has("latitude") || !coordNode.has("longitude")) {
					logger.warn("Les champs latitude et/ou longitude sont manquants pour une coordonnée, ignorée");
					continue;
				}

				double latitude = coordNode.get("latitude").asDouble();
				double longitude = coordNode.get("longitude").asDouble();

				// Validate coordinate values
				if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
					latitude = Math.max(-90, Math.min(90, latitude));
					longitude = Math.max(-180, Math.min(180, longitude));
				}

				// Parse the date
				LocalDateTime dateTime = LocalDateTime.now();
				if (coordNode.has("date") && !coordNode.get("date").asText().isEmpty()) {
					try {
						String dateStr = coordNode.get("date").asText();
						// Try parsing as timestamp first
						try {
							long timestamp = Long.parseLong(dateStr);
							dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC);
						} catch (NumberFormatException e) {
							// Try as ISO format
							try {
								dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
							} catch (DateTimeParseException ex) {
								logger.warn("Format de date non reconnu '{}', utilisation de la date actuelle",
										dateStr);
							}
						}
					} catch (Exception e) {
						logger.warn("Erreur lors du parsing de la date: {}", e.getMessage());
					}
				}

				// Update min and max dates
				if (minDate == null || dateTime.isBefore(minDate)) {
					minDate = dateTime;
				}
				if (maxDate == null || dateTime.isAfter(maxDate)) {
					maxDate = dateTime;
				}

				// Store the processed coordinate for later use
				processedCoordinates.add(new CoordinateData(latitude, longitude, dateTime));
			}

			if (processedCoordinates.isEmpty()) {
				logger.warn("Aucune coordonnée valide n'a pu être extraite");
				return;
			}

			// If minDate or maxDate is still null, use current time
			if (minDate == null)
				minDate = LocalDateTime.now();
			if (maxDate == null)
				maxDate = LocalDateTime.now();

			// Create a new parcours with the min and max dates
			Parcours newParcours = new Parcours();
			newParcours.setId(UUID.randomUUID());
			newParcours.setDevice(device);
			newParcours.setStartDate(minDate);
			newParcours.setEndDate(maxDate);

			// Save the new parcours
			parcoursRepository.save(newParcours);
			logger.info("Nouveau parcours créé avec ID: {}, startDate: {}, endDate: {}", newParcours.getId(), minDate,
					maxDate);

			// Now save all coordinates with the new parcours
			for (CoordinateData coord : processedCoordinates) {
				Coordonates coordonates = new Coordonates();
				coordonates.setIdCoordonates(UUID.randomUUID());
				coordonates.setLatitude(coord.latitude);
				coordonates.setLongitude(coord.longitude);
				coordonates.setDate(coord.dateTime);
				coordonates.setParcours(newParcours);

				coordonatesRepository.save(coordonates);
			}

			logger.info("{} coordonnées de parcours sauvegardées avec succès", processedCoordinates.size());

		} catch (Exception e) {
			logger.error("Erreur lors du traitement des données de parcours: {}", e.getMessage(), e);
		}
	}

	/**
	 * Classe utilitaire pour stocker temporairement les données de coordonnées
	 */
	private static class CoordinateData {
		public double latitude;
		public double longitude;
		public LocalDateTime dateTime;

		public CoordinateData(double latitude, double longitude, LocalDateTime dateTime) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.dateTime = dateTime;
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
			boolean fallDetected = payload.has("fall") ? payload.get("fall").asBoolean()
					: (payload.has("FALL") ? payload.get("FALL").asBoolean()
							: (payload.has("sos") ? payload.get("sos").asBoolean()
									: (payload.has("SOS") ? payload.get("SOS").asBoolean() : false)));

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
					UUID personUuid = UUID.fromString("a2cf729b-fbd2-4b13-91e1-61118daf7627");
					Optional<Person> optionalPerson = personRepository.findById(personUuid);
					if (optionalPerson.isPresent()) {
						person = optionalPerson.get();

						// Si la personne a un appareil associé et que l'appareil a un parcours actif
						if (person.getDevice() != null) {
							Optional<Parcours> activeParcours = parcoursRepository
									.findByDeviceIdAndEndDateIsNull(person.getDevice().getId());
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

			// Si aucun parcours n'a pu être associé à cette coordonnée, on ne peut pas la
			// sauvegarder
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
	 * Méthode utilitaire pour trouver un parcours actif Soit en utilisant l'ID de
	 * l'appareil, soit en prenant le dernier parcours sans date de fin
	 */
	private Optional<Parcours> findActiveParcours(String deviceId) {
		if (deviceId != null && !deviceId.isEmpty()) {
			// Rechercher le device par son ID et récupérer son parcours actif
			return parcoursRepository.findByDeviceIdAndEndDateIsNull(UUID.fromString(deviceId));
		}

		// Sinon, prendre le premier parcours actif trouvé (à adapter selon votre
		// logique métier)
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
