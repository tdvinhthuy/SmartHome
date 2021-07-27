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
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.Query;
import com.google.type.Date;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


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


    final String LED_FEED = "CSE_BBC/feeds/bk-iot-led";
    final String FAN_FEED = "CSE_BBC/feeds/bk-iot-drv";
    private TextView tvDate;
    private MQTTService mqttService;
    final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
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
            case TEMP_HIGH: return "25";
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
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendDataMQTT(value.toString(), LED_FEED);
    }

    @Override
    public void onFanChange(int state) {
        JSONObject value = new JSONObject();
        try {
            value.put("id", FAN);
            value.put("name", "DRV_PWM");
            value.put("data", getValue(state));
            value.put("unit", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendDataMQTT(value.toString(), FAN_FEED);
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
        db.child("Notifications").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NotificationItem item = snapshot.getValue(NotificationItem.class);
                try {
                    String title = item.getTitle();
                    String text = item.getNotification();
                    sendOnChannel(title, text);
                } catch (Exception e) {
                    Log.e("listen notification: ", e.toString());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
