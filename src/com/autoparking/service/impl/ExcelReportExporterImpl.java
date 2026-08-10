package com.autoparking.service.impl;

import com.autoparking.config.MultiLotConfig;
import com.autoparking.core.interfaces.IHistoryManager;
import com.autoparking.core.interfaces.IReportExporter;
import com.autoparking.model.Ticket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ExcelReportExporterImpl implements IReportExporter {

    private final IHistoryManager historyManager;

    public ExcelReportExporterImpl(IHistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public void exportReport() {
        System.out.println("\n[SYSTEM ALERT] Analytics Report exported successfully.");
        exportHistoryCsv();
    }

    @Override
    public void exportActiveTicketsCsv() {
        List<Ticket> activeTickets = historyManager != null ? historyManager.loadActiveTickets() : List.of();
        File dir = new File(MultiLotConfig.DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File csv = new File(MultiLotConfig.DB_DIR + "active_tickets.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv))) {
            writeCsvRow(writer, "TicketID", "LicensePlate", "VehicleType", "SlotID", "TimeIn");

            if (activeTickets != null && !activeTickets.isEmpty()) {
                for (Ticket ticket : activeTickets) {
                    writeCsvRow(writer,
                            ticket.getTicketId(),
                            ticket.getLicensePlate(),
                            ticket.getVehicleType() != null ? ticket.getVehicleType().name() : "",
                            ticket.getSlotId(),
                            ticket.getTimeIn() != null ? ticket.getTimeIn().toString() : "");
                }
            }

            System.out.println("[SUCCESS] Active tickets CSV exported to: " + csv.getPath());
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to export active tickets CSV: " + e.getMessage());
        }
    }

    @Override
    public void exportHistoryCsv() {
        List<Ticket> history = historyManager != null ? historyManager.getHistoryList() : List.of();
        File dir = new File(MultiLotConfig.DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File csv = new File(MultiLotConfig.DB_DIR + "history_parking.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv))) {
            writeCsvRow(writer, "TicketID", "LicensePlate", "VehicleType", "SlotID", "TimeIn", "TimeOut", "TotalFee");

            if (history != null && !history.isEmpty()) {
                for (Ticket ticket : history) {
                    writeCsvRow(writer,
                            ticket.getTicketId(),
                            ticket.getLicensePlate(),
                            ticket.getVehicleType() != null ? ticket.getVehicleType().name() : "",
                            ticket.getSlotId(),
                            ticket.getTimeIn() != null ? ticket.getTimeIn().toString() : "",
                            ticket.getTimeOut() != null ? ticket.getTimeOut().toString() : "",
                            String.valueOf(ticket.getTotalFee()));
                }
            }

            System.out.println("[SUCCESS] History CSV exported to: " + csv.getPath());
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to export history CSV: " + e.getMessage());
        }
    }

    private void writeCsvRow(BufferedWriter writer, String... fields) throws Exception {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                writer.write(",");
            }
            writer.write(escapeCsv(fields[i]));
        }
        writer.newLine();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    @Override
    public void printActiveTickets() {
        List<Ticket> activeTickets = historyManager != null ? historyManager.loadActiveTickets() : List.of();
        System.out.println("\n=== ACTIVE TICKETS CONSOLE VIEW ===");
        if (activeTickets == null || activeTickets.isEmpty()) {
            System.out.println("[INFO] No active tickets currently found.");
            return;
        }
        for (Ticket ticket : activeTickets) {
            System.out.println("- " + ticket.getTicketId() + " | "
                    + ticket.getLicensePlate() + " | "
                    + ticket.getVehicleType() + " | "
                    + ticket.getSlotId() + " | "
                    + (ticket.getTimeIn() != null ? ticket.getTimeIn() : ""));
        }
    }

    @Override
    public void printHistory() {
        List<Ticket> history = historyManager != null ? historyManager.getHistoryList() : List.of();
        System.out.println("\n=== HISTORY CONSOLE VIEW ===");
        if (history == null || history.isEmpty()) {
            System.out.println("[INFO] No history records found.");
            return;
        }
        for (Ticket ticket : history) {
            System.out.println("- " + ticket.getTicketId() + " | "
                    + ticket.getLicensePlate() + " | "
                    + ticket.getVehicleType() + " | "
                    + ticket.getSlotId() + " | "
                    + (ticket.getTimeIn() != null ? ticket.getTimeIn() : "") + " | "
                    + (ticket.getTimeOut() != null ? ticket.getTimeOut() : "") + " | "
                    + ticket.getTotalFee());
        }
    }
}