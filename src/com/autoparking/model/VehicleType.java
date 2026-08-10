package com.autoparking.model;

public enum VehicleType {
    MOTORBIKE(1.0),
    CAR(2.0),
    TRUCK(3.0),
    EV(1.5),
    BUS(4.0);

    private final double multiplier;

    VehicleType(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}