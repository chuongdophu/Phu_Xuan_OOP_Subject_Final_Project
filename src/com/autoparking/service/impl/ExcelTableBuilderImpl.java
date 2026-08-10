package com.autoparking.service.impl;

import com.autoparking.config.MultiLotConfig;
import com.autoparking.core.interfaces.IExcelTableBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExcelTableBuilderImpl implements IExcelTableBuilder {

    private static final String ACTIVE_FILE = MultiLotConfig.ACTIVE_TICKETS_FILE;
    private static final String HISTORY_FILE = MultiLotConfig.HISTORY_FILE;

    @Override
    public void initializeExcelFiles() {
        createFileIfNotExists(ACTIVE_FILE, "TicketID|LicensePlate|VehicleType|SlotID|TimeIn");
        createFileIfNotExists(HISTORY_FILE, "TicketID|LicensePlate|VehicleType|SlotID|TimeIn|TimeOut|TotalFee");
    }

    private void createFileIfNotExists(String fileName, String headerLine) {
        File file = new File(fileName);
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        // Chỉ tạo file khi chưa tồn tại để tránh wipe dữ liệu cũ
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(headerLine);
                writer.write(System.lineSeparator());
                System.out.println("[SYSTEM] Created missing file structure: " + fileName);
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to initialize file " + fileName + ": " + e.getMessage());
            }
        }
    }
}