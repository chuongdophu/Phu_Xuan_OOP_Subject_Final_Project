package com.autoparking.service.impl;

import com.autoparking.config.MultiLotConfig;
import com.autoparking.core.interfaces.IHistoryManager;
import com.autoparking.model.Ticket;
import com.autoparking.model.VehicleType;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelHistoryManagerImpl implements IHistoryManager {

    private static final String HISTORY_FILE = MultiLotConfig.HISTORY_FILE;
    private static final String ACTIVE_FILE = MultiLotConfig.ACTIVE_TICKETS_FILE;

    private static final String DELIMITER = "|";
    private static final Pattern LEADING_NUMBER_PATTERN = Pattern.compile("^-?\\d+(?:\\.\\d+)?");

    @Override
    public void recordTransaction(Ticket ticket) {
        File file = new File(HISTORY_FILE);
        File dir = new File(MultiLotConfig.DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileWriter fw = new FileWriter(file, true)) {
            if (needsLeadingNewline(file)) {
                fw.write(System.lineSeparator());
            }
            String line = toTransactionLine(ticket);
            fw.write(line);
            fw.write(System.lineSeparator());
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to record transaction: " + e.getMessage());
        }
    }

    @Override
    public void recordActiveTicket(Ticket ticket) {
        File file = new File(ACTIVE_FILE);
        File dir = new File(MultiLotConfig.DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileWriter fw = new FileWriter(file, true)) {
            if (needsLeadingNewline(file)) {
                fw.write(System.lineSeparator());
            }
            String line = toActiveLine(ticket);
            fw.write(line);
            fw.write(System.lineSeparator());
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to record active ticket: " + e.getMessage());
        }
    }

    @Override
    public List<Ticket> loadActiveTickets() {
        List<Ticket> activeTickets = new ArrayList<>();
        File file = new File(ACTIVE_FILE);
        if (!file.exists()) {
            return activeTickets;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|");
                if (parts.length < 5) {
                    continue;
                }

                String ticketId = parts[0].trim();
                String plate = parts[1].trim();
                VehicleType type = VehicleType.valueOf(parts[2].trim());
                String slotId = parts[3].trim();
                String timeInStr = parts[4].trim();
                LocalDateTime timeIn = (timeInStr != null && !timeInStr.isBlank()) ? LocalDateTime.parse(timeInStr)
                        : null;

                if (ticketId.isEmpty()) {
                    continue;
                }

                Ticket ticket = new Ticket(ticketId, plate, type, slotId, timeIn);
                activeTickets.add(ticket);
            }
        } catch (Exception e) {
            System.out.println("[WARNING] Could not read active tickets: " + e.getMessage());
        }
        return activeTickets;
    }

    @Override
    public List<Ticket> getHistoryList() {
        List<Ticket> historyList = new ArrayList<>();
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            return historyList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                try {
                    String[] parts = line.split("\\|");
                    if (parts.length < 7) {
                        continue;
                    }

                    String ticketId = parts[0].trim();
                    String plate = parts[1].trim();
                    VehicleType type = VehicleType.valueOf(parts[2].trim());
                    String slotId = parts[3].trim();
                    String timeInStr = parts[4].trim();
                    String timeOutStr = parts[5].trim();
                    double fee = parseLeadingFee(parts[6].trim());

                    if (ticketId.isEmpty()) {
                        continue;
                    }

                    LocalDateTime timeIn = (timeInStr != null && !timeInStr.isBlank()) ? LocalDateTime.parse(timeInStr)
                            : null;
                    LocalDateTime timeOut = (timeOutStr != null && !timeOutStr.isBlank())
                            ? LocalDateTime.parse(timeOutStr)
                            : null;

                    Ticket ticket = new Ticket(ticketId, plate, type, slotId, timeIn);
                    ticket.setTimeOut(timeOut);
                    ticket.setTotalFee(fee);
                    historyList.add(ticket);
                } catch (Exception lineError) {
                    System.out.println(
                            "[WARNING] Skipped malformed history row " + lineNumber + ": " + lineError.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("[WARNING] Could not read full history: " + e.getMessage());
        }
        return historyList;
    }

    private String toActiveLine(Ticket ticket) {
        return String.join(DELIMITER,
                ticket.getTicketId(),
                ticket.getLicensePlate(),
                ticket.getVehicleType().name(),
                ticket.getSlotId(),
                ticket.getTimeIn() != null ? ticket.getTimeIn().toString() : "");
    }

    private String toTransactionLine(Ticket ticket) {
        return String.join(DELIMITER,
                ticket.getTicketId(),
                ticket.getLicensePlate(),
                ticket.getVehicleType().name(),
                ticket.getSlotId(),
                ticket.getTimeIn() != null ? ticket.getTimeIn().toString() : "",
                ticket.getTimeOut() != null ? ticket.getTimeOut().toString() : "",
                String.valueOf(ticket.getTotalFee()));
    }

    private double parseLeadingFee(String feeText) {
        Matcher matcher = LEADING_NUMBER_PATTERN.matcher(feeText);
        if (!matcher.find()) {
            throw new NumberFormatException("Invalid fee value: " + feeText);
        }
        return Double.parseDouble(matcher.group());
    }

    private boolean needsLeadingNewline(File file) {
        if (!file.exists() || file.length() == 0) {
            return false;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(file.length() - 1);
            int lastByte = raf.read();
            return lastByte != '\n' && lastByte != '\r';
        } catch (IOException e) {
            return false;
        }
    }
}