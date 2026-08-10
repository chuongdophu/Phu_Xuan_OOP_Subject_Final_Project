package com.autoparking.core.interfaces;

import com.autoparking.model.VehicleType;
import java.time.LocalDateTime;

public interface IPricingCalculator {
    double calculateFee(VehicleType vehicleType, LocalDateTime timeIn, LocalDateTime timeOut, double baseRate);
}