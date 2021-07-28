package com.example.smarthome.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.smarthome.Activity.MainActivity;
import com.example.smarthome.Utils.RecordItem;
import com.example.smarthome.Utils.RoomListener;
import com.example.smarthome.R;
import com.example.smarthome.Utils.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment  {
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
    // Room
    private TextView tvTemperatureRoom;
    private TextView tvLightIntensityRoom;
    private TextView tvHumidityRoom;
    // data
    FirebaseFirestore db = FirebaseFirestore.getInstance();
//    final DatabaseReference db = FirebaseDatabase.getInstance("https://hcmut-smart-home-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvTemperatureRoom = view.findViewById(R.id.tvTemperatureRoom);
        tvLightIntensityRoom = view.findViewById(R.id.tvLightIntensityRoom);
        tvHumidityRoom = view.findViewById(R.id.tvHumidityRoom);

        loadData(LIGHT);
        loadData(TEMP_HUMID);
        return view;
    }

    private void loadData(String sensor) {
        db.collection("Records")
                .whereEqualTo("device", sensor)
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) return;
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        if (sensor.equals(LIGHT)) {
                            tvLightIntensityRoom.setText(document.get("value") + " lux");
                        }
                        else if (sensor.equals(TEMP_HUMID)) {
                            String data = document.get("value").toString().replace("\0","");

                            String[] value = data.split("-");
                            String temp = value[0];
                            String humid = value[1];
                            tvTemperatureRoom.setText(temp + " ˚C");
                            tvHumidityRoom.setText(humid + " %");
                        }
                    }
                });

        /*
        db.child("Records").child(sensor).orderByChild("time").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    RecordItem item = snapshot.getValue(RecordItem.class);
                    if (sensor.equals(LIGHT)) {
                        String value = item.getValue();
                        Log.e("QUERY", "light value=" + value.toString());
                        tvLightIntensityRoom.setText(String.format("%s lux", value));
                    }
                    else if (sensor.equals(TEMP_HUMID)) {
                        String[] value = item.getValue().split("-");
                        Log.e("QUERY", "temp value0=" + value[0].toString() + " value1=" + value[1].toString());
                        int temp = Integer.parseInt(value[0]);
                        int humid = Integer.parseInt(value[1]);
                        tvTemperatureRoom.setText(String.format("%d ˚C", temp));
                        tvHumidityRoom.setText(String.format("%.2f %", humid/100));
                    }
                }
                else {
                    Log.e("QUERY", "Load data failed");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); */
    }

}