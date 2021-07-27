package com.example.smarthome.Utils;

import com.example.smarthome.Utils.Room;

public interface RoomListener {
    void onLedChange(int state);
    void onFanChange(int state);
}
