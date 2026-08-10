package com.autoparking.service;

import com.autoparking.config.MultiLotConfig;
import com.autoparking.core.interfaces.*;
import com.autoparking.model.ParkingSlot;
import com.autoparking.model.*;
import com.autoparking.service.impl.UserProfileProviderImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

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
    private ParkingSlot[][] parkingGrid;
    private int maxSlotsPerFloor;
    private final Map<VehicleType, ZoneAllocator> zoneAllocators;

    private static class ZoneAllocator {
        private final PriorityQueue<String> heap;
        private final Set<String> inHeap;

        private ZoneAllocator(PriorityQueue<String> heap, Set<String> inHeap) {
            this.heap = heap;
            this.inHeap = inHeap;
        }
    }

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
        this.zoneAllocators = new HashMap<>();

        initializeRuntimeStructures();

        // Nạp dữ liệu từ storage vào RAM khi mở app
        refreshActiveTicketsFromExcel();
    }

    public void refreshActiveTicketsFromExcel() {
        this.activeTicketsMap.clear();
        initializeRuntimeStructures();

        if (this.historyManager != null) {
            List<Ticket> savedActiveTickets = this.historyManager.loadActiveTickets();
            if (savedActiveTickets != null && !savedActiveTickets.isEmpty()) {
                for (Ticket ticket : savedActiveTickets) {
                    if (!isValidSlotId(ticket.getSlotId())) {
                        continue;
                    }
                    this.activeTicketsMap.put(ticket.getTicketId(), ticket);
                    markSlotOccupied(ticket.getSlotId(), ticket.getLicensePlate(), ticket.getTicketId());
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
        markSlotOccupied(ticket.getSlotId(), ticket.getLicensePlate(), ticket.getTicketId());
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
        releaseSlot(ticket.getSlotId());
        if (historyManager != null) {
            historyManager.recordTransaction(ticket);
        }

        rewriteActiveTicketsExcel();
        return ticket;
    }

    // --- TÌM SLOT TRỐNG PHÙ HỢP ---
    private String findAvailableSlot(VehicleType vehicleType) {
        ZoneAllocator allocator = zoneAllocators.get(vehicleType);
        if (allocator == null) {
            return null;
        }

        while (!allocator.heap.isEmpty()) {
            String slotId = allocator.heap.poll();
            allocator.inHeap.remove(slotId);
            if (!isSlotOccupied(slotId)) {
                return slotId;
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
                ParkingSlot cell = parkingGrid[f][s];
                String slotId = "F" + f + "-S" + s;

                if (cell != null && cell.isOccupied()) {
                    System.out.print("  [" + slotId + ": " + cell.getLicensePlate() + "]  ");
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
        refreshActiveTicketsFromExcel();
        System.out.println("\n[SUCCESS] System Configuration Updated!");
    }

    private void initializeRuntimeStructures() {
        int totalFloors = Math.max(1, profile != null ? profile.getTotalFloors() : 1);
        Map<Integer, Integer> floorSlotsMap = profile != null ? profile.getFloorSlotsMap() : null;
        if (floorSlotsMap == null || floorSlotsMap.isEmpty()) {
            floorSlotsMap = new HashMap<>();
            for (int f = 1; f <= totalFloors; f++) {
                floorSlotsMap.put(f, MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR);
            }
        }

        maxSlotsPerFloor = MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR;
        for (int f = 1; f <= totalFloors; f++) {
            int slots = floorSlotsMap.getOrDefault(f, MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR);
            if (slots > maxSlotsPerFloor) {
                maxSlotsPerFloor = slots;
            }
        }

        parkingGrid = new ParkingSlot[totalFloors + 1][maxSlotsPerFloor + 1];
        for (int f = 1; f <= totalFloors; f++) {
            int slotsPerFloor = floorSlotsMap.getOrDefault(f, MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR);
            for (int s = 1; s <= slotsPerFloor; s++) {
                String slotId = "F" + f + "-S" + s;
                parkingGrid[f][s] = new ParkingSlot(slotId, f, s);
            }
        }

        zoneAllocators.clear();
        Map<VehicleType, List<Integer>> zones = profile != null ? profile.getVehicleZones() : null;
        Set<VehicleType> supported = profile != null ? profile.getSupportedVehicles() : null;
        if (supported == null || supported.isEmpty()) {
            supported = new HashSet<>();
            supported.add(VehicleType.CAR);
        }

        for (VehicleType type : supported) {
            PriorityQueue<String> heap = new PriorityQueue<>(Comparator.comparingInt(this::slotOrderKey));
            Set<String> inHeap = new HashSet<>();

            List<Integer> allowedFloors = zones != null ? zones.get(type) : null;
            if (allowedFloors == null || allowedFloors.isEmpty()) {
                allowedFloors = Collections.singletonList(1);
            }

            for (int floor : allowedFloors) {
                if (floor <= 0 || floor >= parkingGrid.length) {
                    continue;
                }
                int slotsPerFloor = floorSlotsMap.getOrDefault(floor, MultiLotConfig.DEFAULT_SLOTS_PER_FLOOR);
                for (int s = 1; s <= slotsPerFloor; s++) {
                    String slotId = "F" + floor + "-S" + s;
                    heap.offer(slotId);
                    inHeap.add(slotId);
                }
            }

            zoneAllocators.put(type, new ZoneAllocator(heap, inHeap));
        }
    }

    private int slotOrderKey(String slotId) {
        int[] coordinates = parseSlotCoordinates(slotId);
        if (coordinates == null) {
            return Integer.MAX_VALUE;
        }
        return coordinates[0] * 10000 + coordinates[1];
    }

    private int[] parseSlotCoordinates(String slotId) {
        if (slotId == null || !slotId.startsWith("F") || !slotId.contains("-S")) {
            return null;
        }

        try {
            String[] parts = slotId.split("-S");
            int floor = Integer.parseInt(parts[0].substring(1));
            int slot = Integer.parseInt(parts[1]);
            return new int[] { floor, slot };
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isValidSlotId(String slotId) {
        int[] coordinates = parseSlotCoordinates(slotId);
        if (coordinates == null) {
            return false;
        }
        int floor = coordinates[0];
        int slot = coordinates[1];
        return floor > 0
                && slot > 0
                && floor < parkingGrid.length
                && slot < parkingGrid[floor].length
                && parkingGrid[floor][slot] != null;
    }

    private boolean isSlotOccupied(String slotId) {
        if (!isValidSlotId(slotId)) {
            return true;
        }
        int[] coordinates = parseSlotCoordinates(slotId);
        return parkingGrid[coordinates[0]][coordinates[1]].isOccupied();
    }

    private void markSlotOccupied(String slotId, String licensePlate, String ticketId) {
        if (!isValidSlotId(slotId)) {
            return;
        }
        int[] coordinates = parseSlotCoordinates(slotId);
        parkingGrid[coordinates[0]][coordinates[1]].occupy(licensePlate, ticketId);
    }

    private void releaseSlot(String slotId) {
        if (!isValidSlotId(slotId)) {
            return;
        }

        int[] coordinates = parseSlotCoordinates(slotId);
        ParkingSlot slot = parkingGrid[coordinates[0]][coordinates[1]];
        slot.release();

        int floor = coordinates[0];
        for (Map.Entry<VehicleType, ZoneAllocator> entry : zoneAllocators.entrySet()) {
            List<Integer> floors = profile.getVehicleZones() != null
                    ? profile.getVehicleZones().get(entry.getKey())
                    : null;
            if (floors == null || floors.isEmpty()) {
                floors = Collections.singletonList(1);
            }
            if (!floors.contains(floor)) {
                continue;
            }

            ZoneAllocator allocator = entry.getValue();
            if (allocator.inHeap.add(slotId)) {
                allocator.heap.offer(slotId);
            }
        }
    }
}