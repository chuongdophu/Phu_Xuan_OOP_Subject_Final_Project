package com.autoparking.factory;

import com.autoparking.core.interfaces.ITicketFactory;
import com.autoparking.core.interfaces.ITimeProvider;
import com.autoparking.model.Ticket;
import com.autoparking.model.VehicleType;
import java.util.UUID;

public class TicketFactoryImpl implements ITicketFactory {
    private final ITimeProvider timeProvider;

    public TicketFactoryImpl(ITimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public Ticket createTicket(String licensePlate, VehicleType vehicleType, String slotId) {
        String ticketId = "TICK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new Ticket(ticketId, licensePlate, vehicleType, slotId, timeProvider.getCurrentTime());
    }
}