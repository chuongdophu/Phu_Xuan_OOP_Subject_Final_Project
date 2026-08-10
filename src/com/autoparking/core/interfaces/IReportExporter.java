package com.autoparking.core.interfaces;

public interface IReportExporter {
    void exportReport();

    void exportActiveTicketsCsv();

    void exportHistoryCsv();

    void printActiveTickets();

    void printHistory();
}