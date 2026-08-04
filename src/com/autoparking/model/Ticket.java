package com.autoparking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private final String ticketId;
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final String parkLotId;
    private final LocalDateTime timeIn;
    private LocalDateTime timeOut;
    private double totalPrice;

    public Ticket(String ticketId, String licensePlate, VehicleType vehicleType, String parkLotId,
            LocalDateTime timeIn) {
        this.ticketId = ticketId;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.parkLotId = parkLotId;
        this.timeIn = timeIn;
    }

    // Bilingual Ticket Output for End-Customers
    public void printBilingualTicket() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        System.out.println("\n==================================================");
        System.out.println("        AUTOMATED PARKING TICKET / VÉ GỬI XE      ");
        System.out.println("==================================================");
        System.out.println(" Ticket ID / Mã vé:        " + ticketId);
        System.out.println(" License Plate / Biển số:  " + licensePlate);
        System.out.println(" Vehicle Type / Loại xe:   " + vehicleType.getDescription());
        System.out.println(" Park Lot ID / Vị trí đỗ:  " + parkLotId);
        System.out.println(" Time In / Thời gian vào:  " + timeIn.format(fmt));
        System.out.println("--------------------------------------------------");
        System.out.println(" * Vui lòng giữ vé cẩn thận để xuất bãi.");
        System.out.println(" * Please keep this ticket carefully for exit.");
        System.out.println("==================================================\n");
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getParkLotId() {
        return parkLotId;
    }

    public LocalDateTime getTimeIn() {
        return timeIn;
    }

    public LocalDateTime getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(LocalDateTime timeOut) {
        this.timeOut = timeOut;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}