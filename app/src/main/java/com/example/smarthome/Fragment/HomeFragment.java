package com.example.smarthome.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.smarthome.Utils.RoomListener;
import com.example.smarthome.R;
import com.example.smarthome.Utils.Room;

public class HomeFragment extends Fragment {
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
    // data
    private final Room room;
    private RoomListener roomListener;
    public HomeFragment() {
        room = null;
    }
    public HomeFragment(Room room) {
        this.room = room;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setup(view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RoomListener) {
            roomListener = (RoomListener) context;
        }
    }

    private void setup(View view) {
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
                Room livingRoom = new Room(Room.RoomType.LIVING_ROOM);
                roomListener.onChange(livingRoom);
            }
        });
        imgBedroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room bedroom = new Room(Room.RoomType.BEDROOM);
                roomListener.onChange(bedroom);
            }
        });
        imgKitchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room kitchen = new Room(Room.RoomType.KITCHEN);
                roomListener.onChange(kitchen);
            }
        });
        imgBathroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room bathroom = new Room(Room.RoomType.BATHROOM);
                roomListener.onChange(bathroom);
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
    }
}