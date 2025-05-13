package com.fallz.backend.broker;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

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
                    logger.info("📥 Message reçu sur {} : {}", topic, new String(message.getPayload()));
                    // ici tu peux parser le JSON reçu et le traiter
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
