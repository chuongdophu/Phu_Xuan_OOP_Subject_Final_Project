package com.autoparking.core.interfaces;

import com.autoparking.model.Ticket;
import java.util.List;

public interface IHistoryManager {
    void recordTransaction(Ticket ticket);

    void recordActiveTicket(Ticket ticket);

    List<Ticket> loadActiveTickets();

    List<Ticket> getHistoryList();
}