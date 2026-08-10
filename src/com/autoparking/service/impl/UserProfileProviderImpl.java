package com.autoparking.service.impl;

import com.autoparking.config.MultiLotConfig;
import com.autoparking.core.interfaces.IUserProfileProvider;
import com.autoparking.model.UserProfile;
import com.autoparking.model.VehicleType;

import java.io.*;
import java.util.*;

public class UserProfileProviderImpl implements IUserProfileProvider {

    @Override
    public void saveProfile(UserProfile profile) {
        File dir = new File(MultiLotConfig.DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(MultiLotConfig.USER_PROFILE_FILE);
        if (file.exists()) {
            file.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            Properties props = new Properties();
            props.setProperty("enterpriseName", profile.getEnterpriseName());
            props.setProperty("totalFloors", String.valueOf(profile.getTotalFloors()));
            props.setProperty("isLargeScale", String.valueOf(profile.isLargeScale()));
            props.setProperty("hasOvernightFee", String.valueOf(profile.hasOvernightFee()));

            StringBuilder slotsConfig = new StringBuilder();
            profile.getFloorSlotsMap()
                    .forEach((floor, slots) -> slotsConfig.append(floor).append(":").append(slots).append(";"));
            props.setProperty("floorSlots", slotsConfig.toString());

            StringBuilder supported = new StringBuilder();
            for (VehicleType type : profile.getSupportedVehicles()) {
                if (supported.length() > 0) {
                    supported.append(",");
                }
                supported.append(type.name());
            }
            props.setProperty("supportedVehicles", supported.toString());

            StringBuilder hourlyRateConfig = new StringBuilder();
            profile.getHourlyRates()
                    .forEach((type, rate) -> hourlyRateConfig.append(type.name()).append("=").append(rate).append(";"));
            props.setProperty("hourlyRates", hourlyRateConfig.toString());

            StringBuilder overnightConfig = new StringBuilder();
            profile.getOvernightFees()
                    .forEach((type, fee) -> overnightConfig.append(type.name()).append("=").append(fee).append(";"));
            props.setProperty("overnightFees", overnightConfig.toString());

            props.store(fos, "Parking profile");
            System.out.println("[SUCCESS] Cap nhat cau hinh bai xe thanh cong!");
        } catch (Exception e) {
            System.out.println("[FILE ERROR] Loi khi cap nhat UserProfile: " + e.getMessage());
        }
    }

    @Override
    public UserProfile loadProfile() {
        File file = new File(MultiLotConfig.USER_PROFILE_FILE);
        if (!file.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);

            String enterpriseName = props.getProperty("enterpriseName");
            if (enterpriseName == null || enterpriseName.isBlank()) {
                return null;
            }

            int totalFloors = Integer.parseInt(props.getProperty("totalFloors", "1"));
            String slotsConfigStr = props.getProperty("floorSlots", "");
            boolean isLargeScale = Boolean.parseBoolean(props.getProperty("isLargeScale", "false"));
            boolean hasOvernightFee = Boolean.parseBoolean(props.getProperty("hasOvernightFee", "false"));

            Map<Integer, Integer> floorSlotsMap = new HashMap<>();
            if (slotsConfigStr != null && !slotsConfigStr.isBlank() && slotsConfigStr.contains(":")) {
                String[] entries = slotsConfigStr.split(";");
                for (String entry : entries) {
                    if (!entry.trim().isEmpty()) {
                        String[] pair = entry.split(":");
                        floorSlotsMap.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
                    }
                }
            } else {
                int defaultSlots = 10;
                try {
                    defaultSlots = Integer.parseInt(slotsConfigStr);
                } catch (Exception ignored) {
                }
                for (int f = 1; f <= totalFloors; f++) {
                    floorSlotsMap.put(f, defaultSlots);
                }
            }

            Set<VehicleType> supportedVehicles = new HashSet<>();
            String supportedVehiclesLine = props.getProperty("supportedVehicles", "CAR");
            if (supportedVehiclesLine != null && !supportedVehiclesLine.isBlank()) {
                String[] parts = supportedVehiclesLine.split(",");
                for (String part : parts) {
                    try {
                        supportedVehicles.add(VehicleType.valueOf(part.trim().toUpperCase(Locale.ROOT)));
                    } catch (Exception ignored) {
                    }
                }
            } else {
                supportedVehicles.add(VehicleType.CAR);
            }

            Map<VehicleType, List<Integer>> vehicleZones = new HashMap<>();
            Map<VehicleType, Double> hourlyRates = new HashMap<>();
            Map<VehicleType, Double> overnightFees = new HashMap<>();

            for (VehicleType type : supportedVehicles) {
                vehicleZones.put(type, Collections.singletonList(1));
                hourlyRates.put(type, MultiLotConfig.DEFAULT_BASE_RATE);
                overnightFees.put(type, 50000.0);
            }

            String rateConfig = props.getProperty("hourlyRates", "");
            if (!rateConfig.isBlank()) {
                String[] entries = rateConfig.split(";");
                for (String entry : entries) {
                    if (entry.isBlank()) {
                        continue;
                    }
                    String[] pair = entry.split("=");
                    if (pair.length == 2) {
                        try {
                            hourlyRates.put(VehicleType.valueOf(pair[0].trim().toUpperCase(Locale.ROOT)),
                                    Double.parseDouble(pair[1].trim()));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            String overnightConfig = props.getProperty("overnightFees", "");
            if (!overnightConfig.isBlank()) {
                String[] entries = overnightConfig.split(";");
                for (String entry : entries) {
                    if (entry.isBlank()) {
                        continue;
                    }
                    String[] pair = entry.split("=");
                    if (pair.length == 2) {
                        try {
                            overnightFees.put(VehicleType.valueOf(pair[0].trim().toUpperCase(Locale.ROOT)),
                                    Double.parseDouble(pair[1].trim()));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            List<String> features = isLargeScale
                    ? Arrays.asList("CHECK_IN", "CHECK_OUT", "VIEW_MATRIX", "EXPORT_REPORT", "MANAGE_ZONES")
                    : Arrays.asList("CHECK_IN", "CHECK_OUT", "VIEW_MATRIX");

            return new UserProfile(enterpriseName, totalFloors, floorSlotsMap, isLargeScale,
                    features, supportedVehicles, vehicleZones, hourlyRates, hasOvernightFee, overnightFees);

        } catch (Exception e) {
            System.out.println("[FILE ERROR] Failed to load UserProfile: " + e.getMessage());
            return null;
        }
    }
}