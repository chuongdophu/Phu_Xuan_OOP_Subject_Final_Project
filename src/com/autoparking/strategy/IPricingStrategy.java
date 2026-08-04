package com.autoparking.strategy;

import java.time.LocalDateTime;

public interface IPricingStrategy {
    double calculateFee(LocalDateTime timeIn, LocalDateTime timeOut);
}