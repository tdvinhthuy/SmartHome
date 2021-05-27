package com.example.smarthome.Activity;

import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.smarthome.Fragment.*;
import com.example.smarthome.R;
import com.example.smarthome.Utils.MQTTService;
import com.example.smarthome.Utils.Room;
import com.example.smarthome.Utils.RoomListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.type.Date;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements RoomListener {
    private TextView tvDate;
    private Room room;
    private MQTTService mqttService;
    // topic
    final String topicLight = "smarthomehcmut/feeds/light";
    final String topicFan = "smarthomehcmut/feeds/fan";
    //db
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // MQTT
        setupMqttService();
        // setup view
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        tvDate = findViewById(R.id.tvDate);
        // room
        room = null;
        // set date
        setDate();
        // fragment
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commitNow();
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navHome:
                        selectedFragment = new HomeFragment(room);
                        break;
                    case R.id.navControl:
                        selectedFragment = new ControlFragment(room);
                        break;
                    case R.id.navNotifications:
                        selectedFragment = new NotificationsFragment();
                        break;
                    case R.id.navStatistics:
                        selectedFragment = new StatisticsFragment();
                        break;
                    case R.id.navAccount:
                        selectedFragment = new AccountFragment();
                        break;
                    default:
                        break;
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commitNow();

                return true;
            }
        });
    }

    private void setDate() {
        String[] months = {
                "Jan", "Feb", "Mar", "Apr",
                "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec"};
        Calendar cal = Calendar.getInstance();
        tvDate.setText(String.format("%s %s, %s",
                months[cal.get(Calendar.MONTH)],
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.YEAR)));
    }

    @Override
    public void onRoomChange(Room room, boolean control) {
        this.room = room;
        Fragment fragment;
        if (control) {
            fragment = new ControlFragment(room);
        }
        else {
            fragment = new HomeFragment(room);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, fragment)
                .commitNow();
    }

    @Override
    public void onLightChange(int state) {
        // MQTT send
        String key = getDeviceKey(false);
        String value = getDeviceValue(state, false);
        String topic = getTopic(key);
        JSONObject data = new JSONObject();
        try {
            data.put("key", key);
            data.put("value", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendDataMQTT(data.toString(), topic);
        writeDeviceState(value, false);
    }

    @Override
    public void onFanChange(int state) {
        // send MQTT
        String key = getDeviceKey(true);
        String value = getDeviceValue(state, true);
        String topic = getTopic(key);
        JSONObject data = new JSONObject();
        try {
            data.put("key", key);
            data.put("value", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendDataMQTT(data.toString(), topic);
        // store to database
        Map<String, Object> history = new HashMap<>();
        String fanState = state==0?"OFF":(state==1?"LOW":(state==2?"MEDIUM":"HIGH"));
        writeDeviceState(fanState, true);
    }

    private void writeDeviceState(String value, boolean isFan) {
        String userID = mAuth.getUid();
        FieldValue timestamp = FieldValue.serverTimestamp();
        // store to device collection
        Map<String, Object> history = new HashMap<>();
        history.put("room_name", room.getName());
        history.put("state", value);
        history.put("timestamp", timestamp);
        history.put("userID", userID);
        String cDevice = isFan?"fan_states":"light_states";
        db.collection(cDevice)
                .add(history)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEVICE_STATE_WRITE", "Write success: " + history);
                        }
                        else {
                            Log.d("DEVICE_STATE_WRITE", "Write fail");
                        }
                    }
                });
        // store to notification collection
        String notificationType = "";
        notificationType = notificationType + (isFan ? "FAN_" : "LIGHT_");
        notificationType = notificationType + value;
        Map<String, Object> notification = new HashMap<>();
        notification.put("room_name", room.getName());
        notification.put("type", notificationType);
        notification.put("timestamp", timestamp);
        notification.put("userID", userID);
        db.collection("notifications")
                .add(notification)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Log.d("NOTIFICATION_STATE", "Notify state sucess: "
                                    + notification);
                        }
                        else {
                            Log.d("NOTIFICATION_STATE", "Notify state fail");
                        }
                    }
                });
    }

    private String getTopic(String key) {
        return "smarthomehcmut/feeds/" + key;
    }

    private String getDeviceKey(boolean isFan) {
        String roomName = room.getName().toLowerCase();
        String key = "";
        char index = roomName.charAt(roomName.length() - 1);
        if (isFan) {
            key += "fan-";
        }
        else {
            key += "light-";
        }
        if (roomName.contains("living room")) {
            key += "l-" + index;
        }
        else if (roomName.contains("bedroom")) {
            key += "be-" + index;
        }
        else if (roomName.contains("kitchen")) {
            key += "k-" + index;
        }
        else if (roomName.contains("bathroom")) {
            key += "ba-" + index;
        }
        return key;
    }

    private String getDeviceValue(int state, boolean isFan) {
        if (isFan) {
            return String.valueOf(state);
        }
        else {
            return (state == 0)?"OFF":"ON";
        }
    }

    private void setupMqttService() {
        // MQTT Service
        mqttService = new MQTTService(this);
        //mqttService.addTopic(topicLight);
        //mqttService.addTopic(topicFan);
        mqttService.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("Mqtt", topic + ": " + message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void sendDataMQTT(String data, String topic) {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);

        byte[] b = data.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttService.mqttAndroidClient.publish(topic, msg);
            Log.w("Mqtt", topic + ": " + msg);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
