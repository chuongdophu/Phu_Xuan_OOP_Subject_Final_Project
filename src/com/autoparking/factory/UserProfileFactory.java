package com.autoparking.factory;

import com.autoparking.config.MultiLotConfig;
import com.autoparking.model.UserProfile;
import com.autoparking.model.VehicleType;
import com.autoparking.service.impl.UserProfileProviderImpl;

import java.util.*;

public class UserProfileFactory {

    public static UserProfile checkOrRunOnboarding(Scanner scanner, UserProfileProviderImpl profileProvider) {
        UserProfile existingProfile = profileProvider.loadProfile();

        if (existingProfile != null) {
            System.out.println("\n==================================================");
            System.out.println("  [DATA FOUND] Welcome back, " + existingProfile.getEnterpriseName().toUpperCase());
            System.out.println("  System loaded existing configuration automatically.");
            System.out.println("==================================================");
            return existingProfile;
        }

        UserProfile newProfile = runOnboarding(scanner);

        profileProvider.saveProfile(newProfile);
        return newProfile;
    }

    public static UserProfile runOnboarding(Scanner scanner) {
        System.out.println("\n==================================================");
        System.out.println("           DYNAMIC SYSTEM ONBOARDING              ");
        System.out.println("==================================================");

        System.out.print("Enter Enterprise Name: ");
        String name = scanner.nextLine().trim();

        System.out.println("\nSelect Scalability Level:");
        System.out.println("1. Small-scale (Single Floor, Basic Rates, No Overnight Fee)");
        System.out.println("2. Medium/Large-scale (Multi-floor, Zone Rules, Overnight Fees)");
        System.out.print("Your Choice (1/2): ");
        int choice = 1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception ignored) {
        }

        boolean isLarge = (choice == 2);
        int floors = 1;
        Map<Integer, Integer> floorSlotsMap = new HashMap<>();

        if (isLarge) {
            System.out.print("\n[CONFIG] Enter Total Floors (e.g. 3): ");
            try {
                floors = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception ignored) {
            }

            // HOI LOGIC SO SLOT MOI TANG CO GIONG NHAU KHONG
            System.out.print("[CONFIG] Do all floors have the SAME number of slots? (y/n): ");
            String sameSlotsOpt = scanner.nextLine().trim().toLowerCase();

            if (sameSlotsOpt.equals("y")) {
                // Setup 1 lan cho tat ca cac tang
                System.out.print("   - Enter total slots for ALL floors: ");
                int sameSlots = MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR;
                try {
                    sameSlots = Integer.parseInt(scanner.nextLine().trim());
                } catch (Exception ignored) {
                }
                for (int f = 1; f <= floors; f++) {
                    floorSlotsMap.put(f, sameSlots);
                }
            } else {
                // Setup rieng cho tung tang
                System.out.println("   Setup Slots for Each Floor:");
                for (int f = 1; f <= floors; f++) {
                    System.out.print("   - Enter total slots for Floor " + f + ": ");
                    int slots = MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR;
                    try {
                        slots = Integer.parseInt(scanner.nextLine().trim());
                    } catch (Exception ignored) {
                    }
                    floorSlotsMap.put(f, slots);
                }
            }
        } else {
            System.out.print("[CONFIG] Enter Total Slots for Floor 1: ");
            int slots = MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR;
            try {
                slots = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception ignored) {
            }
            floorSlotsMap.put(1, slots);
        }

        Set<VehicleType> supportedVehicles = new HashSet<>();
        Map<VehicleType, List<Integer>> vehicleZones = new HashMap<>();
        Map<VehicleType, Double> hourlyRates = new HashMap<>();
        Map<VehicleType, Double> overnightFees = new HashMap<>();
        boolean hasOvernightFee = false;

        System.out.println("\n--------------------------------------------------");
        System.out.println(" STEP 1: ZONE & VEHICLE SELECTION                ");
        System.out.println("--------------------------------------------------");

        for (VehicleType type : VehicleType.values()) {
            if (!isLarge) {
                System.out.print("Do you accept " + type.name() + "? (y/n): ");
                String acc = scanner.nextLine().trim().toLowerCase();
                if (acc.equals("y")) {
                    supportedVehicles.add(type);
                    vehicleZones.put(type, Collections.singletonList(1));
                }
            } else {
                System.out.print("Allow " + type.name() + " in this parking lot? (y/n): ");
                String acc = scanner.nextLine().trim().toLowerCase();
                if (acc.equals("y")) {
                    supportedVehicles.add(type);
                    System.out.println("   Available Floors: 1 to " + floors);
                    System.out.print("   Enter allowed floors (comma-separated, e.g. '1,2'): ");
                    String floorInput = scanner.nextLine().trim();
                    List<Integer> allowedFloors = new ArrayList<>();
                    for (String fStr : floorInput.split(",")) {
                        try {
                            int f = Integer.parseInt(fStr.trim());
                            if (f >= 1 && f <= floors)
                                allowedFloors.add(f);
                        } catch (Exception ignored) {
                        }
                    }
                    if (allowedFloors.isEmpty())
                        allowedFloors.add(1);
                    vehicleZones.put(type, allowedFloors);
                }
            }
        }

        if (supportedVehicles.isEmpty()) {
            System.out.println("[WARNING] No vehicle types selected! Defaulting to CAR on Floor 1.");
            supportedVehicles.add(VehicleType.CAR);
            vehicleZones.put(VehicleType.CAR, Collections.singletonList(1));
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println(" STEP 2: HOURLY PRICING SETUP                    ");
        System.out.println(" (Only showing active supported vehicles)         ");
        System.out.println("--------------------------------------------------");

        for (VehicleType type : supportedVehicles) {
            System.out.print("Enter Hourly Rate for " + type.name() + " (VND): ");
            double rate = MultiLotConfig.DEFAULT_BASE_RATE;
            try {
                rate = Double.parseDouble(scanner.nextLine().trim());
            } catch (Exception ignored) {
            }
            hourlyRates.put(type, rate);
        }

        if (isLarge) {
            System.out.println("\n--------------------------------------------------");
            System.out.println(" STEP 3: OVERNIGHT FEE CONFIGURATION             ");
            System.out.println("--------------------------------------------------");
            System.out.print("Do you want to charge Overnight Fees? (y/n): ");
            String nightOpt = scanner.nextLine().trim().toLowerCase();
            if (nightOpt.equals("y")) {
                hasOvernightFee = true;
                for (VehicleType type : supportedVehicles) {
                    System.out.print("Enter Overnight Flat Fee for " + type.name() + " (VND): ");
                    double fee = 50000.0;
                    try {
                        fee = Double.parseDouble(scanner.nextLine().trim());
                    } catch (Exception ignored) {
                    }
                    overnightFees.put(type, fee);
                }
            }
        }

        List<String> features = isLarge
                ? Arrays.asList("CHECK_IN", "CHECK_OUT", "VIEW_MATRIX", "EXPORT_REPORT", "MANAGE_ZONES")
                : Arrays.asList("CHECK_IN", "CHECK_OUT", "VIEW_MATRIX");

        System.out.println("\n[SUCCESS] Onboarding completed successfully!\n");

        return new UserProfile(name, floors, floorSlotsMap, isLarge, features,
                supportedVehicles, vehicleZones, hourlyRates, hasOvernightFee, overnightFees);
    }

    public static UserProfile updateExistingProfile(Scanner scanner, UserProfile currentProfile,
            UserProfileProviderImpl profileProvider) {
        if (currentProfile == null) {
            System.out.println("[INFO] No profile found in memory. Launching onboarding instead.");
            UserProfile onboarded = runOnboarding(scanner);
            if (profileProvider != null) {
                profileProvider.saveProfile(onboarded);
            }
            return onboarded;
        }

        System.out.println("\n==================================================");
        System.out.println("          UPDATE EXISTING SYSTEM PROFILE          ");
        System.out.println("==================================================");
        System.out.println("[CURRENT DATA] Old profile values shown below:");
        System.out.println(" - Enterprise Name : " + currentProfile.getEnterpriseName());
        System.out.println(" - Total Floors     : " + currentProfile.getTotalFloors());
        System.out.println(" - FloorSlotsMap    : " + currentProfile.getFloorSlotsMap());
        System.out.println(" - Is Large Scale   : " + currentProfile.isLargeScale());
        System.out.println(" - Supported Vehicles: " + currentProfile.getSupportedVehicles());
        System.out.println(" - Vehicle Zones    : " + currentProfile.getVehicleZones());
        System.out.println(" - Hourly Rates     : " + currentProfile.getHourlyRates());
        System.out.println(" - Overnight Fee    : " + currentProfile.hasOvernightFee());
        System.out.println(" - Overnight Fees   : " + currentProfile.getOvernightFees());

        String enterpriseName = currentProfile.getEnterpriseName();
        int totalFloors = currentProfile.getTotalFloors();
        Map<Integer, Integer> floorSlotsMap = new HashMap<>(currentProfile.getFloorSlotsMap());
        boolean isLargeScale = currentProfile.isLargeScale();
        List<String> features = new ArrayList<>(currentProfile.getEnabledFeatures());
        Set<VehicleType> supportedVehicles = new HashSet<>(currentProfile.getSupportedVehicles());
        Map<VehicleType, List<Integer>> vehicleZones = new HashMap<>(currentProfile.getVehicleZones());
        Map<VehicleType, Double> hourlyRates = new HashMap<>(currentProfile.getHourlyRates());
        boolean hasOvernightFee = currentProfile.hasOvernightFee();
        Map<VehicleType, Double> overnightFees = new HashMap<>(currentProfile.getOvernightFees());

        if (askYesNo(scanner, "Do you want to update Enterprise Name [old: " + enterpriseName + "]?")) {
            System.out.print("Enter new Enterprise Name: ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                enterpriseName = newName;
            }
        }

        if (askYesNo(scanner, "Do you want to update Total Floors [old: " + totalFloors + "]?")) {
            System.out.print("Enter new Total Floors: ");
            try {
                totalFloors = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception ignored) {
                System.out.println("[WARNING] Invalid floor count, old value kept.");
            }
        }

        if (askYesNo(scanner, "Do you want to update Floor Slot Map [old: " + floorSlotsMap + "]?")) {
            System.out.print("Enter floor slots using format 1:10;2:12;3:14 (or press Enter to keep old): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty() && input.contains(":")) {
                Map<Integer, Integer> newMap = new HashMap<>();
                String[] entries = input.split(";");
                for (String entry : entries) {
                    if (entry.isBlank()) {
                        continue;
                    }
                    String[] pair = entry.split(":");
                    if (pair.length == 2) {
                        try {
                            newMap.put(Integer.parseInt(pair[0].trim()), Integer.parseInt(pair[1].trim()));
                        } catch (Exception ignored) {
                        }
                    }
                }
                if (!newMap.isEmpty()) {
                    floorSlotsMap = newMap;
                }
            }
        }

        if (askYesNo(scanner, "Do you want to update large-scale mode [old: " + isLargeScale + "]?")) {
            System.out.print("Enter new scalability mode (true/false): ");
            String val = scanner.nextLine().trim().toLowerCase();
            if (val.equals("true") || val.equals("false")) {
                isLargeScale = Boolean.parseBoolean(val);
            }
        }

        if (askYesNo(scanner, "Do you want to update supported vehicles and zones [old: " + vehicleZones + "]?")) {
            System.out.println(
                    "[INFO] Re-enter vehicle zones by type using floor numbers separated by commas. Example: 1,2");
            Map<VehicleType, List<Integer>> newZones = new HashMap<>();
            for (VehicleType type : VehicleType.values()) {
                System.out.print("Enter allowed floors for " + type.name() + " [old: "
                        + vehicleZones.getOrDefault(type, Collections.singletonList(1)) + "] or press Enter to keep: ");
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    newZones.put(type, vehicleZones.get(type));
                } else {
                    List<Integer> floors = new ArrayList<>();
                    for (String piece : line.split(",")) {
                        try {
                            int f = Integer.parseInt(piece.trim());
                            if (f >= 1 && f <= totalFloors) {
                                floors.add(f);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    if (floors.isEmpty()) {
                        floors.add(1);
                    }
                    newZones.put(type, floors);
                    supportedVehicles.add(type);
                }
            }
            vehicleZones = newZones;
        }

        if (askYesNo(scanner, "Do you want to update hourly rates [old: " + hourlyRates + "]?")) {
            System.out.println("[INFO] Re-enter hourly rate for supported vehicles.");
            Map<VehicleType, Double> newRates = new HashMap<>();
            for (VehicleType type : supportedVehicles) {
                System.out.print("Enter hourly rate for " + type.name() + " [old: "
                        + hourlyRates.getOrDefault(type, MultiLotConfig.DEFAULT_BASE_RATE) + "]: ");
                try {
                    double rate = Double.parseDouble(scanner.nextLine().trim());
                    newRates.put(type, rate);
                } catch (Exception ignored) {
                    newRates.put(type, hourlyRates.getOrDefault(type, MultiLotConfig.DEFAULT_BASE_RATE));
                }
            }
            hourlyRates = newRates;
        }

        if (askYesNo(scanner, "Do you want to update Overnight Fee setting [old: " + hasOvernightFee + "]?")) {
            System.out.print("Enter overnight enable value (true/false): ");
            String val = scanner.nextLine().trim().toLowerCase();
            if (val.equals("true") || val.equals("false")) {
                hasOvernightFee = Boolean.parseBoolean(val);
            }
        }

        if (hasOvernightFee
                && askYesNo(scanner, "Do you want to update overnight fees [old: " + overnightFees + "]?")) {
            Map<VehicleType, Double> newFees = new HashMap<>();
            for (VehicleType type : supportedVehicles) {
                System.out.print("Enter overnight fee for " + type.name() + " [old: "
                        + overnightFees.getOrDefault(type, 50000.0) + "]: ");
                try {
                    double fee = Double.parseDouble(scanner.nextLine().trim());
                    newFees.put(type, fee);
                } catch (Exception ignored) {
                    newFees.put(type, overnightFees.getOrDefault(type, 50000.0));
                }
            }
            overnightFees = newFees;
        }

        UserProfile updatedProfile = new UserProfile(
                enterpriseName,
                totalFloors,
                floorSlotsMap,
                isLargeScale,
                features,
                supportedVehicles,
                vehicleZones,
                hourlyRates,
                hasOvernightFee,
                overnightFees);

        if (profileProvider != null) {
            profileProvider.saveProfile(updatedProfile);
        }

        return updatedProfile;
    }

    private static boolean askYesNo(Scanner scanner, String title) {
        System.out.println();
        System.out.println(title);
        System.out.print("(y/n): ");
        String opt = scanner.nextLine().trim().toLowerCase();
        return opt.equals("y") || opt.equals("yes");
    }
}