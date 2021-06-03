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
import com.example.smarthome.Utils.RoomListener;
import com.example.smarthome.R;
import com.example.smarthome.Utils.Room;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment /*implements AdapterView.OnItemSelectedListener*/ {
    private View layoutHome, layoutRoom;
    // Home
    private ImageView imgLivingRoom;
    private ImageView imgBedroom;
    private ImageView imgKitchen;
    private ImageView imgBathroom;
    // Room
    private TextView tvTemperatureRoom;
    private TextView tvLightIntensityRoom;
    private TextView tvHumidityRoom;
    private ImageView imgBackHome;
    private Spinner spinner;
    private Room room;
    // data
    private FirebaseFirestore db;
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
        db = FirebaseFirestore.getInstance();
//        if (room != null) loadRoom();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // SCREEN
        //layoutHome = view.findViewById(R.id.layoutHome);
        layoutRoom = view.findViewById(R.id.layoutRoom);
        //layoutHome.setVisibility(room == null? View.VISIBLE: View.GONE);
        //layoutRoom.setVisibility(room != null? View.VISIBLE: View.GONE);
        // HOME
        /*
        imgLivingRoom = view.findViewById(R.id.imgLivingRoom);
        imgBedroom = view.findViewById(R.id.imgBedroom);
        imgKitchen = view.findViewById(R.id.imgKitchen);
        imgBathroom = view.findViewById(R.id.imgBathroom);
        // IMAGE ON CLICK
        imgLivingRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room room = new Room(Room.RoomType.LIVING_ROOM);
                roomListener.onRoomChange(room, false);
            }
        });
        imgBedroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room room = new Room(Room.RoomType.BEDROOM);
                roomListener.onRoomChange(room, false);
            }
        });
        imgKitchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room room = new Room(Room.RoomType.KITCHEN);
                roomListener.onRoomChange(room, false);
            }
        });
        imgBathroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room room = new Room(Room.RoomType.BATHROOM);
                roomListener.onRoomChange(room, false);
            }
        });
         */
        // ROOM
        tvTemperatureRoom = view.findViewById(R.id.tvTemperatureRoom);
        tvLightIntensityRoom = view.findViewById(R.id.tvLightIntensityRoom);
        tvHumidityRoom = view.findViewById(R.id.tvHumidityRoom);
        /*
        imgBackHome = view.findViewById(R.id.imgBackHome);
        imgBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomListener.onRoomChange(null, false);
            }
        });
        */
        // spinner
//        spinner = view.findViewById(R.id.spinnerRoom);
//        spinner.setOnItemSelectedListener(this);

        loadData();
        return view;
    }

    private void loadRoom() {
        db.collection("rooms").whereEqualTo("type", room.getStringType())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException error) {

                        // get room names
                        List<String> roomNames = new ArrayList<>();
                        if (!room.getName().equals("")) roomNames.add(room.getName());
                        for (DocumentSnapshot document: querySnapshot.getDocuments()) {
                            if (!document.getString("name").equals(room.getName())) {
                                roomNames.add(document.getString("name"));
                            }
                        }
                        // create adapter for spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout.support_simple_spinner_dropdown_item,
                                roomNames);
                        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    }
                });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RoomListener) {
            roomListener = (RoomListener) context;
        }
    }


    private void loadData() {
        // Light intensity
        db.collection("light_records")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) return;
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        tvLightIntensityRoom.setText(String.format("%d lux", document.get("data")));
                    }
                });
        // Temp humid
        db.collection("temp_humid_records")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) return;
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        tvTemperatureRoom.setText(String.format("%d ˚C", document.getString("temp_data")));
                        tvHumidityRoom.setText(String.format("%.2f %", Float.parseFloat(document.getString("humid_data"))/100));
                    }
                });
    }

    /*
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String roomName = parent.getItemAtPosition(position).toString();
        if (!room.getName().equals(roomName)) {
            roomListener.onRoomChange(
                    new Room(room.getType(),
                            parent.getItemAtPosition(position).toString()),
                    false
            );
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    */
}