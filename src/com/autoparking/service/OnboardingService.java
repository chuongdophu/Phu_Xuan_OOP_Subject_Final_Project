package com.autoparking.service;

import com.autoparking.config.ParkingConfig;
import com.autoparking.model.VehicleType;
import java.io.*;
import java.util.*;

public class OnboardingService {
    private static final String CONFIG_FILE = "config.txt";

    public static ParkingConfig getOrSetupConfig(Scanner scanner) {
        ParkingConfig existingConfig = loadConfigFromFile();
        if (existingConfig != null) {
            System.out.println("==================================================");
            System.out.println("   [SYSTEM] Loaded existing configuration from 'config.txt'");
            System.out.println("   Parking Lot: " + existingConfig.getLotName());
            System.out.println("   Capacity   : " + existingConfig.getTotalCapacity() + " slots");
            System.out.println("==================================================");
            return existingConfig;
        }

        ParkingConfig newConfig = setupParkingLot(scanner);
        saveConfigToFile(newConfig);
        return newConfig;
    }

    private static ParkingConfig setupParkingLot(Scanner scanner) {
        System.out.println("==================================================");
        System.out.println("   SYSTEM ONBOARDING & ENTERPRISE INITIAL SETUP   ");
        System.out.println("==================================================");

        String lotName;
        while (true) {
            System.out.print("1. Enter Parking Lot / Business Name: ");
            lotName = scanner.nextLine().trim();
            if (!lotName.isEmpty())
                break;
            System.out.println("[ERROR] Invalid format for 'Business Name'! Value cannot be empty. Please try again.");
        }

        int floors = readPositiveInt(scanner, "2. Enter total number of floors: ", "Total Number of Floors");
        int slotsPerFloor = readPositiveInt(scanner, "3. Enter number of slots per floor: ", "Slots Per Floor");

        boolean isTypeRestricted = readBooleanYN(scanner,
                "4. Enable vehicle type segregation per floor? (Y: Specific types per floor / N: Any type anywhere): ",
                "Vehicle Type Segregation Option");

        ParkingConfig config = new ParkingConfig(lotName, floors, slotsPerFloor, isTypeRestricted);

        Set<VehicleType> activeVehicleTypes = new HashSet<>();

        if (isTypeRestricted) {
            System.out.println("\n--- FLOOR-LEVEL VEHICLE ALLOCATION SETUP ---");
            VehicleType[] types = VehicleType.values();
            for (int f = 1; f <= floors; f++) {
                System.out.println("\n[Setting up Floor " + f + "]");
                Set<VehicleType> allowedTypes = new HashSet<>();

                for (VehicleType type : types) {
                    boolean allow = readBooleanYN(scanner,
                            "  -> Allow " + type.getDescription() + " on Floor " + f + "? (Y/N): ",
                            "Floor " + f + " Allocation for " + type.getDescription());
                    if (allow) {
                        allowedTypes.add(type);
                        activeVehicleTypes.add(type); // Lưu lại loại xe được cho phép
                    }
                }

                if (allowedTypes.isEmpty()) {
                    System.out.println("  [WARNING] No vehicle types allowed for Floor " + f
                            + ". No vehicles will be able to park here!");
                }
                config.setFloorAllowedTypes(f, allowedTypes);
            }
        } else {
            activeVehicleTypes.addAll(Arrays.asList(VehicleType.values()));
        }

        System.out.println("\n--- HOURLY RATE SETUP FOR VEHICLE TYPES (VND/hour) ---");

        for (VehicleType type : VehicleType.values()) {
            if (activeVehicleTypes.contains(type)) {
                double rate = readPositiveDouble(scanner,
                        "Enter hourly rate for " + type.getDescription() + " (VND): ",
                        "Hourly Rate for " + type.getDescription());
                config.setRate(type, rate);
            } else {

                System.out.println(
                        "[INFO] Skipped rate setup for " + type.getDescription() + " (Not allowed in any floor)");
                config.setRate(type, 0.0);
            }
        }

        System.out.println("\n[SUCCESS] Setup completed! Total capacity: "
                + config.getTotalCapacity() + " slots.");
        return config;
    }

    private static void saveConfigToFile(ParkingConfig config) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            writer.println("LOT_NAME=" + config.getLotName());
            writer.println("TOTAL_FLOORS=" + config.getTotalFloors());
            writer.println("SLOTS_PER_FLOOR=" + config.getSlotsPerFloor());
            writer.println("IS_TYPE_RESTRICTED=" + config.isTypeRestricted());

            for (Map.Entry<VehicleType, Double> entry : config.getAllRates().entrySet()) {
                writer.println("RATE_" + entry.getKey().name() + "=" + entry.getValue());
            }

            if (config.isTypeRestricted()) {
                for (Map.Entry<Integer, Set<VehicleType>> entry : config.getFloorAllowedTypes().entrySet()) {
                    StringBuilder typesStr = new StringBuilder();
                    for (VehicleType vt : entry.getValue()) {
                        if (typesStr.length() > 0)
                            typesStr.append(",");
                        typesStr.append(vt.name());
                    }
                    writer.println("FLOOR_ALLOWED_" + entry.getKey() + "=" + typesStr.toString());
                }
            }
            System.out.println("[SYSTEM] Configuration saved to 'config.txt' successfully.");
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save 'config.txt': " + e.getMessage());
        }
    }

    private static ParkingConfig loadConfigFromFile() {
        File file = new File(CONFIG_FILE);
        if (!file.exists())
            return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Map<String, String> props = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    props.put(parts[0].trim(), parts[1].trim());
                }
            }

            String lotName = props.getOrDefault("LOT_NAME", "AutoParking");
            int floors = Integer.parseInt(props.getOrDefault("TOTAL_FLOORS", "1"));
            int slots = Integer.parseInt(props.getOrDefault("SLOTS_PER_FLOOR", "10"));
            boolean isRestricted = Boolean.parseBoolean(props.getOrDefault("IS_TYPE_RESTRICTED", "false"));

            ParkingConfig config = new ParkingConfig(lotName, floors, slots, isRestricted);

            for (VehicleType type : VehicleType.values()) {
                String rateKey = "RATE_" + type.name();
                if (props.containsKey(rateKey)) {
                    config.setRate(type, Double.parseDouble(props.get(rateKey)));
                }
            }

            if (isRestricted) {
                for (int f = 1; f <= floors; f++) {
                    String floorKey = "FLOOR_ALLOWED_" + f;
                    if (props.containsKey(floorKey)) {
                        Set<VehicleType> allowed = new HashSet<>();
                        String[] typeNames = props.get(floorKey).split(",");
                        for (String tName : typeNames) {
                            if (!tName.trim().isEmpty()) {
                                try {
                                    allowed.add(VehicleType.valueOf(tName.trim()));
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        }
                        config.setFloorAllowedTypes(f, allowed);
                    }
                }
            }
            return config;
        } catch (Exception e) {
            System.out.println("[WARNING] Could not read 'config.txt'. Fallback to Setup process.");
            return null;
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value > 0)
                    return value;
                System.out.println("[ERROR] Invalid value for '" + fieldName
                        + "'! Number must be greater than 0. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid format for '" + fieldName + "'! Expecting a number, but got '"
                        + input + "'. Please try again.");
            }
        }
    }

    private static double readPositiveDouble(Scanner scanner, String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= 0)
                    return value;
                System.out.println(
                        "[ERROR] Invalid value for '" + fieldName + "'! Price cannot be negative. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid format for '" + fieldName + "'! Expecting a valid number, but got '"
                        + input + "'. Please try again.");
            }
        }
    }

    private static boolean readBooleanYN(Scanner scanner, String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y"))
                return true;
            if (input.equals("N"))
                return false;
            System.out.println("[ERROR] Invalid format for '" + fieldName + "'! Expecting 'Y' or 'N', but got '" + input
                    + "'. Please try again.");
        }
    }
}