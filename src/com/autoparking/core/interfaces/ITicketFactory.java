package com.autoparking.core.interfaces;

import com.autoparking.model.Ticket;
import com.autoparking.model.VehicleType;

public interface ITicketFactory {
    Ticket createTicket(String licensePlate, VehicleType vehicleType, String slotId);
}