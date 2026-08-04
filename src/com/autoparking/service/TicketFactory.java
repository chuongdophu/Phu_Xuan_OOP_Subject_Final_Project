package com.autoparking.service;

import com.autoparking.model.Ticket;
import com.autoparking.model.VehicleType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TicketFactory {
    public static Ticket createTicket(String licensePlate, VehicleType vehicleType, String parkLotId) {
        LocalDateTime timeIn = LocalDateTime.now();
        String cleanPlate = licensePlate.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        String timestamp = timeIn.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String ticketId = cleanPlate + "_" + timestamp;

        return new Ticket(ticketId, licensePlate, vehicleType, parkLotId, timeIn);
    }
}