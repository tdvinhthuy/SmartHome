package com.example.smarthome.Utils;

import com.example.smarthome.Utils.Room;

public interface RoomListener {
    //void onRoomChange(Room room, boolean control);
    void onLightChange(int state, boolean bAuto);
    void onFanChange(int state, boolean bAuto);
    void onLightDataArrived(float data);
    void onTemperatureDataArrived(float data);
}
