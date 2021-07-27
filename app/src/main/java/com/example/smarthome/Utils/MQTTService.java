package com.example.smarthome.Utils;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MQTTService {
    private final String serverUri = "tcp://io.adafruit.com:1883";
//    private String serverUri = "https://io.adafruit.com/";
    private final String clientID = "";
    private final String username = "CSE_BBC";
    private final String key = "";
    private final String LED_FEED = "CSE_BBC/feeds/bk-iot-led";
    private final String FAN_FEED = "CSE_BBC/feeds/bk-iot-drv";
    private List<String> FEEDS;
    public MqttAndroidClient mqttAndroidClient;

    public MQTTService(Context context) {
        // mqtt client
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientID);

        // feeds
        FEEDS = new ArrayList<>();
        FEEDS.add(LED_FEED);
        FEEDS.add(FAN_FEED);

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
        mqttConnectOptions.setPassword(key.toCharArray());

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
                    subscribeToFeed(FEEDS);
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

    public void subscribeToFeed(List<String> feeds) {
        for (String feed : feeds) {
            try {
                mqttAndroidClient.subscribe(feed, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.w("Mqtt", "Subscribed to " + feed + "!");
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