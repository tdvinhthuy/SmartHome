package com.example.smarthome.Utils;

import java.util.Date;

public class StateItem {
    private String device;
    private int state;
    private Date time;

    public StateItem() {}

    public StateItem(String device, int state, Date time) {
        this.device = device;
        this.state = state;
        this.time = time;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}