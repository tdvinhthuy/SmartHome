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

public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener {
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
    // data
    private final FirebaseFirestore db;
    private final Room room;
    private RoomListener roomListener;
    public HomeFragment() {
        room = null;
        db = FirebaseFirestore.getInstance();
    }
    public HomeFragment(Room room) {
        this.room = room;
        db = FirebaseFirestore.getInstance();
        // load data
        if (room != null) loadRoom();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // load data
        if (room != null) loadRoom();
        // SCREEN
        layoutHome = view.findViewById(R.id.layoutHome);
        layoutRoom = view.findViewById(R.id.layoutRoom);
        layoutHome.setVisibility(room == null? View.VISIBLE: View.GONE);
        layoutRoom.setVisibility(room != null? View.VISIBLE: View.GONE);
        // HOME
        imgLivingRoom = view.findViewById(R.id.imgLivingRoom);
        imgBedroom = view.findViewById(R.id.imgBedroom);
        imgKitchen = view.findViewById(R.id.imgKitchen);
        imgBathroom = view.findViewById(R.id.imgBathroom);
        // IMAGE ON CLICK
        imgLivingRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room room = new Room(Room.RoomType.LIVING_ROOM);
                roomListener.onChange(room);
            }
        });
        imgBedroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room room = new Room(Room.RoomType.BEDROOM);
                roomListener.onChange(room);
            }
        });
        imgKitchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room room = new Room(Room.RoomType.KITCHEN);
                roomListener.onChange(room);
            }
        });
        imgBathroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room room = new Room(Room.RoomType.BATHROOM);
                roomListener.onChange(room);
            }
        });
        // ROOM
        tvTemperatureRoom = view.findViewById(R.id.tvTemperatureRoom);
        tvLightIntensityRoom = view.findViewById(R.id.tvLightIntensityRoom);
        tvHumidityRoom = view.findViewById(R.id.tvHumidityRoom);
        imgBackHome = view.findViewById(R.id.imgBackHome);
        imgBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomListener.onChange(null);
            }
        });
        // spinner
        spinner = view.findViewById(R.id.spinnerRoom);
        spinner.setOnItemSelectedListener(this);

        return view;
    }

    private void loadRoom() {
        db.collection("rooms").whereEqualTo("type", room.getStringType())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException error) {

                        // get room names
                        List<String> roomNames = new ArrayList<String>();
                        for (DocumentSnapshot document: querySnapshot.getDocuments()) {
                            roomNames.add(document.getString("name"));
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        roomListener.onChange(
//                new Room(room.getType(),
//                        parent.getItemAtPosition(position).toString())
//        );
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}