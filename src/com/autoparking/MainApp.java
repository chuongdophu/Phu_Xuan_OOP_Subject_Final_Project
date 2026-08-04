package com.autoparking;

import com.autoparking.config.ParkingConfig;
import com.autoparking.model.Ticket;
import com.autoparking.model.VehicleType;
import com.autoparking.service.ParkingManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Ticket> activeTickets = new ArrayList<>();
        List<Ticket> historicalTickets = new ArrayList<>();

        // 1. Load Cau hinh & Thong tin xe tu config.txt
        ParkingConfig config = ParkingConfig.loadOrCreateConfig(scanner, activeTickets, historicalTickets);
        ParkingManager manager = new ParkingManager(config, activeTickets, historicalTickets);

        boolean running = true;
        while (running) {
            System.out.println("\n========== AUTOMATED PARKING SYSTEM ==========");
            System.out.println("1. Check-In Vehicle (Gui xe)");
            System.out.println("2. Check-Out Vehicle (Lay xe)");
            System.out.println("3. Display Parking Status (Trang thai bai)");
            System.out.println("4. Display Active Vehicles (Xe dang do)");
            System.out.println("5. Sync Data to config.txt (Luu du lieu)");
            System.out.println("0. Exit (Thoat)");
            System.out.print("Choose option [0-5]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter License Plate: ");
                    String plate = scanner.nextLine().trim();
                    if (plate.isEmpty()) {
                        System.out.println("[LOI] Bien so xe khong duoc de trong!");
                        break;
                    }
                    System.out.println("Select Vehicle Type:");
                    System.out.println(" 1. CAR");
                    System.out.println(" 2. MOTORBIKE");
                    System.out.println(" 3. ELECTRIC VEHICLE (EV)");
                    System.out.println(" 4. TRUCK");
                    System.out.println(" 5. BUS");
                    System.out.print("Choice: ");
                    String tChoice = scanner.nextLine().trim();
                    VehicleType type = switch (tChoice) {
                        case "2" -> VehicleType.MOTORBIKE;
                        case "3" -> VehicleType.EV;
                        case "4" -> VehicleType.TRUCK;
                        case "5" -> VehicleType.BUS;
                        default -> VehicleType.CAR;
                    };
                    manager.processCheckIn(plate, type);
                }
                case "2" -> {
                    System.out.print("Enter Ticket ID or License Plate to Checkout: ");
                    String query = scanner.nextLine().trim();
                    if (!query.isEmpty()) {
                        manager.processCheckOut(query);
                    }
                }
                case "3" -> manager.displayParkingStatus();
                case "4" -> manager.displayActiveVehicles();
                case "5" -> manager.exportReport();
                case "0" -> {
                    running = false;
                    System.out.println("\n[SYSTEM] Cam on ban da su dung phan mem. Tam biet!");
                }
                default -> System.out.println("[LOI] Lua chon khong hop le!");
            }
        }
        scanner.close();
    }
}