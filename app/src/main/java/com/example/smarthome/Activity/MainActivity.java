package com.example.smarthome.Activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.util.JsonReader;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import com.example.smarthome.Fragment.*;
import com.example.smarthome.R;
import com.example.smarthome.Utils.*;
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
import org.json.JSONStringer;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements RoomListener {
    private TextView tvDate;
    //private Room room;
    private MQTTService mqttService;
    // topic
    final String topicLight = "smarthomehcmut/feeds/bk-iot-led";
    final String topicFan = "smarthomehcmut/feeds/bk-iot-traffic";
    // db
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    // notification
    final String CHANNEL_ID = "NOTIFICATION";
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
        //room = null;
        // notification
        createNotificationChannel();
        listenToNotification();
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
//                        selectedFragment = new HomeFragment(room);
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.navControl:
//                        selectedFragment = new ControlFragment(room);
                        selectedFragment = new ControlFragment();
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

    /*
    @Override
    public void onRoomChange(Room room, boolean control) {
        //this.room = room;
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
     */

    @Override
    public void onLightChange(int state, boolean bAuto) {
        // MQTT send
        JSONObject data = getJSONData(state ,false);
        String topic = topicLight;
        sendDataMQTT(data.toString(), topic);
        // store to database
        String lightState = state==0?"OFF":"ON";
        writeDeviceState(lightState, false, bAuto);
    }

    @Override
    public void onFanChange(int state, boolean bAuto) {
        // send MQTT
        JSONObject data = getJSONData(state ,true);
        String topic = topicFan;
        sendDataMQTT(data.toString(), topic);
        // store to database
        String fanState = state==0?"OFF":(state==1?"LOW":(state==2?"MEDIUM":"HIGH"));
        writeDeviceState(fanState, true, bAuto);
    }

    @Override
    public void onLightDataArrived(float data) {
        db.collection("light_states")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null || querySnapshot.isEmpty()) return;
                            DocumentSnapshot document =  querySnapshot.getDocuments().get(0);
                            //if (document.getString("room_name").equals(room.getName())) {
                                // get state
                                String state = document.getString("state");
                                if (state.equals("OFF") && data < 100) {
                                    // turn light ON
                                    onLightChange(1, true);
                                    Log.d("LIGHT_STATE", "Automatically turned ON!");
                                }
                                else if (state.equals("ON") && data >= 100) {
                                    // turn light OFF
                                    onLightChange(0, true);
                                    Log.d("LIGHT_STATE", "Automatically turned OFF!");
                                }
                            //}
                        }
                    }
                });
    }

    @Override
    public void onTemperatureDataArrived(float data) {
        db.collection("fan_states")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null || querySnapshot.isEmpty()) return;
                            for (DocumentSnapshot document: querySnapshot.getDocuments()) {
                                //if (document.getString("room_name").equals(room.getName())) {
                                    // get state
                                    String state = document.getString("state");
                                    if (state == null) return;
                                    if (!state.equals("OFF") && data <= 30) { // turn the Fan OFF
                                        onFanChange(0, true);
                                    }
                                    if (!state.equals("HIGH") && data > 34) { // change Fan to HIGH
                                        onFanChange(3, true);
                                    }
                                    else if (!state.equals("MEDIUM") && data > 32) { // change Fan to MEDIUM
                                        onFanChange(2, true);
                                    }
                                    else if (!state.equals("LOW") && data > 30) { // change Fan to LOW
                                        onFanChange(1, true);
                                    }
                                    return;
                                //}
                            }
                        }
                    }
                });
    }

    private void writeDeviceState(String value, boolean isFan, boolean bAuto) {
        String userID = bAuto?"":mAuth.getUid();
        FieldValue timestamp = FieldValue.serverTimestamp();
        // store to device collection
        Map<String, Object> history = new HashMap<>();
        //history.put("room_name", room.getName());
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
        //notification.put("room_name", room.getName());
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

    private JSONObject getJSONData(int state, boolean isFan) {
        JSONObject jsonObject = new JSONObject();
        String id = isFan?"6":"1";
        String name = isFan?"TRAFFIC":"LED";
        String data = Integer.toBinaryString(state);
        if (data.length() == 1) data = '0' + data;
        String unit = "";
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("data", data);
            jsonObject.put("unit", unit);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void setupMqttService() {
        // MQTT Service
        mqttService = new MQTTService(this);
        mqttService.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                processMessage(message);
                Log.d("Mqtt_receive", topic + ": " + message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void processMessage(MqttMessage message) throws JSONException {
        JSONObject jsonObject = new JSONObject(new String(message.getPayload()));
        Float data = Float.valueOf(jsonObject.getString("data"));
        if (jsonObject.get("name").equals("LIGHT")) { // light sensor
            // store to db
            //writeSensorData(jsonObject);
            // check light intensity
            onLightDataArrived(data);
        }
        else if (jsonObject.get("name").equals("TEMP-HUMID")) { // temp-humid sensor
            // store to db
            writeSensorData(jsonObject);
            // check temperature
            onTemperatureDataArrived(data);
        }
    }

    private void writeSensorData(JSONObject jsonObject) {
        try {
            String data = jsonObject.getString("data");
            String sensor_name = jsonObject.getString("name");
            String collection = sensor_name.equals("LIGHT")?"light_records":"temp_humid_records";
            Map<String, Object> record = new HashMap<>();
            if (sensor_name.equals("LIGHT")) {
                record.put("data", Integer.valueOf(data));
            }
            else {
                String[] temp_humid = data.split("-");
                record.put("temp_data", Integer.valueOf(temp_humid[0]));
                record.put("humid_data", Integer.valueOf(temp_humid[1]));
            }
            record.put("timestamp", FieldValue.serverTimestamp());
            db.collection(collection)
                    .add(record)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d("SENSOR", "Write data success: " + record);
                            }
                            else {
                                Log.d("SENSOR", "Write fail");
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    // notification
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Channel";
            String description = "This is notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendOnChannel(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }

    private void listenToNotification() {
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) return;
                        // notification view
                        // system notification
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Log.d("NOTIFICATION", "New notification!");
                                NotificationItem item = dc.getDocument().toObject(NotificationItem.class);
                                String title = item.getType().replace("_", " ").toUpperCase();
                                String text = item.getContent();
                                sendOnChannel(title, text);
                            }
                        }
                    }
                });
    }

}
