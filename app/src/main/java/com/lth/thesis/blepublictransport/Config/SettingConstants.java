package com.lth.thesis.blepublictransport.Config;

/**
 * A class containing constants for the SharedPreference objects.
 *
 * @author Jacob Arvidsson & Jonathan Vidmar
 * @version 1.1
 */
public class SettingConstants {
    public static final String SETTINGS_PREFERENCES = "settings_preferences";
    public static final String TICKET_PREFERENCES = "ticket_preferences";

    public static final String DESTINATION_DEPENDENT_PRICE = "destination_dependent_price";
    public static final String VALID_TICKET_DATE = "valid_ticket_date";
    public static final String BOUGHT_TICKET_DATE = "bought_ticket_date";
    public static final String BOUGHT_TICKET_DESTINATION = "bought_ticket_destination";
    public static final String PAY_AUTOMATICALLY = "pay_automatically";
    public static final String HAS_SUBSCRIPTION = "has_subscription";
    public static final String KALMAN_SEEK_VALUE = "kalman_filter_mode";
    public static final String SELF_CORRECTING_BEACON = "self_correcting_beacon";
    public static final String WALK_DETECTION = "walk_detection";
    public static final String SIMULATE_GATE = "simulate_gate";


    public static final double KALMAN_NOISE_MIN = 0.01;
    public static final double KALMAN_NOISE_MAX = 25.0;
}
