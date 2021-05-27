package com.example.smarthome.Utils;

import com.google.firebase.firestore.FieldValue;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class NotificationItem {
    public enum NotificationType {
        LIGHT_ON,
        LIGHT_OFF,
        FAN_OFF,
        FAN_LOW,
        FAN_MEDIUM,
        FAN_HIGH
    }
    private String room_name;
    private String type;
    private Date timestamp;
    private String userID;

    public NotificationItem() {
        this.room_name = "";
        this.type = "";
        this.timestamp = null;
        this.userID = "";
    }
    public NotificationItem(String room_name, String type, Date timestamp, String userID) {
        this.type = type;
        this.room_name = room_name;
        this.timestamp = timestamp;
        this.userID = userID;
    }

    public String getRoom_name() {
        return room_name;
    }

    public String getType() {
        return type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public NotificationType getNotificationType() {
        return NotificationType.valueOf(type);
    }

    public String getContent() {
        String[] metadata;
        metadata = type.split("_");
        return (metadata[0].equals("FAN")?"Your fan ":"Your light ")
        + "at " + room_name + " "
        + "is " + (userID.equals("")?"automatically turned ":"turned ")
        + (metadata[1].equals("OFF")?"off":"on")
        + (metadata[1].equals("ON")?"":"at mode " + metadata[1])
        + (userID.equals("")?"!":" by mobile app!");
    }

    public String getStringTime() {
        String[] months = {
                "Jan", "Feb", "Mar", "Apr",
                "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        return cal.get(Calendar.HOUR_OF_DAY) + ":"
                + cal.get(Calendar.MINUTE) + ", "
                + months[cal.get(Calendar.MONTH)] + " "
                + cal.get(Calendar.DAY_OF_MONTH) + ", "
                + cal.get(Calendar.YEAR);
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
