package com.autoparking.strategy;

import java.time.Duration;
import java.time.LocalDateTime;

public class HourlyPricingStrategy implements IPricingStrategy {
    private final double hourlyRate;

    public HourlyPricingStrategy(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    @Override
    public double calculateFee(LocalDateTime timeIn, LocalDateTime timeOut) {
        if (timeIn == null || timeOut == null)
            return 0.0;

        long minutes = Duration.between(timeIn, timeOut).toMinutes();
        long hours = (long) Math.ceil((double) minutes / 60.0);
        if (hours == 0)
            hours = 1;

        return hours * hourlyRate;
    }
}