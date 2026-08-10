package com.autoparking.service.impl;

import com.autoparking.core.interfaces.IPricingCalculator;
import com.autoparking.model.UserProfile;
import com.autoparking.model.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;

public class FlexiblePricingCalculatorImpl implements IPricingCalculator {
    private final UserProfile profile;

    // Constructor mặc định khi chưa có Profile (tránh lỗi xung đột)
    public FlexiblePricingCalculatorImpl() {
        this.profile = null;
    }

    // Constructor chính nhận Profile để tính Dynamic Fee
    public FlexiblePricingCalculatorImpl(UserProfile profile) {
        this.profile = profile;
    }

    @Override
    public double calculateFee(VehicleType vehicleType, LocalDateTime timeIn, LocalDateTime timeOut, double baseRate) {
        long minutes = Math.max(1, Duration.between(timeIn, timeOut).toMinutes());
        double hours = Math.ceil(minutes / 60.0);

        if (profile == null) {
            return hours * baseRate * vehicleType.getMultiplier();
        }

        // Lấy giá theo giờ của xe đã setup từ Onboarding
        double hourlyRate = profile.getHourlyRates().getOrDefault(vehicleType, baseRate);
        double total = hours * hourlyRate;

        // Tính phí qua đêm (nếu qua 00:00 và bãi có bật tính năng này)
        if (profile.hasOvernightFee() && timeIn.getDayOfYear() != timeOut.getDayOfYear()) {
            double overnightFee = profile.getOvernightFees().getOrDefault(vehicleType, 0.0);
            total += overnightFee;
        }

        return total;
    }
}