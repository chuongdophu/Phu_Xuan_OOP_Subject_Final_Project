package com.autoparking.service;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

public class ExcelReportExporter {

    public static void exportReportFromTxtFiles() {
        String exportFileName = "Parking_Report.csv";
        File activeFile = new File("active_vehicles.txt");
        File historyFile = new File("history.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(exportFileName))) {
            writer.write('\ufeff'); // UTF-8 BOM giup Excel mo khong loi font
            writer.println("Ticket ID,License Plate,Vehicle Type,Parked Slot,Time In,Time Out,Total Fee (VND),Status");

            int count = 0;

            // Doc xe dang do tu active_vehicles.txt
            if (activeFile.exists()) {
                try (Scanner scanner = new Scanner(activeFile)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (line.isEmpty() || line.startsWith("#"))
                            continue;

                        String[] parts = line.split("\\|");
                        if (parts.length >= 6) {
                            writer.println(parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4]
                                    + ",N/A,0,PARKED");
                            count++;
                        }
                    }
                }
            }

            // Doc xe da thanh toan xong 1 vong tu history.txt
            if (historyFile.exists()) {
                try (Scanner scanner = new Scanner(historyFile)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (line.isEmpty() || line.startsWith("#"))
                            continue;

                        String[] parts = line.split("\\|");
                        if (parts.length >= 8) {
                            writer.println(parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4]
                                    + "," + parts[5] + "," + parts[6] + ",COMPLETED");
                            count++;
                        }
                    }
                }
            }

            writer.flush();
            System.out.println("\n==================================================");
            System.out.println(" [THANH CONG] Da xuat " + count + " dong du lieu tu file text sang Excel!");
            System.out.println(" [DUONG DAN] " + new File(exportFileName).getAbsolutePath());
            System.out.println("==================================================\n");

        } catch (Exception e) {
            System.out.println("\n[LOI] Khong the xuat bao cao Excel: " + e.getMessage());
        }
    }
}