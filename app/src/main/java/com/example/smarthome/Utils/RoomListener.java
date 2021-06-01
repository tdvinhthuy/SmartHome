package com.example.smarthome.Utils;

import com.example.smarthome.Utils.Room;

public interface RoomListener {
    //void onRoomChange(Room room, boolean control);
    void onLightChange(int state);
    void onFanChange(int state);
    void onLightDataArrived(float data);
    void onTemperatureDataArrived(float data);
}
