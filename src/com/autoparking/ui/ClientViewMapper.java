package com.autoparking.ui;

import com.autoparking.model.UserProfile;

public class ClientViewMapper {
    public static void renderMenu(UserProfile profile) {
        System.out.println("\n==============================================");
        System.out.println("   " + profile.getEnterpriseName().toUpperCase() + " - MANAGEMENT MENU");
        System.out.println("==============================================");
        System.out.println("1. Check-in Vehicle");
        System.out.println("2. Check-out Vehicle");
        System.out.println("3. View Parking Matrix");

        if (profile.isLargeScale()) {
            System.out.println("4. Export Analytics Report");
            System.out.println("5. Zone Management Dashboard");
        }

        System.out.println("6. System Setup & Configuration");
        System.out.println("0. Exit Application");
        System.out.println("==============================================");
        System.out.print("Select action: ");
    }

    public static void renderManagementDashboard(UserProfile profile) {
        System.out.println("\n==============================================");
        System.out.println("      ⚙️ SYSTEM MANAGEMENT SETUP DASHBOARD     ");
        System.out.println("==============================================");
        System.out.println(" Enterprise Name : " + profile.getEnterpriseName());
        System.out.println(" Total Floors    : " + profile.getTotalFloors());
        System.out.println(" Slot Distribution per Floor:");
        profile.getFloorSlotsMap()
                .forEach((floor, slots) -> System.out.println("   - Floor " + floor + ": " + slots + " slots"));
        System.out.println(" Total Capacity  : " + profile.getTotalSlots() + " slots");
        System.out.println(" System Mode     : " + (profile.isLargeScale() ? "Large Scale Enterprise" : "Standard"));
        System.out.println(" Overnight Fee   : " + (profile.hasOvernightFee() ? "ENABLED" : "DISABLED"));
        System.out.println("----------------------------------------------");
        System.out.println(" [1] Edit / Reconfigure System Setup");
        System.out.println(" [0] Back to Main Menu");
        System.out.println("==============================================");
        System.out.print("Select action: ");
    }
}