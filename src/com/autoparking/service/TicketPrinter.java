package com.autoparking.service;

import com.autoparking.model.Ticket;
import com.autoparking.model.VehicleType;

public class TicketPrinter {

    private static String getVehicleName(VehicleType type) {
        return switch (type) {
            case CAR -> "O to con (Car)";
            case MOTORBIKE -> "Xe may (Motorbike)";
            case TRUCK -> "Xe tai (Truck)";
            case EV -> "Xe dien (EV)";
            case BUS -> "Xe khach (Bus)";
        };
    }

    public static void printEntryTicket(Ticket ticket, int floor, int slotNumber) {
        System.out.println("\n==================================================");
        System.out.println("          CHECK IN TICKET (VE XE VAO BAI)           ");
        System.out.println("==================================================");
        System.out.println("Ma ve          : " + ticket.getTicketId());
        System.out.println("Bien so xe     : " + ticket.getLicensePlate());
        System.out.println("Loai xe        : " + getVehicleName(ticket.getVehicleType()));
        System.out.println("Vi tri do      : Tang " + floor + " - O so " + slotNumber);
        System.out.println("Thoi gian vao  : " + ticket.getTimeIn());
        System.out.println("==================================================");
        System.out.println("   Xin vui long giu ve de doi chieu khi ra bai!   ");
        System.out.println("==================================================\n");
    }

    public static void printCheckoutReceipt(Ticket ticket, double totalFee, long hoursParked) {
        System.out.println("\n==================================================");
        System.out.println("                RECEIPT (HOA DON)                 ");
        System.out.println("==================================================");
        System.out.println("Ma ve          : " + ticket.getTicketId());
        System.out.println("Bien so xe     : " + ticket.getLicensePlate());
        System.out.println("Thoi gian do   : " + hoursParked + " gio");
        System.out.println("--------------------------------------------------");
        System.out.printf("TONG TIEN      : %,.0f VND%n", totalFee);
        System.out.println("==================================================");
        System.out.println("   Cam on quy khach va chuc thuong lo binh an!    ");
        System.out.println("==================================================\n");
    }
}