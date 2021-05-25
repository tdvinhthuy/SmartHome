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
import com.example.smarthome.R;
import com.example.smarthome.Utils.Room;
import com.example.smarthome.Utils.RoomListener;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ControlFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private Room room;
    private Spinner spinner;
    private RadioGroup rgFan;
    private RadioButton rbLow;
    private SwitchCompat switchFan;
    private final FirebaseFirestore db;
    private RoomListener roomListener;

    public ControlFragment() {
        room = null;
        db = FirebaseFirestore.getInstance();
    }
    public ControlFragment(Room room) {
        this.room = room;
        db = FirebaseFirestore.getInstance();
        // load data
        if (room != null) loadRoom();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        // spinner
        spinner = view.findViewById(R.id.spinnerRoomControl);
        spinner.setOnItemSelectedListener(this);
        // fan
        switchFan = view.findViewById(R.id.switchFan);
        rgFan = view.findViewById(R.id.rgFan);
        rbLow = view.findViewById(R.id.rbLow);
        switchFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < rgFan.getChildCount(); i++) {
                    rgFan.getChildAt(i).setEnabled(isChecked);
                }
                if (isChecked) rgFan.check(rbLow.getId());
                else rgFan.clearCheck();
            }
        });
        // default
        for (int i = 0; i < rgFan.getChildCount(); i++) {
            rgFan.getChildAt(i).setEnabled(false);
        }

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RoomListener) {
            roomListener = (RoomListener) context;
        }
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