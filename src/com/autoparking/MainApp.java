package com.autoparking;

import com.autoparking.core.interfaces.*;
import com.autoparking.factory.*;
import com.autoparking.model.*;
import com.autoparking.service.ParkingManagerService;
import com.autoparking.service.impl.*;
import com.autoparking.ui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        IExcelTableBuilder tableBuilder = new ExcelTableBuilderImpl();
        tableBuilder.initializeExcelFiles();

        ITimeProvider timeProvider = new RealTimeProviderImpl();
        ITicketFactory ticketFactory = new TicketFactoryImpl(timeProvider);
        ITicketValidator ticketValidator = new TicketValidatorImpl();
        IHistoryManager historyManager = new ExcelHistoryManagerImpl();
        IReportExporter reportExporter = new ExcelReportExporterImpl(historyManager);
        UserProfileProviderImpl profileProvider = new UserProfileProviderImpl();

        // 1. Quét kiểm tra Enterprise Name / User Data trước khi Onboarding
        UserProfile profile = UserProfileFactory.checkOrRunOnboarding(scanner, profileProvider);

        // 2. Pass profile vào Calculator
        IPricingCalculator pricingCalculator = new FlexiblePricingCalculatorImpl(profile);

        ParkingManagerService parkingService = new ParkingManagerService(
                profile, ticketFactory, ticketValidator, pricingCalculator, timeProvider, historyManager,
                profileProvider);

        boolean running = true;
        while (running) {
            // Lấy profile mới nhất từ service để UI hiển thị đúng cấu hình
            profile = parkingService.getProfile();
            ClientViewMapper.renderMenu(profile);
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    System.out.print("Enter License Plate: ");
                    String plate = scanner.nextLine().trim();

                    List<VehicleType> activeTypes = new ArrayList<>(profile.getSupportedVehicles());
                    System.out.println("Select Vehicle Type:");
                    for (int i = 0; i < activeTypes.size(); i++) {
                        System.out.println((i + 1) + ". " + activeTypes.get(i).name());
                    }
                    System.out.print("Choice: ");
                    int typeChoice = 1;
                    try {
                        typeChoice = Integer.parseInt(scanner.nextLine().trim());
                    } catch (Exception ignored) {
                    }

                    VehicleType selectedType = (typeChoice >= 1 && typeChoice <= activeTypes.size())
                            ? activeTypes.get(typeChoice - 1)
                            : activeTypes.get(0);

                    Ticket newTicket = parkingService.checkIn(plate, selectedType);
                    if (newTicket != null) {
                        TicketConsolePrinter.printCheckInTicket(newTicket);
                    }
                    break;

                case "2":
                    System.out.print("Enter Ticket ID to Check-out: ");
                    String ticketId = scanner.nextLine().trim();
                    Ticket completedTicket = parkingService.checkOut(ticketId);
                    if (completedTicket != null) {
                        TicketConsolePrinter.printCheckOutReceipt(completedTicket);
                    }
                    break;

                case "3":
                    System.out.println("\n=== ACTIVE TICKETS MATRIX OUTPUT ===");
                    System.out.println("1. View current active tickets on console");
                    System.out.println("2. Export current active tickets as CSV");
                    System.out.print("Choose: ");
                    String matrixChoice = scanner.nextLine().trim();

                    if ("1".equals(matrixChoice)) {
                        parkingService.displayMatrix();
                        reportExporter.printActiveTickets();
                    } else if ("2".equals(matrixChoice)) {
                        reportExporter.exportActiveTicketsCsv();
                    } else {
                        System.out.println("\n[ERROR] Invalid Option!");
                    }
                    break;

                case "4":
                    if (profile.isLargeScale()) {
                        System.out.println("\n=== HISTORY REPORT EXPORT ===");
                        System.out.println("1. View history on console");
                        System.out.println("2. Export history to CSV file");
                        System.out.print("Choose: ");
                        String reportChoice = scanner.nextLine().trim();

                        if ("1".equals(reportChoice)) {
                            reportExporter.printHistory();
                        } else if ("2".equals(reportChoice)) {
                            reportExporter.exportHistoryCsv();
                        } else {
                            reportExporter.exportReport();
                        }
                    } else {
                        System.out.println("\n[ERROR] Invalid Option!");
                    }
                    break;

                case "5":
                    if (profile.isLargeScale()) {
                        System.out.println("\n[INFO] Zone Rules Active:");
                        profile.getVehicleZones().forEach((type, floors) -> System.out
                                .println(" - " + type.name() + " -> Allowed Floors: " + floors));
                    } else {
                        System.out.println("\n[ERROR] Invalid Option!");
                    }
                    break;

                case "6":
                    // Chức năng CẬP NHẬT CẤU HÌNH BÃI XE
                    System.out.println("\n=== CAP NHAT CAU HINH BAI XE ===");
                    UserProfile currentProfile = parkingService.getProfile();
                    UserProfile updatedProfile = UserProfileFactory.updateExistingProfile(scanner, currentProfile,
                            profileProvider);
                    if (updatedProfile != null) {
                        parkingService.updateSystemSetup(updatedProfile);
                    }
                    break;

                case "0":
                    running = false;
                    System.out.println("\nExiting System. Goodbye!");
                    break;

                default:
                    System.out.println("\n[ERROR] Invalid Option!");
                    break;
            }
        }
        scanner.close();
    }
}