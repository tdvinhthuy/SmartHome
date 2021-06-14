package com.example.smarthome.Fragment;

import android.annotation.SuppressLint;
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
import com.example.smarthome.R;
import com.example.smarthome.Utils.MQTTService;
import com.example.smarthome.Utils.Room;
import com.example.smarthome.Utils.RoomListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlFragment extends Fragment /*implements AdapterView.OnItemSelectedListener*/ {
    //private Room room;
    private Spinner spinner;
    private RadioGroup rgFan;
    private RadioButton rbLow;
    private RadioButton rbMedium;
    private RadioButton rbHigh;
    private SwitchCompat switchFan;
    private SwitchCompat switchLight;
    private FirebaseFirestore db;
    private RoomListener roomListener;
    private CompoundButton.OnCheckedChangeListener lightListener;
    private CompoundButton.OnCheckedChangeListener fanListener;
    private ArrayAdapter<String> adapter;
//    public ControlFragment() {
//        room = null;
//    }
//    public ControlFragment(Room room) {
//        this.room = room;
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        lightListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                roomListener.onLightChange(isChecked?1:0, false);
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
                    roomListener.onFanChange(1, false);
                }
                if (!isChecked) {
                    rgFan.clearCheck();
                    roomListener.onFanChange(0, false);
                }
            }
        };
        // load data
//        if (room != null) {
//            loadRoom();
//            loadLight();
//            loadFan();
//        }
        loadLight();
        loadFan();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        // spinner
        //spinner = view.findViewById(R.id.spinnerRoomControl);
        //spinner.setOnItemSelectedListener(this);
        // light
        switchLight = view.findViewById(R.id.switchLight);
        //if (room == null) switchLight.setEnabled(false);
        switchLight.setOnCheckedChangeListener(lightListener);
        // fan
        switchFan = view.findViewById(R.id.switchFan);
        rgFan = view.findViewById(R.id.rgFan);
//        if (room == null) {
//            switchFan.setEnabled(false);
//            for (int i = 0; i < rgFan.getChildCount(); i++) {
//                rgFan.getChildAt(i).setEnabled(false);
//            }
//        }
        // get radio button
        rbLow = view.findViewById(R.id.rbLow);
        rbMedium = view.findViewById(R.id.rbMedium);
        rbHigh = view.findViewById(R.id.rbHigh);
        // event
        switchFan.setOnCheckedChangeListener(fanListener);
        // radio button
        rbLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomListener.onFanChange(85, false);
            }
        });
        rbMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomListener.onFanChange(170, false);
            }
        });
        rbHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomListener.onFanChange(255, false);
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RoomListener) {
            roomListener = (RoomListener) context;
        }
    }

    private void loadLight() {
        db.collection("light_states")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) {
                            return;
                        }
                        for (DocumentSnapshot document: querySnapshot.getDocuments()) {
                            //if (document.getString("room_name").equals(room.getName())) {
                                String state = document.getString("state");
                                switchLight.setOnCheckedChangeListener(null);
                                switchLight.setChecked(state.equals("ON"));
                                switchLight.setOnCheckedChangeListener(lightListener);
                                Log.d("LIGHT_STATE", "Load light state success:"
                                        + /*room.getName() + */" " + state);
                                return;
                            //}
                        }
                    }
                });
    }

    private void loadFan() {
        db.collection("fan_states")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                if (querySnapshot == null || querySnapshot.isEmpty()) {
                    return;
                }
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    //if (document.getString("room_name").equals(room.getName())) {
                        String state = document.getString("state");
                        if (state.equals("OFF")) {
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
                                case "LOW":
                                    rbLow.setChecked(true);
                                    break;
                                case "MEDIUM":
                                    rbMedium.setChecked(true);
                                    break;
                                case "HIGH":
                                    rbHigh.setChecked(true);
                                    break;
                            }
                        }
                        Log.d("FAN_STATE", "Load fan state success: " /*+ room.getName()*/ + " " + state);
                        return;
                    //}
                }
            }
        });
    }

    /*
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
                        adapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout.support_simple_spinner_dropdown_item,
                                roomNames);
                        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String roomName = parent.getItemAtPosition(position).toString();
        if (!room.getName().equals(roomName)) {
            roomListener.onRoomChange(
                    new Room(room.getType(),
                            parent.getItemAtPosition(position).toString()),
                    true
            );
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

     */
}