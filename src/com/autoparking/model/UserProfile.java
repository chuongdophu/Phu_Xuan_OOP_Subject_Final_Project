package com.autoparking.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserProfile {
    private final String enterpriseName;
    private final int totalFloors;
    private final Map<Integer, Integer> floorSlotsMap; // Key: So tang (1..N), Value: So slot tang do
    private final boolean isLargeScale;
    private final List<String> enabledFeatures;

    // Rules & Dynamic Pricing Attributes
    private final Set<VehicleType> supportedVehicles;
    private final Map<VehicleType, List<Integer>> vehicleZones;
    private final Map<VehicleType, Double> hourlyRates;
    private final boolean hasOvernightFee;
    private final Map<VehicleType, Double> overnightFees;

    public UserProfile(String enterpriseName, int totalFloors, Map<Integer, Integer> floorSlotsMap,
            boolean isLargeScale,
            List<String> enabledFeatures, Set<VehicleType> supportedVehicles,
            Map<VehicleType, List<Integer>> vehicleZones, Map<VehicleType, Double> hourlyRates,
            boolean hasOvernightFee, Map<VehicleType, Double> overnightFees) {
        this.enterpriseName = enterpriseName;
        this.totalFloors = totalFloors;
        this.floorSlotsMap = floorSlotsMap;
        this.isLargeScale = isLargeScale;
        this.enabledFeatures = enabledFeatures;
        this.supportedVehicles = supportedVehicles;
        this.vehicleZones = vehicleZones;
        this.hourlyRates = hourlyRates;
        this.hasOvernightFee = hasOvernightFee;
        this.overnightFees = overnightFees;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public Map<Integer, Integer> getFloorSlotsMap() {
        return floorSlotsMap;
    }

    public boolean isLargeScale() {
        return isLargeScale;
    }

    public List<String> getEnabledFeatures() {
        return enabledFeatures;
    }

    public Set<VehicleType> getSupportedVehicles() {
        return supportedVehicles;
    }

    public Map<VehicleType, List<Integer>> getVehicleZones() {
        return vehicleZones;
    }

    public Map<VehicleType, Double> getHourlyRates() {
        return hourlyRates;
    }

    public boolean hasOvernightFee() {
        return hasOvernightFee;
    }

    public Map<VehicleType, Double> getOvernightFees() {
        return overnightFees;
    }

    public int getTotalSlots() {
        return floorSlotsMap.values().stream().mapToInt(Integer::intValue).sum();
    }
}