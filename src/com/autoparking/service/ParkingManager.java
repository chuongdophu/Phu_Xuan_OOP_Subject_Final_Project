package com.autoparking.service;

import com.autoparking.config.ParkingConfig;
import com.autoparking.model.ParkingSlot;
import com.autoparking.model.Ticket;
import com.autoparking.model.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParkingManager {
    private ParkingConfig config;
    private List<ParkingSlot> allSlots;
    private List<Ticket> activeTickets;
    private List<Ticket> historicalTickets;

    public ParkingManager(ParkingConfig config, List<Ticket> activeTickets, List<Ticket> historicalTickets) {
        this.config = config;
        this.allSlots = new ArrayList<>();
        this.activeTickets = activeTickets;
        this.historicalTickets = historicalTickets;
        initializeSlots();
        syncSlotState();
    }

    private void initializeSlots() {
        for (int f = 1; f <= config.getTotalFloors(); f++) {
            for (int s = 1; s <= config.getSlotsPerFloor(); s++) {
                allSlots.add(new ParkingSlot(f, s));
            }
        }
    }

    private void syncSlotState() {
        for (Ticket t : activeTickets) {
            for (ParkingSlot slot : allSlots) {
                if (slot.getPosition().equals(t.getParkLotId())) {
                    slot.parkVehicle(t.getLicensePlate(), t.getVehicleType());
                    break;
                }
            }
        }
    }

    public ParkingSlot findAvailableSlot(VehicleType type) {
        for (int currentFloor = 1; currentFloor <= config.getTotalFloors(); currentFloor++) {
            if (!config.isFloorAllowed(currentFloor, type))
                continue;

            for (ParkingSlot slot : allSlots) {
                if (slot.getFloor() == currentFloor && !slot.isOccupied()) {
                    return slot;
                }
            }
        }
        return null;
    }

    public void processCheckIn(String licensePlate, VehicleType type) {
        for (Ticket t : activeTickets) {
            if (t.getLicensePlate().equalsIgnoreCase(licensePlate)) {
                System.out.println("\n[LOI] Xe co bien so '" + licensePlate + "' hien da co trong bai!");
                return;
            }
        }

        ParkingSlot slot = findAvailableSlot(type);
        if (slot == null) {
            System.out.println("\n[LOI] Khong con chu trong cho loai xe: " + type.getDescription());
            return;
        }

        slot.parkVehicle(licensePlate, type);
        Ticket ticket = TicketFactory.createTicket(licensePlate, type, slot.getPosition());
        activeTickets.add(ticket);

        // Luu ngay thong tin ve xe khach hang vao config.txt
        config.saveToFile(activeTickets, historicalTickets);

        System.out.println("\n[THANH CONG] Cho xe vao bai thanh cong!");
        TicketPrinter.printEntryTicket(ticket, slot.getFloor(), slot.getSlotNumber());
    }

    public void processCheckOut(String query) {
        Ticket targetTicket = null;
        for (Ticket ticket : activeTickets) {
            if (ticket.getTicketId().equalsIgnoreCase(query) || ticket.getLicensePlate().equalsIgnoreCase(query)) {
                targetTicket = ticket;
                break;
            }
        }

        if (targetTicket == null) {
            System.out.println("\n[LOI] Khong tim thay Ma ve hoac Bien so xe trong he thong: " + query);
            return;
        }

        for (ParkingSlot slot : allSlots) {
            if (slot.getPosition().equals(targetTicket.getParkLotId())) {
                slot.freeSlot();
                break;
            }
        }

        LocalDateTime timeOut = LocalDateTime.now();
        targetTicket.setTimeOut(timeOut);

        long minutes = Duration.between(targetTicket.getTimeIn(), timeOut).toMinutes();
        long hoursParked = (long) Math.ceil(minutes / 60.0);
        if (hoursParked <= 0)
            hoursParked = 1;

        double rate = config.getRate(targetTicket.getVehicleType());
        double totalFee = hoursParked * rate;
        targetTicket.setTotalPrice(totalFee);

        activeTickets.remove(targetTicket);
        historicalTickets.add(targetTicket);

        // Cap nhat lai file config.txt sau khi xe ra
        config.saveToFile(activeTickets, historicalTickets);

        TicketPrinter.printCheckoutReceipt(targetTicket, totalFee, hoursParked);
    }

    public void displayParkingStatus() {
        System.out.println("\n==================================================");
        System.out.println("               PARKING LOT STATUS                 ");
        System.out.println("==================================================");
        System.out.println("Business Name   : " + config.getLotName());
        System.out.println("Total Capacity  : " + config.getTotalCapacity() + " slots");
        System.out.println("Occupied Slots  : " + activeTickets.size() + " slots");
        System.out.println("Available Slots : " + (config.getTotalCapacity() - activeTickets.size()) + " slots");
        System.out.println("--------------------------------------------------");

        for (int f = 1; f <= config.getTotalFloors(); f++) {
            int occupiedCount = 0;
            for (ParkingSlot slot : allSlots) {
                if (slot.getFloor() == f && slot.isOccupied()) {
                    occupiedCount++;
                }
            }
            System.out.println("Floor " + f + ": " + occupiedCount + "/" + config.getSlotsPerFloor() + " occupied");
        }
        System.out.println("==================================================\n");
    }

    public void displayActiveVehicles() {
        System.out.println("\n==================================================");
        System.out.println("           CURRENTLY PARKED VEHICLES              ");
        System.out.println("==================================================");
        if (activeTickets.isEmpty()) {
            System.out.println(" Khong co xe nao dang do trong bai.");
        } else {
            for (Ticket t : activeTickets) {
                System.out.printf(" - Plate: %-10s | Type: %-15s | Slot: %s%n",
                        t.getLicensePlate(), t.getVehicleType().getDescription(), t.getParkLotId());
            }
        }
        System.out.println("==================================================\n");
    }

    public void exportReport() {
        config.saveToFile(activeTickets, historicalTickets);
        System.out.println("\n==================================================");
        System.out.println(" [THANH CONG] Da dong bo toan bo du lieu vao 'config.txt'!");
        System.out.println("==================================================\n");
    }
}