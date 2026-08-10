package com.autoparking.service.impl;

import com.autoparking.core.interfaces.ITicketValidator;

public class TicketValidatorImpl implements ITicketValidator {
    @Override
    public boolean isValidTicketId(String ticketId) {
        return ticketId != null && ticketId.matches("^TICK-[A-Z0-9]{8}$");
    }
}