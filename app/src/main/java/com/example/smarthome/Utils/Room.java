package com.example.smarthome.Utils;

public class Room {
    public enum RoomType {LIVING_ROOM, BEDROOM, KITCHEN, BATHROOM}
    private RoomType type;

    public Room() {
        type = null;
    }

    public Room(RoomType type) {
        this.type = type;
    }

    public RoomType getType() {
        return type;
    }
}
