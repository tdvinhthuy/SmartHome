package com.example.smarthome.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.smarthome.Activity.MainActivity;
import com.example.smarthome.R;
import com.example.smarthome.Utils.NotificationItem;
import com.example.smarthome.Utils.RecordItem;
import com.example.smarthome.Utils.RoomListener;
import com.example.smarthome.Utils.StateItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.Query;

public class ControlFragment extends Fragment {
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


    private Spinner spinner;
    private RadioGroup rgFan;
    private RadioButton rbLow;
    private RadioButton rbMedium;
    private RadioButton rbHigh;
    private SwitchCompat switchFan;
    private SwitchCompat switchLed;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
//    final DatabaseReference db = FirebaseDatabase.getInstance("https://hcmut-smart-home-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    private RoomListener roomListener;
    private CompoundButton.OnCheckedChangeListener ledListener;
    private CompoundButton.OnCheckedChangeListener fanListener;
    private TextView tvTempVal, tvLightIntVal;
    private ArrayAdapter<String> adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ledListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                roomListener.onLedChange(isChecked?1:0);
            }
        };
        fanListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < rgFan.getChildCount(); i++) {
                    rgFan.getChildAt(i).setEnabled(isChecked);
                }
                if (isChecked) {
                    rbLow.setChecked(true);
                    roomListener.onFanChange(TEMP_LOW);
                }
                if (!isChecked) {
                    rgFan.clearCheck();
                    roomListener.onFanChange(TEMP_XLOW);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, container, false);

        switchLed = view.findViewById(R.id.switchLed);
        switchLed.setOnCheckedChangeListener(ledListener);
        switchFan = view.findViewById(R.id.switchFan);
        rgFan = view.findViewById(R.id.rgFan);
        rbLow = view.findViewById(R.id.rbLow);
        rbMedium = view.findViewById(R.id.rbMedium);
        rbHigh = view.findViewById(R.id.rbHigh);
        // event
        switchFan.setOnCheckedChangeListener(fanListener);
        // radio button
        rbLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomListener.onFanChange(TEMP_LOW);
            }
        });
        rbMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomListener.onFanChange(TEMP_MEDIUM);
            }
        });
        rbHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomListener.onFanChange(TEMP_HIGH);
            }
        });


        // get text view of temperature value and light intensity value
        tvTempVal = view.findViewById(R.id.tvTemperature);
        tvLightIntVal = view.findViewById(R.id.tvLightIntVal);

        loadDeviceState(LED);
        loadDeviceState(FAN);
        loadSensorData(TEMP_HUMID);
        loadSensorData(LIGHT);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RoomListener) {
            roomListener = (RoomListener) context;
        }
    }

    private void loadDeviceState(String device) {
        db.collection("States")
          .whereEqualTo("device", device)
          .orderBy("time", Query.Direction.DESCENDING)
          .addSnapshotListener(new EventListener<QuerySnapshot>() {
              @Override
              public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                  if (querySnapshot == null || querySnapshot.isEmpty()) return;
                  DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                  StateItem item = document.toObject(StateItem.class);
                  int state = item.getState();

                  if (state == LIGHT_LOW || state == LIGHT_HIGH) {
                      switchLed.setOnCheckedChangeListener(null);
                      switchLed.setChecked(state == LIGHT_LOW);
                      switchLed.setOnCheckedChangeListener(ledListener);
                  }
                  else if (state == TEMP_XLOW) {
                      switchFan.setOnCheckedChangeListener(null);
                      switchFan.setChecked(false);
                      switchFan.setOnCheckedChangeListener(fanListener);
                      rgFan.clearCheck();
                      for (int i = 0; i < rgFan.getChildCount(); i++) {
                          rgFan.getChildAt(i).setEnabled(false);
                      }
                  } else {
                      switchFan.setOnCheckedChangeListener(null);
                      switchFan.setChecked(true);
                      switchFan.setOnCheckedChangeListener(fanListener);
                      switch (state) {
                          case TEMP_LOW:
                              rbLow.setChecked(true);
                              break;
                          case TEMP_MEDIUM:
                              rbMedium.setChecked(true);
                              break;
                          case TEMP_HIGH:
                              rbHigh.setChecked(true);
                              break;
                      }
                  }
              }
          });
    }


    private void loadSensorData(String sensor) {
        db.collection("Records")
                .whereEqualTo("device", sensor)
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) return;
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        if (sensor.equals(LIGHT)) {
                            tvLightIntVal.setText(document.get("value") + "  lux");
                        }
                        else if (sensor.equals(TEMP_HUMID)) {
                            String data = document.get("value").toString().replace("\0","");

                            String[] value = data.split("-");
                            String temp = value[0];
                            String humid = value[1];
                            tvTempVal.setText(temp + " ˚C");
                        }
                    }
                });
    }
}