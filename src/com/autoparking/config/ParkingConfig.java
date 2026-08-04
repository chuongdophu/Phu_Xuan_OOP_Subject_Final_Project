package com.autoparking.config;

import com.autoparking.model.Ticket;
import com.autoparking.model.VehicleType;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ParkingConfig {
    private String lotName;
    private int totalFloors;
    private int slotsPerFloor;
    private Map<VehicleType, Double> rates = new EnumMap<>(VehicleType.class);

    public ParkingConfig(String lotName, int totalFloors, int slotsPerFloor) {
        this.lotName = lotName;
        this.totalFloors = totalFloors;
        this.slotsPerFloor = slotsPerFloor;
    }

    // Load toan bo Cau hinh + Thong tin ve xe khach hang tu config.txt
    public static ParkingConfig loadOrCreateConfig(Scanner scanner, List<Ticket> activeTickets,
            List<Ticket> historicalTickets) {
        File configFile = new File("config.txt");

        if (configFile.exists()) {
            try (Scanner fileScanner = new Scanner(configFile)) {
                String name = "Phu Xuan Auto Parking";
                int floors = 3;
                int slots = 10;
                Map<VehicleType, Double> loadedRates = new EnumMap<>(VehicleType.class);

                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty() || line.startsWith("#"))
                        continue;

                    if (line.startsWith("LOT_NAME="))
                        name = line.substring(9).trim();
                    else if (line.startsWith("TOTAL_FLOORS="))
                        floors = Integer.parseInt(line.substring(13).trim());
                    else if (line.startsWith("SLOTS_PER_FLOOR="))
                        slots = Integer.parseInt(line.substring(16).trim());
                    else if (line.startsWith("RATE_")) {
                        String[] parts = line.split("=");
                        String typeKey = parts[0].replace("RATE_", "").trim();
                        double price = Double.parseDouble(parts[1].trim());
                        try {
                            loadedRates.put(VehicleType.valueOf(typeKey), price);
                        } catch (Exception ignored) {
                        }
                    } else if (line.startsWith("CUSTOMER_TICKET|")) {
                        // Doc thong tin ve xe/khach hang
                        String[] parts = line.split("\\|");
                        if (parts.length >= 8) {
                            String ticketId = parts[1].trim();
                            String plate = parts[2].trim();
                            VehicleType type = VehicleType.valueOf(parts[3].trim());
                            String slotId = parts[4].trim();
                            LocalDateTime timeIn = LocalDateTime.parse(parts[5].trim());
                            String timeOutStr = parts[6].trim();
                            double fee = Double.parseDouble(parts[7].trim());
                            String status = parts[8].trim();

                            Ticket ticket = new Ticket(ticketId, plate, type, slotId, timeIn);
                            if ("PARKED".equalsIgnoreCase(status)) {
                                activeTickets.add(ticket);
                            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                                if (!"N/A".equalsIgnoreCase(timeOutStr)) {
                                    ticket.setTimeOut(LocalDateTime.parse(timeOutStr));
                                }
                                ticket.setTotalPrice(fee);
                                historicalTickets.add(ticket);
                            }
                        }
                    }
                }

                ParkingConfig config = new ParkingConfig(name, floors, slots);
                config.rates = loadedRates;

                System.out.println("==================================================");
                System.out.println("   [HE THONG] Da tai cau hinh va du lieu tu 'config.txt'");
                System.out.println("   Ten bai xe : " + name);
                System.out.println("   Suc chua   : " + (floors * slots) + " cho do");
                System.out.println("   Xe dang do : " + activeTickets.size() + " xe");
                System.out.println("==================================================");

                return config;
            } catch (Exception e) {
                System.out.println("[CANH BAO] Loi khi doc config.txt, dang mo trinh thiet lap...");
            }
        }

        return setupNewConfig(scanner);
    }

    private static ParkingConfig setupNewConfig(Scanner scanner) {
        System.out.println("\n==================================================");
        System.out.println("       THIET LAP HE THONG LAN DAU                ");
        System.out.println("==================================================");

        System.out.print("Nhap ten bai xe (MD: Phu Xuan Auto Parking): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty())
            name = "Phu Xuan Auto Parking";

        System.out.print("Nhap tong so tang (MD: 3): ");
        int floors = parseInputInt(scanner, 3);

        System.out.print("Nhap so o do tren moi tang (MD: 10): ");
        int slots = parseInputInt(scanner, 10);

        ParkingConfig config = new ParkingConfig(name, floors, slots);

        System.out.println("\n--- CAI DAT BANG GIA GUI XE (VND / Gio) ---");
        for (VehicleType type : VehicleType.values()) {
            System.out.print("Gia gui xe " + type.getDescription() + " (" + type.name() + "): ");
            double defaultRate = switch (type) {
                case MOTORBIKE -> 5000;
                case CAR -> 20000;
                case EV -> 15000;
                case TRUCK -> 40000;
                case BUS -> 50000;
            };
            double rate = parseInputDouble(scanner, defaultRate);
            config.rates.put(type, rate);
        }

        config.saveToFile(List.of(), List.of());
        System.out.println("\n[THANH CONG] Da luu cau hinh vao 'config.txt'!\n");
        return config;
    }

    // Luu toan bo Cau hinh + Danh sach ve xe khach hang vao config.txt
    public void saveToFile(List<Ticket> activeTickets, List<Ticket> historicalTickets) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("config.txt"))) {
            writer.println("# CAU HINH HE THONG BAI XE");
            writer.println("LOT_NAME=" + lotName);
            writer.println("TOTAL_FLOORS=" + totalFloors);
            writer.println("SLOTS_PER_FLOOR=" + slotsPerFloor);

            writer.println("\n# BANG GIA GUI XE (VND)");
            for (Map.Entry<VehicleType, Double> entry : rates.entrySet()) {
                writer.println("RATE_" + entry.getKey().name() + "=" + entry.getValue());
            }

            writer.println("\n# DANH SACH THONG TIN KHACH HANG & VE XE");
            writer.println("# CUSTOMER_TICKET|TicketID|Plate|VehicleType|Slot|TimeIn|TimeOut|Fee|Status");
            for (Ticket t : activeTickets) {
                writer.println("CUSTOMER_TICKET|" + t.getTicketId() + "|" +
                        t.getLicensePlate() + "|" +
                        t.getVehicleType().name() + "|" +
                        t.getParkLotId() + "|" +
                        t.getTimeIn() + "|N/A|0|PARKED");
            }

            for (Ticket t : historicalTickets) {
                writer.println("CUSTOMER_TICKET|" + t.getTicketId() + "|" +
                        t.getLicensePlate() + "|" +
                        t.getVehicleType().name() + "|" +
                        t.getParkLotId() + "|" +
                        t.getTimeIn() + "|" +
                        t.getTimeOut() + "|" +
                        t.getTotalPrice() + "|COMPLETED");
            }

        } catch (Exception e) {
            System.out.println("[LOI] Khong the luu vao file config.txt: " + e.getMessage());
        }
    }

    private static int parseInputInt(Scanner scanner, int defaultValue) {
        try {
            String input = scanner.nextLine().trim();
            return input.isEmpty() ? defaultValue : Integer.parseInt(input);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static double parseInputDouble(Scanner scanner, double defaultValue) {
        try {
            String input = scanner.nextLine().trim();
            return input.isEmpty() ? defaultValue : Double.parseDouble(input);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getLotName() {
        return lotName;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public int getSlotsPerFloor() {
        return slotsPerFloor;
    }

    public int getTotalCapacity() {
        return totalFloors * slotsPerFloor;
    }

    public boolean isFloorAllowed(int floor, VehicleType type) {
        return true;
    }

    public double getRate(VehicleType type) {
        return rates.getOrDefault(type, 10000.0);
    }
}