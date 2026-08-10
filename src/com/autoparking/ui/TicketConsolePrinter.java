package com.autoparking.ui;

import com.autoparking.model.Ticket;

public class TicketConsolePrinter {
    public static void printCheckInTicket(Ticket ticket) {
        System.out.println("\n----------------------------------------------");
        System.out.println("              PARKING TICKET                  ");
        System.out.println("----------------------------------------------");
        System.out.println(" Ticket ID : " + ticket.getTicketId());
        System.out.println(" Plate     : " + ticket.getLicensePlate());
        System.out.println(" Type      : " + ticket.getVehicleType());
        System.out.println(" Slot      : " + ticket.getSlotId());
        System.out.println(" Time In   : " + ticket.getTimeIn());
        System.out.println("----------------------------------------------");
    }

    public static void printCheckOutReceipt(Ticket ticket) {
        System.out.println("\n----------------------------------------------");
        System.out.println("             CHECK-OUT RECEIPT                ");
        System.out.println("----------------------------------------------");
        System.out.println(" Ticket ID : " + ticket.getTicketId());
        System.out.println(" Plate     : " + ticket.getLicensePlate());
        System.out.println(" Time In   : " + ticket.getTimeIn());
        System.out.println(" Time Out  : " + ticket.getTimeOut());
        System.out.println(" Total Fee : " + String.format("%,.0f VND", ticket.getTotalFee()));
        System.out.println("----------------------------------------------");
    }
}