package com.example.smarthome.Utils;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class MQTTService {
    private String serverUri = "tcp://io.adafruit.com:1883";
    private String clientID = "";
    private String username = "smarthomehcmut";
    private String password = "aio_yPRQ24NT0LXN6itBeH9yBa00MhVz";
    private final String topicLightSensor = "CSE_BBC1/feeds/bk-iot-light";
    private final String topicTempHumidSensor = "CSE_BBC/feeds/bk-iot-temp-humid";
    private ArrayList<String> topics;
    public MqttAndroidClient mqttAndroidClient;

    public MQTTService(Context context) {
        // subscribed topics
        topics = new ArrayList<>();
        topics.add(topicTempHumidSensor);
        topics.add(topicLightSensor);

        // mqtt client
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientID);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.w("Mqtt", serverUri);
            }

            @Override
            public void connectionLost(Throwable cause) {
                
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.w("Mqtt", message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        connect();
    }

    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions
                            disconnectedBufferOptions = new
                            DisconnectedBufferOptions();

                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);

                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to:" + serverUri + " " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic() {
        for (String topic: topics) {
            try {
                mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.w("Mqtt", "Subscribed to " + topic + "!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Mqtt", "Subscribed fail!");
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }
}