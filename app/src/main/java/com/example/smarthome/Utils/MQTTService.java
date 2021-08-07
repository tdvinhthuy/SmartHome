package com.example.smarthome.Utils;

import android.content.Context;
import android.util.Log;
import com.example.smarthome.Activity.MainActivity;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MQTTService {
    private final String serverUri = "tcp://io.adafruit.com:1883";
    private final String clientID = "";
    private String username;
    private String key;
    private String LED_FEED;
    private String FAN_FEED;
    private String TEST_LED_FEED;
    private String TEST_FAN_FEED;

    private List<String> FEEDS;
    public MqttAndroidClient mqttAndroidClient;

    public List<String> getFEEDS() {
        return FEEDS;
    }

    public String getFAN_FEED() {
        return FAN_FEED;
    }

    public String getLED_FEED() {
        return LED_FEED;
    }

    public String getTEST_LED_FEED() {
        return TEST_LED_FEED;
    }

    public String getTEST_FAN_FEED() {
        return TEST_FAN_FEED;
    }

    public void setup(boolean test) {
        if (test) {
            username = "smarthomehcmut";
            key = "";
            LED_FEED = "smarthomehcmut/feeds/bk-iot-led";
            FAN_FEED = "smarthomehcmut/feeds/bk-iot-drv";
            TEST_LED_FEED = "smarthomehcmut/feeds/LED";
            TEST_FAN_FEED = "smarthomehcmut/feeds/DRV";
        }
        else {
            username = "CSE_BBC";
            key = "";
            LED_FEED = "CSE_BBC/feeds/bk-iot-led";
            FAN_FEED = "CSE_BBC/feeds/bk-iot-drv";
        }
        // feeds
        FEEDS = new ArrayList<>();
        FEEDS.add(LED_FEED);
        FEEDS.add(FAN_FEED);
    }
    public MQTTService(Context context) {
        setup(MainActivity.TEST); // setup(boolean test)

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
