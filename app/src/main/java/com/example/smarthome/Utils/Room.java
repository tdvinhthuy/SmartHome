package com.example.smarthome.Utils;

import java.util.Locale;

public class Room {
    public enum RoomType {LIVING_ROOM, BEDROOM, KITCHEN, BATHROOM}
    private RoomType type;
    private String name;

    public Room() {
        type = null;
        name = "";
    }

    public Room(RoomType type) {
        this.type = type;
        this.name = "";
    }

    public Room(RoomType type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getStringType() {
        return type.toString()
                .replace("_", " ")
                .toLowerCase(Locale.ROOT);
    }

    public RoomType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
