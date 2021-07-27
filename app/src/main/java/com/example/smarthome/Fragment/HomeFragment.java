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
import com.example.smarthome.Utils.RecordItem;
import com.example.smarthome.Utils.RoomListener;
import com.example.smarthome.R;
import com.example.smarthome.Utils.Room;
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
    private View layoutHome, layoutRoom;
    // Room
    private TextView tvTemperatureRoom;
    private TextView tvLightIntensityRoom;
    private TextView tvHumidityRoom;
    private ImageView imgBackHome;
    private Spinner spinner;
    private Room room;
    // data
    final DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    private RoomListener roomListener;
//    public HomeFragment() {
//        room = null;
//    }
//    public HomeFragment(Room room) {
//        this.room = room;
//        // load data
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        layoutRoom = view.findViewById(R.id.layoutRoom);

        tvTemperatureRoom = view.findViewById(R.id.tvTemperatureRoom);
        tvLightIntensityRoom = view.findViewById(R.id.tvLightIntensityRoom);
        tvHumidityRoom = view.findViewById(R.id.tvHumidityRoom);

        loadData();
        return view;
    }

    private void loadData() {
        // Light intensity
        db.child("Records").child(LIGHT).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot == null) return;
                RecordItem item = snapshot.getValue(RecordItem.class);
                String data = item.getValue();
                tvLightIntensityRoom.setText(String.format("%s lux", data));
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
        // Temp humid
        db.child("Records").child(TEMP_HUMID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot == null) return;
                RecordItem item = snapshot.getValue(RecordItem.class);
                String[] data = item.getValue().split("-");
                int temp = Integer.parseInt(data[0]);
                int humid = Integer.parseInt(data[1]);
                tvTemperatureRoom.setText(String.format("%d ˚C", temp));
                tvHumidityRoom.setText(String.format("%.2f %", humid/100));
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