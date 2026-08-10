package com.autoparking.model;

public class ParkingSlot {
    private final String slotId;
    private final int floor;
    private final int slotNumber;
    private boolean occupied;
    private String licensePlate;
    private String ticketId;

    public ParkingSlot(String slotId, int floor, int slotNumber) {
        this.slotId = slotId;
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.occupied = false;
    }

    public String getSlotId() {
        return slotId;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void occupy(String licensePlate, String ticketId) {
        this.occupied = true;
        this.licensePlate = licensePlate;
        this.ticketId = ticketId;
    }

    public void release() {
        this.occupied = false;
        this.licensePlate = null;
        this.ticketId = null;
    }
}
