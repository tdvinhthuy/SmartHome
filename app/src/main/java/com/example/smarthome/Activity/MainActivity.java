package com.example.smarthome.Activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
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
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.nio.charset.Charset;
import java.util.*;

public class MainActivity extends AppCompatActivity implements RoomListener {

    final String LIGHT = "13";
    final String TEMP_HUMID = "7";
    final String LED = "1";
    final String FAN = "10";
    final int LIGHT_HIGH = 0;
    final int LIGHT_LOW = 1;
    final int TEMP_XLOW = 2;
    final int TEMP_LOW = 3;
    final int TEMP_MEDIUM = 4;
    final int TEMP_HIGH = 5;
    public static final boolean TEST = true;
    private final String CHANNEL_ID = "Notifications";
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    private TextView tvDate;
    private MQTTService mqttService;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
//    final DatabaseReference db = FirebaseDatabase.getInstance("https://hcmut-smart-home-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private int LIGHT_CURRENT_STATE = -1;
    private int FAN_CURRENT_STATE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // MQTT
        setupMqttService();
        // setup view
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        tvDate = findViewById(R.id.tvDate);
        // channel for LIGHT - TEMP_HUMID - LED - FAN
        createNotificationChannel();
        createNotificationChannel();
        createNotificationChannel();
        createNotificationChannel();
        // notification listener
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
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.navControl:
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

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void sendDataMQTT(String data, String feed) {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);

        byte[] b = data.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttService.mqttAndroidClient.publish(feed, msg);
            Log.w("Mqtt", feed + ": " + msg);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public String getValue(int state) {
        switch (state){
            case LIGHT_HIGH: return "0";
            case LIGHT_LOW: return "1";
            case TEMP_XLOW: return "0";
            case TEMP_LOW: return "85";
            case TEMP_MEDIUM: return "170";
            case TEMP_HIGH: return "255";
            default: return "null";
        }
    }

    @Override
    public void onLedChange(int state) {
        // MQTT send
        JSONObject value = new JSONObject();
        try {
            value.put("id", LED);
            value.put("name", "LED");
            value.put("data", getValue(state));
            value.put("unit", "");
            sendDataMQTT(value.toString(), mqttService.getLED_FEED());
            if (TEST) {
                value.put("value", getValue(state).equals("1")?"ON":"OFF");
                sendDataMQTT(value.toString(), mqttService.getTEST_LED_FEED());

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFanChange(int state) {
        JSONObject value = new JSONObject();
        try {
            value.put("id", FAN);
            value.put("name", "DRV_PWM");
            value.put("data", getValue(state));
            value.put("unit", "");
            sendDataMQTT(value.toString(), mqttService.getFAN_FEED());
            if (TEST) {
                value.put("value", getValue(state));
                sendDataMQTT(value.toString(), mqttService.getTEST_FAN_FEED());
            }

        } catch (JSONException e) {
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

    private void sendOnChannel(String title, String text, int timestampId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(timestampId, builder.build());
    }

    private void listenToNotification() {
        db.collection("UnreadNotifications").orderBy("time")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) return;
                        // system notification
                        for (DocumentSnapshot dc : querySnapshot.getDocuments()) {
                                Log.d("NOTIFICATION", "New notification!");
                                NotificationItem item = dc.toObject(NotificationItem.class);
                                String title = item.getTitle();
                                String text = item.getNotification();
                                text = text + " on " + item.getStringTime();
                                String device = item.getDevice();
                                Date time = item.getTime();
                                sendOnChannel(title, text, (int)time.getTime());
                                dc.getReference().delete();
                        }
                    }
                });
    }
}
