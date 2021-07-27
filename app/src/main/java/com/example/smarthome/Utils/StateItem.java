package com.example.smarthome.Utils;

import java.util.Date;

public class StateItem {
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

    private int state;

    public StateItem() {
        this.state = -1;
    }

    public StateItem(int state) {
        this.state = state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

}