package com.autoparking.config;

public class MultiLotConfig {
    public static final String DB_DIR = "database/";
    public static final String USER_PROFILE_FILE = DB_DIR + "user_profile.properties";
    public static final String ACTIVE_TICKETS_FILE = DB_DIR + "active_tickets.txt";
    public static final String HISTORY_FILE = DB_DIR + "history_parking.txt";

    public static final int DEFAULT_FLOORS = 3;
    public static final int DEFAULT_SLOTS_PER_FLOOR = 10;
    public static final double DEFAULT_BASE_RATE = 10000.0;
}