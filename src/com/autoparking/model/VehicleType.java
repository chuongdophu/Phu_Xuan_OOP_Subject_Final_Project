package com.autoparking.model;

public enum VehicleType {
    CAR("Car"),
    MOTORBIKE("Motorbike"),
    TRUCK("Truck"),
    EV("Electric Vehicle"),
    BUS("Bus Passenger");

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}