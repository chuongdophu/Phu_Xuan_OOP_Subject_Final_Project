package com.autoparking.service;

import com.autoparking.config.MultiLotConfig;
import com.autoparking.core.interfaces.*;
import com.autoparking.model.*;
import com.autoparking.service.impl.UserProfileProviderImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingManagerService {

    private static final String ACTIVE_FILE = MultiLotConfig.ACTIVE_TICKETS_FILE;

    private UserProfile profile;
    private final ITicketFactory ticketFactory;
    private final ITicketValidator ticketValidator;
    private final IPricingCalculator pricingCalculator;
    private final ITimeProvider timeProvider;
    private final IHistoryManager historyManager;
    private final UserProfileProviderImpl profileProvider;
    private final Map<String, Ticket> activeTicketsMap;

    public ParkingManagerService(UserProfile profile,
            ITicketFactory ticketFactory,
            ITicketValidator ticketValidator,
            IPricingCalculator pricingCalculator,
            ITimeProvider timeProvider,
            IHistoryManager historyManager,
            UserProfileProviderImpl profileProvider) {
        this.profile = profile;
        this.ticketFactory = ticketFactory;
        this.ticketValidator = ticketValidator;
        this.pricingCalculator = pricingCalculator;
        this.timeProvider = timeProvider;
        this.historyManager = historyManager;
        this.profileProvider = profileProvider;
        this.activeTicketsMap = new HashMap<>();

        // Nạp dữ liệu từ Excel vào RAM khi mở app
        if (this.historyManager != null) {
            refreshActiveTicketsFromExcel();
        }
    }

    public void refreshActiveTicketsFromExcel() {
        this.activeTicketsMap.clear();
        if (this.historyManager != null) {
            List<Ticket> savedActiveTickets = this.historyManager.loadActiveTickets();
            if (savedActiveTickets != null && !savedActiveTickets.isEmpty()) {
                for (Ticket ticket : savedActiveTickets) {
                    this.activeTicketsMap.put(ticket.getTicketId(), ticket);
                }
            }
        }
    }

    // --- CHECK-IN XE VÀO BÃI ---
    public Ticket checkIn(String licensePlate, VehicleType vehicleType) {
        String availableSlot = findAvailableSlot(vehicleType);
        if (availableSlot == null) {
            System.out.println("\n[ERROR] Parking lot is FULL or no slot available for " + vehicleType.name());
            return null;
        }

        Ticket ticket = ticketFactory.createTicket(licensePlate, vehicleType, availableSlot);

        if (ticketValidator != null) {
            boolean isValid = ticketValidator.isValidTicketId(ticket.getTicketId());
            if (!isValid) {
                System.out.println("\n[ERROR] Ticket validation failed!");
                return null;
            }
        }

        activeTicketsMap.put(ticket.getTicketId(), ticket);
        if (historyManager != null) {
            historyManager.recordActiveTicket(ticket);
        }
        return ticket;
    }

    // --- CHECK-OUT XE XUẤT BÃI ---
    public Ticket checkOut(String ticketId) {
        Ticket ticket = activeTicketsMap.get(ticketId);
        if (ticket == null) {
            System.out.println("\n[ERROR] Ticket ID not found or already checked out!");
            return null;
        }

        ticket.setTimeOut(timeProvider.getCurrentTime());

        double baseRate = MultiLotConfig.DEFAULT_BASE_RATE;
        if (profile != null && profile.getHourlyRates() != null) {
            Double configuredRate = profile.getHourlyRates().get(ticket.getVehicleType());
            if (configuredRate != null) {
                baseRate = configuredRate;
            }
        }

        double fee = pricingCalculator.calculateFee(
                ticket.getVehicleType(),
                ticket.getTimeIn(),
                ticket.getTimeOut(),
                baseRate);

        ticket.setTotalFee(fee);

        activeTicketsMap.remove(ticketId);
        if (historyManager != null) {
            historyManager.recordTransaction(ticket);
        }

        rewriteActiveTicketsExcel();
        return ticket;
    }

    // --- TÌM SLOT TRỐNG PHÙ HỢP ---
    private String findAvailableSlot(VehicleType vehicleType) {
        Map<VehicleType, List<Integer>> vehicleZones = profile.getVehicleZones();
        List<Integer> allowedFloors = null;
        if (vehicleZones != null) {
            allowedFloors = vehicleZones.get(vehicleType);
        }
        if (allowedFloors == null || allowedFloors.isEmpty()) {
            allowedFloors = Collections.singletonList(1);
        }

        int totalFloors = profile.getTotalFloors();
        Map<Integer, Integer> floorSlotsMap = profile.getFloorSlotsMap();
        if (floorSlotsMap == null || floorSlotsMap.isEmpty()) {
            floorSlotsMap = new HashMap<>();
            for (int f = 1; f <= totalFloors; f++) {
                floorSlotsMap.put(f, MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR);
            }
        }

        for (int floor : allowedFloors) {
            if (floor <= 0 || floor > totalFloors) {
                continue;
            }

            int slotsPerFloor = floorSlotsMap.getOrDefault(floor, MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR);
            for (int slot = 1; slot <= slotsPerFloor; slot++) {
                String slotId = "F" + floor + "-S" + slot;
                boolean isOccupied = activeTicketsMap.values().stream()
                        .anyMatch(t -> slotId.equalsIgnoreCase(t.getSlotId()));

                if (!isOccupied) {
                    return slotId;
                }
            }
        }
        return null;
    }

    // --- GHI ĐÈ FILE TEXT ACTIVE KHI CHECK-OUT ---
    private void rewriteActiveTicketsExcel() {
        File dir = new File(MultiLotConfig.DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(ACTIVE_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("TicketID|LicensePlate|VehicleType|SlotID|TimeIn");
            writer.newLine();

            for (Ticket t : activeTicketsMap.values()) {
                String line = String.join("|",
                        t.getTicketId(),
                        t.getLicensePlate(),
                        t.getVehicleType().name(),
                        t.getSlotId(),
                        t.getTimeIn() != null ? t.getTimeIn().toString() : "");
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to update active tickets storage: " + e.getMessage());
        }
    }

    // --- HIỂN THỊ MA TRẬN BÃI XE ---
    public void displayMatrix() {
        refreshActiveTicketsFromExcel();

        System.out
                .println("\n=========================================================================================");
        System.out.println("                                DETAILED PARKING MATRIX                                  ");
        System.out.println("=========================================================================================");

        int totalFloors = profile.getTotalFloors();
        Map<Integer, Integer> floorSlotsMap = profile.getFloorSlotsMap();

        for (int f = 1; f <= totalFloors; f++) {
            int slotsPerFloor = floorSlotsMap != null
                    ? floorSlotsMap.getOrDefault(f, MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR)
                    : MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR;

            System.out.println(">>> FLOOR " + f + " <<<");
            for (int s = 1; s <= slotsPerFloor; s++) {
                String slotId = "F" + f + "-S" + s;

                Ticket occupiedTicket = null;
                for (Ticket t : activeTicketsMap.values()) {
                    if (slotId.equalsIgnoreCase(t.getSlotId())) {
                        occupiedTicket = t;
                        break;
                    }
                }

                if (occupiedTicket != null) {
                    System.out.print("  [" + slotId + ": " + occupiedTicket.getLicensePlate() + "]  ");
                } else {
                    System.out.print("  [" + slotId + ": EMPTY]  ");
                }

                if (s % 5 == 0)
                    System.out.println();
            }
            System.out.println(
                    "\n-----------------------------------------------------------------------------------------");
        }
    }

    public UserProfile getProfile() {
        return profile;
    }

    public Map<String, Ticket> getActiveTicketsMap() {
        return activeTicketsMap;
    }

    public void updateSystemSetup(UserProfile newProfile) {
        this.profile = newProfile;
        if (this.profileProvider != null) {
            this.profileProvider.saveProfile(newProfile);
        }
        System.out.println("\n[SUCCESS] System Configuration Updated!");
    }
}