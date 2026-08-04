package com.autoparking.model;

public class ParkingSlot {
    private int floor;
    private int slotNumber;
    private boolean isOccupied;
    private VehicleType currentVehicleType;
    private String currentLicensePlate;

    public ParkingSlot(int floor, int slotNumber) {
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.isOccupied = false;
    }

    public void parkVehicle(String licensePlate, VehicleType type) {
        this.isOccupied = true;
        this.currentLicensePlate = licensePlate;
        this.currentVehicleType = type;
    }

    public void freeSlot() {
        this.isOccupied = false;
        this.currentLicensePlate = null;
        this.currentVehicleType = null;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getPosition() {
        return "F" + floor + "-S" + slotNumber;
    }
}